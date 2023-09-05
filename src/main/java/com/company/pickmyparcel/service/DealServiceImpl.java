package com.company.pickmyparcel.service;


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
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
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

    @Autowired
    private WalletServiceImpl walletServiceImpl;

    private final static String SEND_ACCEPTANCE_SMS_TEXT = "Your parcel order has been picked by a carrier. Be ready with your parcel. Carrier will reach out to you soon. Thanks for choosing pickmyparcel. Jenesys";
    private static final String NOTIFY_SENDER_MSG_TEMPLATE_ID = "1707168594186969321";
    private static final String OTP_TEMPLATE_ID = "1707168594179168430";


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
    public Deal saveDealViaPhonePe(Deal deal, DealPaymentStatus dealPaymentStatus) {
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
        LinkedHashSet<String> onRoutePinCodes = new LinkedHashSet<>();
        String pickUpPinCode = searchDealsRequest.getPickUpLocation().getPinCode();
        String dropPinCode = searchDealsRequest.getDropLocation().getPinCode();
        if (pickUpPinCode != null) {
            //onRoutePinCodes.add(pickUpPinCode);
            // get subdistrict by pickUpPinCode from excel and then get all pincodes of that subdistrict and add it to onRoutePinCodes set
            findSurroundingPinCodes(onRoutePinCodes, pickUpPinCode);
        }
        findOnRouteVillages(onRoutePinCodes, searchDealsRequest.getPickUpLocation().getLatitude(), searchDealsRequest.getPickUpLocation().getLongitude(),
                searchDealsRequest.getDropLocation().getLatitude(), searchDealsRequest.getDropLocation().getLongitude());

        if (dropPinCode != null) {
            //onRoutePinCodes.add(dropPinCode);
            // get subdistrict by dropPinCode from excel and then get all pincodes of that subdistrict and add it to onRoutePinCodes set
            findSurroundingPinCodes(onRoutePinCodes, pickUpPinCode);
        }
        LOGGER.info("ON ROUTE PIN CODE SET FOR SEARCH DEALS : " + onRoutePinCodes);
        boolean isPickupPinCodeExist = onRoutePinCodes.contains(pickUpPinCode);
        boolean isDropPinCodeExist = onRoutePinCodes.contains(dropPinCode);
        if (isPickupPinCodeExist && isDropPinCodeExist) {
            return dealRepository.findByPickUpLocation_PinCodeInAndDropLocation_PinCodeInAndParcel_ParcelStatusInAndDealPaymentStatus(
                    onRoutePinCodes,
                    onRoutePinCodes,
                    List.of(ParcelStatus.PENDING_FOR_CARRIER_ACCEPTANCE, ParcelStatus.CANCELLED_BY_CARRIER),
                    DealPaymentStatus.PAID
            );
        }
        return Collections.emptyList();
    }

    public void findSurroundingPinCodes(LinkedHashSet<String> onRoutePinCodes, String pincode) {
        String filePath = Objects.requireNonNull(getClass().getResource("/SubdistrictWisePincodes.xls")).getPath();
        try (FileInputStream file = new FileInputStream(filePath)) {
            Workbook workbook = WorkbookFactory.create(file);
            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet
            for (Row row : sheet) {
                Cell pincodeCell = row.getCell(0);
                Cell subdistnameCell = row.getCell(1);
                if (pincodeCell != null && subdistnameCell != null) {
                    String currentPincode = String.valueOf((int) pincodeCell.getNumericCellValue());
                    String currentSubdistname = subdistnameCell.getStringCellValue();

                    if (pincode.equals(currentPincode)) {
                        onRoutePinCodes.add(currentPincode);
                        onRoutePinCodes.addAll(findPinCodesBySubdistName(currentSubdistname, sheet));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle any exceptions that may occur during file reading or processing
        }
    }

    private Set<String> findPinCodesBySubdistName(String subdistname, Sheet sheet) {
        Set<String> result = new LinkedHashSet<>();

        for (Row row : sheet) {
            Cell pincodeCell = row.getCell(0);
            Cell subdistnameCell = row.getCell(1);

            if (pincodeCell != null && subdistnameCell != null) {
                String currentPincode = String.valueOf((int) pincodeCell.getNumericCellValue());
                String currentSubdistname = subdistnameCell.getStringCellValue();

                if (subdistname.equals(currentSubdistname)) {
                    result.add(currentPincode);
                }
            }
        }

        return result;
    }

    public void findOnRouteVillages(LinkedHashSet<String> onRoutePinCodeSet, Double pickupLat, Double pickupLng, Double dropLat, Double dropLng) {
        CountDownLatch latch = new CountDownLatch(1);
        GeoApiContext context = new GeoApiContext.Builder().apiKey("AIzaSyBxVJBJjXRgTQIpOk3bf9sJ_zzfICJJ5jU").build();
        DirectionsApiRequest apiRequest = DirectionsApi.newRequest(context);
        apiRequest.origin(new LatLng(pickupLat, pickupLng));
        apiRequest.destination(new LatLng(dropLat, dropLng));
        apiRequest.transitMode(TransitMode.BUS);
        AtomicReference<Long> sumOfStepDistanceInMeters = new AtomicReference<>(0L);
        AtomicReference<Integer> noOfAPICalls = new AtomicReference<>(0);
        apiRequest.setCallback(new com.google.maps.PendingResult.Callback<DirectionsResult>() { // calling direction api to get on road waypoints (on raod lat, lng)
            @Override
            public void onResult(DirectionsResult result) {
                DirectionsRoute[] routes = result.routes;
                //Integer sumOfStepDistanceInMeters = 0;
                Arrays.stream(routes).forEach(directionsRoute -> Arrays.stream(directionsRoute.legs)
                        .forEach(directionsLeg -> Arrays.stream(directionsLeg.steps)
                                .filter(directionsStep -> directionsStep.endLocation != null)
                                .forEach(s -> {
                                    Double stepEndLat = s.endLocation.lat;
                                    Double stepEndLng = s.endLocation.lng;
                                    int km = 1000;
                                    String pinCode = "";
                                    // extract the step distance
                                    long stepDistanceInMeters = s.distance.inMeters;
                                    sumOfStepDistanceInMeters.updateAndGet(v -> v + stepDistanceInMeters);

                                    if (stepDistanceInMeters > 5 * km) { // if distance is > 20km
                                        int noOfSteps = (int) (stepDistanceInMeters / (5 * km)); // divide by 10km

                                        Double stepStartLat = s.startLocation.lat;
                                        Double stepStartLng = s.startLocation.lng;
                                        double latOffset = (stepEndLat - stepStartLat) / noOfSteps;
                                        double lngOffset = (stepEndLng - stepStartLng) / noOfSteps;
                                        double newStartLat = stepStartLat;
                                        double newStartLng = stepStartLng;
                                        pinCode = getPinCodeFromLatAndLng(newStartLat, newStartLng); // call reverse geocoding api to find pincode from lat, lng and add it into set
                                        if (pinCode != null) {
                                            onRoutePinCodeSet.add(pinCode);
                                        }
                                        for (int step = 0; step < noOfSteps; step++) {
                                            newStartLat += latOffset;
                                            newStartLng += lngOffset;
                                            noOfAPICalls.getAndSet(noOfAPICalls.get() + 1);
                                            pinCode = getPinCodeFromLatAndLng(newStartLat, newStartLng); // call reverse geocoding api to find pincode from lat, lng and add it into set
                                            if (pinCode != null) {
                                                onRoutePinCodeSet.add(pinCode);
                                            }
                                        }
                                    } else {
                                        if (sumOfStepDistanceInMeters.get() > 5 * km) {
                                            noOfAPICalls.getAndSet(noOfAPICalls.get() + 1);
                                            pinCode = getPinCodeFromLatAndLng(stepEndLat, stepEndLng); // call reverse geocoding api to find pincode from lat, lng and add it into set
                                            if (pinCode != null) {
                                                onRoutePinCodeSet.add(pinCode);
                                            }
                                            sumOfStepDistanceInMeters.set(0L);
                                        }
                                    }

                                })));
                latch.countDown();
                LOGGER.info("Number of on route pin codes found: " + onRoutePinCodeSet.size());
                LOGGER.info("Number of API calls hit: " + noOfAPICalls);
            }

            @Override
            public void onFailure(Throwable e) {
                LOGGER.info("FAILED IN CALLBACK WHILE FINDING ON ROUTE VILLAGE:" + e.toString());
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
            sendSMS(deal.getSender().getSenderContactNo(), SEND_ACCEPTANCE_SMS_TEXT, NOTIFY_SENDER_MSG_TEMPLATE_ID);
            String messageToCompany = "Parcel pickup request accepted by carrier " + updatedDeal.getCarrier().getCarrierFirstName() + " " + updatedDeal.getCarrier().getCarrierLastName() + ", CONTACT NO : " + updatedDeal.getCarrier().getCarrierContactNo() + ", DEAL ID : " + updatedDeal.getDealId() + ", ORDER ID : PMPTEMPORDER, AT TIME : " + updatedDeal.getUpdatedDate() + " jenesys";
            sendSMS("9960743366", messageToCompany, "1707168672180434057");
            return updatedDeal;
        } else {
            throw new RuntimeException("Deal does not exist with deal id : " + acceptDealRequest.getDealId());
        }
    }

    public static void sendSMS(String contactNumber, String msgContent, String templateId) {
        try {
            if (contactNumber == null) {
                throw new Exception("Contact number is null. Can't send message");
            }
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://pkmpcl.cwsdomians.in/submitsms.jsp?user=pkmpcl&key=6e0320dfa8XX&mobile=+91"
                    + contactNumber
                    + "&message=" + msgContent
                    + "&senderid=INFOIN&accusage=1&entityid=1201159178741416219&tempid=" + templateId;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        } catch (HttpClientErrorException ex) {
            LOGGER.info("HttpClientErrorException occurred while sending sms to sender" + ex.getMessage());
        } catch (Exception ex) {
            LOGGER.info("Exception occurred while sending sms to sender" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void sendNotificationToPickmyparcel_Internal(String msg) {
        String url = "https://api.telegram.org/bot6160762363:AAFUZyg9WSJLRdLmjWP93PnBa7aYYYbpm8g/sendMessage";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("chat_id", "-859279363");
        params.add("text", msg);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            LOGGER.info("Telegram Message sent successfully!");
        } else {
            LOGGER.warning("Failed to send telegram message. Status code: " + response.getStatusCodeValue());
        }
    }

    public static void Pickmyparcel_LoggedIn_users(String msg) {
        String url = "https://api.telegram.org/bot6160762363:AAFUZyg9WSJLRdLmjWP93PnBa7aYYYbpm8g/sendMessage";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("chat_id", "-801829313");
        params.add("text", msg);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            LOGGER.info("Telegram Message sent successfully!");
        } else {
            LOGGER.warning("Failed to send telegram message. Status code: " + response.getStatusCodeValue());
        }
    }


    @Override
    public DealResponse getSubmittedDealsByContactNumber(String contactNumber) {
        DealResponse dealResponse = new DealResponse();
        List<Deal> dealList = dealRepository.findBySenderSenderContactNoOrderByCreatedDateDesc(contactNumber);
        /*dealList = dealList.stream().sorted(Comparator.comparing(Deal::getCreatedDate).reversed())
                .collect(Collectors.toList());*/
        if (dealList.isEmpty()) {
            throw new EntityNotFoundException("Deal", "Submitted deals not found in database with this contact number");
        } else {
            List<Deal> mySubmittedDealList = dealList.stream().
                    filter(deal -> !DealPaymentStatus.FAILED.equals(deal.getDealPaymentStatus())).collect(Collectors.toList());
            dealResponse.setSelfCreatedDealList(mySubmittedDealList);
        }
        return dealResponse;
    }

    @Override
    public DealResponse getPickupDealsByContactNumber(String contactNumber) {
        DealResponse dealResponse = new DealResponse();
        List<Deal> dealList = dealRepository.findByCarrierCarrierContactNoOrderByCreatedDateDesc(contactNumber);
        /*dealList = dealList.stream().sorted(Comparator.comparing(Deal::getCreatedDate).reversed())
                .collect(Collectors.toList());*/
        if (dealList.isEmpty()) {
            throw new EntityNotFoundException("Deal", "Pickup deals not found in database with this contact number");
        } else {
            dealResponse.setPickUpDealList(dealList);
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
                    String msgContent = "Your parcel has been successfully delivered to " + deal.getReceiver().getReceiverFirstName() + " " + deal.getReceiver().getReceiverLastName() + ". Hope you enjoyed our service. We encourage you to send more parcels with us. Thanks for choosing pickmyparcel.in jenesys";
                    sendSMS(deal.getSender().getSenderContactNo(), msgContent, "1707168784770335327");
                    String telegramInternalMsgForDelivered = "Parcel with deal id : " + deal.getDealId() + " has been delivered successfully by carrier : " + deal.getCarrier().getCarrierFirstName() + " "
                            + deal.getCarrier().getCarrierLastName();
                    sendNotificationToPickmyparcel_Internal(telegramInternalMsgForDelivered);
                    walletServiceImpl.creditMoneyIntoCarrierWallet(deal);
                    break;
                case "CANCELLED_BY_SENDER":  // this is if deal has been cancelled due to any reason by SENDER
                    if (deal.getCarrier() != null && !StringUtils.isEmpty(deal.getCarrier().getCarrierContactNo()) && !StringUtils.isEmpty(deal.getCarrier().getCarrierFirstName())) {
                        String msgContentIfCancelledBySender = "Sender " + deal.getSender().getSenderFirstName() + " " + deal.getSender().getSenderLastName() + " has cancelled parcel. We request you to kindly search for another parcel on your route. pickmyparcel.in jenesys";
                        sendSMS(deal.getCarrier().getCarrierContactNo(), msgContentIfCancelledBySender, "1707168818949120783");
                    }
                    String telegramInternalMsgForCancelOrderBySender = "Parcel with deal id : " + deal.getDealId() + " has been cancelled by sender : " + deal.getSender().getSenderFirstName() + " "
                            + deal.getSender().getSenderLastName();
                    sendNotificationToPickmyparcel_Internal(telegramInternalMsgForCancelOrderBySender);
                    deal.getParcel().setParcelStatus(ParcelStatus.CANCELLED_BY_SENDER);
                    break;
                case "CANCELLED_BY_CARRIER":  // this is if deal has been cancelled due to any reason by CARRIER
                    String msgContentIfCancelledByCarrier = "Carrier " + deal.getCarrier().getCarrierFirstName() + " " + deal.getCarrier().getCarrierLastName() + " has cancelled your pickup order. Please wait for sometime, until we found another carrier for you. pickmyparcel.in jenesys";
                    sendSMS(deal.getSender().getSenderContactNo(), msgContentIfCancelledByCarrier, "1707168818953051964");
                    String telegramInternalMsgForCancelOrderByCarrier = "Parcel with deal id : " + deal.getDealId() + " has been cancelled by carrier : " + deal.getCarrier().getCarrierFirstName() + " "
                            + deal.getCarrier().getCarrierLastName();
                    sendNotificationToPickmyparcel_Internal(telegramInternalMsgForCancelOrderByCarrier);
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
        String msgContent = otp + " is your OTP to receive parcel from Carrier. Kindly share this OTP at the time of receiving parcel from carrier. Please do not disclose to anyone else. Thanks for choosing pickmyparcel. jenesys";
        this.sendSMS(deal.getReceiver().getReceiverContactNo(), msgContent, OTP_TEMPLATE_ID);
        receiverOtpRepository.save(receiverOtp);
        return true;
    }

    public static String generateOtp() {
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

    public Deal saveDealAndPaymentInfo(String merchantTransactionId, DealPaymentInfo dealPaymentInfo, DealPaymentStatus dealPaymentStatus) {
        dealPaymentInfo.setPaymentStatus(dealPaymentStatus.name());
        dealPaymentInfoRepository.save(dealPaymentInfo);

        Deal deal = this.getDealByDealId(dealPaymentInfo.getDealId());
        deal.setDealPaymentStatus(dealPaymentStatus);
        return dealRepository.save(deal);
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

    public DealPaymentInfo getDealPaymentInfoByDealId(Integer dealId) {
        return dealPaymentInfoRepository.findDealPaymentInfoByDealId(dealId);
    }

    public DealPaymentInfo getDealPaymentInfoByOrderId(String orderId) {
        Optional<DealPaymentInfo> byId = dealPaymentInfoRepository.findById(orderId);
        return byId.orElse(null);
    }
}
