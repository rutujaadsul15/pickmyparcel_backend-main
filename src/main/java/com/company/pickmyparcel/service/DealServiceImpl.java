package com.company.pickmyparcel.service;


import com.company.pickmyparcel.controller.DealController;
import com.company.pickmyparcel.exception.EntityNotFoundException;
import com.company.pickmyparcel.model.*;
import com.company.pickmyparcel.model.requests.RazorPayPaymentOrderDetails;
import com.company.pickmyparcel.model.requests.SearchDealsRequest;
import com.company.pickmyparcel.repository.*;
import com.company.pickmyparcel.util.DistanceMatrixElementWrapper;
import com.company.pickmyparcel.util.DistanceMatrixResponseWrapper;
import com.company.pickmyparcel.util.DistanceMatrixRowWrapper;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.*;
import com.razorpay.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class DealServiceImpl implements DealService {
    private static final Logger LOGGER = Logger.getLogger(DealServiceImpl.class.getName());
    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReceiverOtpRepository receiverOtpRepository;

    @Autowired
    private PriceMaintenanceRepository priceMaintenanceRepository;

    @Autowired
    private DealPaymentInfoRepository dealPaymentInfoRepository;


    @Override
    public Deal saveDeal(Deal deal) {
        deal.setDealPaymentStatus(DealPaymentStatus.PAYMENT_INITIATED);
        return dealRepository.save(deal);
    }
    @Override
    public Deal saveDealViaPaytm(Deal deal, DealPaymentStatus dealPaymentStatus) {
        deal.getParcel().setParcelStatus(ParcelStatus.PENDING_FOR_CARRIER_ACCEPTANCE);
        deal.setDealPaymentStatus(dealPaymentStatus);
        return dealRepository.save(deal);
    }

    @Override
    public Deal calculateDealDistanceAndDealTotal(Deal deal) {
        this.calculateDealDistanceInKm(deal);
        this.calculateDealTotal(deal);
        return deal;
    }

    private void calculateDealTotal(Deal deal) {
        List<PriceMaintenance> priceMaintenanceList = priceMaintenanceRepository.findAll();
        Double distance = deal.getDealDistanceInKilometers();
        Long parcelWeight = deal.getParcel().getParcelWeight();
        if (distance == null || parcelWeight == null) {
            throw new RuntimeException("distance or parcel weight is null");
        }

        priceMaintenanceList.forEach(p -> {
            double fromKmsDb = Double.parseDouble(p.getFromKms());
            double toKmsDb = Double.parseDouble(p.getToKms());
            double fromGmsDb = Double.parseDouble(p.getFromGms());
            double toGmsDb = Double.parseDouble(p.getToGms());
            if (fromKmsDb <= distance && toKmsDb >= distance && fromGmsDb <= parcelWeight && toGmsDb >= parcelWeight) {
                double dealTotal = Double.parseDouble(p.getPrice());
                double serviceChargePercent = Double.parseDouble(p.getServiceChargePercent());
                double dealTotalAsCarrier = dealTotal - (dealTotal * (serviceChargePercent / 100));
                deal.setDealTotal(dealTotal);
                deal.setDealTotalAsCarrier(dealTotalAsCarrier);
            }
        });
    }


    private void calculateDealDistanceInKm(Deal deal) {
        String pickupLoc = deal.getPickUpLocation().getAddressLine();
        String dropLoc = deal.getDropLocation().getAddressLine();
        final String uri = "https://maps.googleapis.com/maps/api/distancematrix/json?origins="
                + pickupLoc + "&destinations="
                + dropLoc + "&units=kilometers&mode=driving"
                + "&key=AIzaSyBxVJBJjXRgTQIpOk3bf9sJ_zzfICJJ5jU";
        RestTemplate restTemplate = new RestTemplate();
        Double dealDistance = null;
        DistanceMatrixResponseWrapper distanceMatrixResponse = restTemplate.getForObject(uri, DistanceMatrixResponseWrapper.class);
        if (distanceMatrixResponse != null && distanceMatrixResponse.getRows().length > 0) {
            DistanceMatrixRowWrapper row = distanceMatrixResponse.getRows()[0];
            if (row != null) {
                DistanceMatrixElementWrapper element = row.getElements()[0];
                if (element != null && element.getDistance() != null) {
                    String distance = element.getDistance().getText();
                    if (distance != null && distance.contains("km") || Objects.requireNonNull(distance).contains("KM")) {
                        String kms = distance.split(" ")[0];
                        dealDistance = Double.parseDouble(kms);
                    }
                }
            }
        }
        if (dealDistance != null) {
            deal.setDealDistanceInKilometers(dealDistance);
        } else {
            throw new RuntimeException("Exception occurred while calculating deal distance");
        }
    }

    @Override
    public Deal getDealByDealId(Integer id) {
        Optional<Deal> dealOptionalData = dealRepository.findById(id);
        if (dealOptionalData.isPresent()) {
            return dealOptionalData.get();
        } else
            throw new EntityNotFoundException("Deal", "Not Found in Database");
    }


    @Override
    public List<Deal> searchDeals(SearchDealsRequest searchDealsRequest) {
        List<Deal> dealList = dealRepository.findAll();
        LinkedHashSet<String> onRoutePinCodes =
                findOnRouteVillages(searchDealsRequest.getPickUpLocation().getLatitude(), searchDealsRequest.getPickUpLocation().getLongitude(),
                                    searchDealsRequest.getDropLocation().getLatitude(), searchDealsRequest.getDropLocation().getLongitude());
        boolean isPickupPinCodeExist = onRoutePinCodes.contains(searchDealsRequest.getPickUpLocation().getPinCode());
        boolean isDropPinCodeExist = onRoutePinCodes.contains(searchDealsRequest.getDropLocation().getPinCode());
        if (isPickupPinCodeExist && isDropPinCodeExist) {
            return dealList.stream()
                    .filter(deal -> deal.getPickUpLocation().getPinCode().equals(searchDealsRequest.getPickUpLocation().getPinCode()))
                    .filter(deal -> deal.getDropLocation().getPinCode().equals(searchDealsRequest.getDropLocation().getPinCode()))
                    .filter(deal -> deal.getParcel().getParcelStatus().equals(ParcelStatus.PENDING_FOR_CARRIER_ACCEPTANCE))
                    .filter(deal -> deal.getDealPaymentStatus().equals(DealPaymentStatus.PAID))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public LinkedHashSet<String> findOnRouteVillages(Double pickupLat, Double pickupLng, Double dropLat, Double dropLng) {
        CountDownLatch latch = new CountDownLatch(1);
        GeoApiContext context = new GeoApiContext.Builder().apiKey("AIzaSyBxVJBJjXRgTQIpOk3bf9sJ_zzfICJJ5jU").build();
        DirectionsApiRequest apiRequest = DirectionsApi.newRequest(context);
        apiRequest.origin(new LatLng(pickupLat, pickupLng));
        apiRequest.destination(new LatLng(dropLat, dropLng));
        apiRequest.transitMode(TransitMode.BUS);

        LinkedHashSet<String> onRoutePinCodeSet = new LinkedHashSet<>();
        apiRequest.setCallback(new com.google.maps.PendingResult.Callback<DirectionsResult>() { // calling direction api to get on road waypoints (on raod lat, lng)
            @Override
            public void onResult(DirectionsResult result) {
                DirectionsRoute[] routes = result.routes;
                Arrays.stream(routes).forEach(directionsRoute -> Arrays.stream(directionsRoute.legs)
                        .forEach(directionsLeg -> Arrays.stream(directionsLeg.steps)
                                .filter(directionsStep -> directionsStep.endLocation != null)
                                .forEach(s -> {
                                    Double lat = s.endLocation.lat;
                                    Double lng = s.endLocation.lng;
                                    String pinCode = getPinCodeFromLatAndLng(lat, lng); // call reverse geocoding api to find pincode from lat, lng and add it into set
                                    if (pinCode != null) {
                                        onRoutePinCodeSet.add(pinCode);
                                    }
                                })));
                latch.countDown();
                System.out.println("ON ROUTE PIN CODE SET : " + onRoutePinCodeSet);
            }

            @Override
            public void onFailure(Throwable e) {
                System.out.println("FAILED IN CALLBACK WHILE FINDING ON ROUTE VILLAGE");
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return onRoutePinCodeSet;
    }

    private String getPinCodeFromLatAndLng(Double lat, Double lng) {
        GeoApiContext context = new GeoApiContext.Builder().apiKey("AIzaSyBxVJBJjXRgTQIpOk3bf9sJ_zzfICJJ5jU").build();
        try {
            GeocodingResult[] results = GeocodingApi.reverseGeocode(context, new LatLng(lat, lng)).await();
            Optional<AddressComponent> shortlistedAddress = Arrays.stream(results)
                    .filter(geocodingResult -> geocodingResult.geometry.locationType.equals(LocationType.ROOFTOP) ||
                            geocodingResult.geometry.locationType.equals(LocationType.GEOMETRIC_CENTER))
                    .findFirst()
                    .flatMap(geocodingResult -> Arrays.stream(geocodingResult.addressComponents)
                            .filter(addressComponent -> Arrays.asList(addressComponent.types).contains(AddressComponentType.POSTAL_CODE))
                            .findFirst());
            if (shortlistedAddress.isPresent()) {
                return shortlistedAddress.get().longName;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error on Reverse Geocoding");
        }
        return null;
    }

    @Override
    public Deal requestSenderForConfirmation(Deal acceptDealRequest) {
        Optional<Deal> dealOptional = dealRepository.findById(acceptDealRequest.getDealId());
        Optional<User> userOptional = userRepository.findById(acceptDealRequest.getCarrier().getCarrierContactNo());
        if (dealOptional.isPresent() && userOptional.isPresent()) {
            Deal deal = dealOptional.get();
            Carrier carrier = new Carrier();
            carrier.setCarrierContactNo(acceptDealRequest.getCarrier().getCarrierContactNo());
            carrier.setCarrierFirstName(userOptional.get().getFirstName());
            carrier.setCarrierLastName(userOptional.get().getLastName());
            carrier.setVehicleDetails(acceptDealRequest.getCarrier().getVehicleDetails());
            deal.setCarrier(carrier);
            deal.getParcel().setParcelStatus(ParcelStatus.PENDING_FOR_SENDER_CONFIRMATION);
            Deal updatedDeal = dealRepository.save(deal);
            //todo : Notify SMS sender regarding deal acceptance from carrier
            return updatedDeal;
        } else {
            throw new RuntimeException("Deal does not exist with deal id : " + acceptDealRequest.getDealId());
        }
    }

    @Override
    public DealResponse getSubmittedDealsByContactNumber(String contactNumber) {
        DealResponse dealResponse = new DealResponse();
        List<Deal> dealList = dealRepository.findAll();
        if (dealList.isEmpty()) {
            throw new EntityNotFoundException("Deal", "Submitted deals not found in database with this contact number");
        } else {
            List<Deal> mySubmittedDealList = new ArrayList<>();
            for (Deal deal : dealList) {
                if (deal.getSender() != null && deal.getSender().getSenderContactNo() != null && deal.getSender().getSenderContactNo().equals(contactNumber) &&
                        !deal.getDealPaymentStatus().equals(DealPaymentStatus.FAILED)) {
                    mySubmittedDealList.add(deal);
                }
            }
            dealResponse.setSelfCreatedDealList(mySubmittedDealList);
        }
        return dealResponse;
    }

    @Override
    public DealResponse getPickupDealsByContactNumber(String contactNumber) {
        DealResponse dealResponse = new DealResponse();
        List<Deal> dealList = dealRepository.findAll();
        if (dealList.isEmpty()) {
            throw new EntityNotFoundException("Deal", "Pickup deals not found in database with this contact number");
        } else {
            List<Deal> myPickupDealList = new ArrayList<>();
            for (Deal deal : dealList) {
                if (deal.getCarrier() != null && deal.getCarrier().getCarrierContactNo() != null && deal.getCarrier().getCarrierContactNo().equals(contactNumber)) {
                    myPickupDealList.add(deal);
                }
            }
            dealResponse.setPickUpDealList(myPickupDealList);
        }
        return dealResponse;
    }

    @Override
    public Deal updateParcelStatus(Integer dealId, String parcelStatus) {
        Optional<Deal> optionalDealById = dealRepository.findById(dealId);
        if (optionalDealById.isPresent()) {
            Deal deal = optionalDealById.get();
            switch (parcelStatus) {
                case "PENDING_FOR_CARRIER_ACCEPTANCE": // this is initial status after deal submitted by sender
                    deal.getParcel().setParcelStatus(ParcelStatus.PENDING_FOR_CARRIER_ACCEPTANCE);
                    break;
                case "PENDING_FOR_SENDER_CONFIRMATION": // this is when accepted by carrier but pending for sender confirmation
                    deal.getParcel().setParcelStatus(ParcelStatus.PENDING_FOR_SENDER_CONFIRMATION);
                    break;
                case "LOCKED_BY_SENDER":  // this is after sender is confirming & locking the deal
                    boolean result = sendOTPToReceiver(deal);
                    if (!result) {
                        LOGGER.info("Unable to send OTP to receiver for deal id : " + dealId + " parcelStatus : " + parcelStatus);
                        return null;
                    }
                    deal.getParcel().setParcelStatus(ParcelStatus.LOCKED_BY_SENDER);
                    break;
                case "DELIVERED":  // this is after completion of parcel delivery
                    deal.getParcel().setParcelStatus(ParcelStatus.DELIVERED);
                    //todo : send sms to sender regarding parcel delivered.
                    break;
                case "CANCELLED_BY_SENDER":  // this is if deal has been cancelled due to any reason by SENDER
                    deal.getParcel().setParcelStatus(ParcelStatus.CANCELLED_BY_SENDER);
                    break;
                case "CANCELLED_BY_CARRIER":  // this is if deal has been cancelled due to any reason by CARRIER
                    deal.getParcel().setParcelStatus(ParcelStatus.CANCELLED_BY_CARRIER);
                    break;
            }
            if (deal.getParcel().getParcelStatus() != null) {
                return dealRepository.save(deal);
            }
            return null;
        } else {
            throw new EntityNotFoundException("Deal", "Not Found in Database with id : " + dealId);
        }
    }

    private boolean sendOTPToReceiver(Deal deal) {
        if (deal == null || deal.getDealId() == null || deal.getReceiver() == null || deal.getReceiver().getReceiverContactNo() == null)
            return false;
        String otp = generateOtp();
        ReceiverOtp receiverOtp = new ReceiverOtp();
        receiverOtp.setDealId(deal.getDealId());
        receiverOtp.setReceiverContactNo(deal.getReceiver().getReceiverContactNo());
        receiverOtp.setOtp(otp);
        //todo : logic to send OTP to receiver via OTP SMS ROUTE
        receiverOtpRepository.save(receiverOtp);
        return true;
    }

    private String generateOtp() {
        return new DecimalFormat("000000")
                .format(new Random().nextInt(99999));
    }

    public Boolean verifyReceiverOTP(Integer dealId, String otp) {
        Optional<ReceiverOtp> optionalReceiverOtp = receiverOtpRepository.findById(dealId);
        if (optionalReceiverOtp.isPresent() && optionalReceiverOtp.get().getOtp().equals(otp)) {
            this.updateParcelStatus(dealId, "DELIVERED");
            return true;
        }
        return false;
    }

    public Boolean updateDealPaymentStatus(RazorPayPaymentOrderDetails razorPayPaymentOrderDetails) {
        Optional<Deal> dbDeal = dealRepository.findById(razorPayPaymentOrderDetails.getDealId());
        Optional<DealPaymentInfo> dbPaymentInfo = dealPaymentInfoRepository.findById(razorPayPaymentOrderDetails.getRazorpay_order_id());
        if (dbDeal.isPresent() && razorPayPaymentOrderDetails.getPaymentStatus().equals("PAID")) {
            Deal deal = dbDeal.get();
            deal.getParcel().setParcelStatus(ParcelStatus.PENDING_FOR_CARRIER_ACCEPTANCE); // make it eligible for pickup
            deal.setDealPaymentStatus(DealPaymentStatus.PAID); // mark deal payment status paid
            dealRepository.save(deal);
            if (dbPaymentInfo.isPresent() && razorPayPaymentOrderDetails.getRazorpay_order_id().equals(dbPaymentInfo.get().getOrderId())) {
                dbPaymentInfo.get().setTransactionId(razorPayPaymentOrderDetails.getRazorpay_payment_id()); // adding transaction id
                dealPaymentInfoRepository.save(dbPaymentInfo.get());
            }
            return true;
        } else {
            return false;
        }
    }

    public void addDealPaymentInfo(Deal deal, Order order) {
        DealPaymentInfo dealPaymentInfo = new DealPaymentInfo();
        dealPaymentInfo.setDealId(deal.getDealId()); // adding deal id into payment info
        dealPaymentInfo.setOrderId(order.get("id")); // adding razorpay order id
        dealPaymentInfo.setAmount(deal.getDealTotal()); // adding total amount
        dealPaymentInfo.setTransactionId(null);  // adding razorpay transaction id as null as this is payment initiation. After payment is PAID, we will add transaction id to this record
        dealPaymentInfoRepository.save(dealPaymentInfo);
    }

    //this method is for paytm payment integration
    public void addDealPaymentInfo(Deal deal, String orderId, String transactionId, String paymentStatus) {
        DealPaymentInfo dealPaymentInfo = new DealPaymentInfo();
        dealPaymentInfo.setDealId(deal.getDealId());
        dealPaymentInfo.setOrderId(orderId);
        dealPaymentInfo.setAmount(deal.getDealTotal());
        dealPaymentInfo.setTransactionId(transactionId);
        dealPaymentInfo.setPaymentStatus(paymentStatus);
        dealPaymentInfoRepository.save(dealPaymentInfo);
    }
}
