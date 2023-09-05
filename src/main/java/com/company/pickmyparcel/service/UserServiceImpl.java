package com.company.pickmyparcel.service;


import com.company.pickmyparcel.model.*;
import com.company.pickmyparcel.model.requests.FutureTripRequest;
import com.company.pickmyparcel.model.requests.PasswordResetOTPRequest;
import com.company.pickmyparcel.model.requests.PasswordResetRequest;
import com.company.pickmyparcel.repository.FutureTripRepository;
import com.company.pickmyparcel.repository.RegisterOtpRepository;
import com.company.pickmyparcel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger LOGGER = Logger.getLogger(UserServiceImpl.class.getName());
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FutureTripRepository futureTripRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private DealServiceImpl dealService;

    @Autowired
    private RegisterOtpRepository registerOtpRepository;

    @Autowired
    private NotificationService notificationService;

    public boolean sendRegisterOtp(String mobileNo) {
        Optional<User> dbUser = userRepository.findById(mobileNo);
        if (dbUser.isPresent()) {
            throw new RuntimeException("User already registered with this contact number. Please try login");
        }
        if (!StringUtils.isEmpty(mobileNo)) {
            String generatedOtp = DealServiceImpl.generateOtp();
            String msgForRegistrationOtp = generatedOtp + " is your OTP for pickmyparcel registration. Please do not disclose to anyone else. Thanks for choosing pickmyparcel.in jenesys";
            RegisterOtp existingRecord = registerOtpRepository.findByUserMobileNo(mobileNo);
            if (existingRecord != null) {
                existingRecord.setOtp(generatedOtp);
                registerOtpRepository.save(existingRecord);
            } else {
                RegisterOtp registerOtp = new RegisterOtp();
                registerOtp.setUserMobileNo(mobileNo);
                registerOtp.setOtp(generatedOtp);
                registerOtpRepository.save(registerOtp);
            }

            DealServiceImpl.sendSMS(mobileNo, msgForRegistrationOtp, "1707168837342818133");
            return true;
        }
        return false;
    }


    public boolean sendPasswordResetOTP(String mobileNo) {
        Optional<User> dbUser = userRepository.findById(mobileNo);
        if (dbUser.isPresent()) {
            String generatedOtp = DealServiceImpl.generateOtp();
            String msgForRegistrationOtp = generatedOtp + " is your password reset OTP for pickmyparcel. Please do not disclose to anyone else. Thanks for choosing pickmyparcel.in jenesys";
            dbUser.get().setPasswordResetOtp(generatedOtp);
            userRepository.save(dbUser.get());
            DealServiceImpl.sendSMS(mobileNo, msgForRegistrationOtp, "1707169122574081902");
            return true;
        } else {
            throw new RuntimeException("Account does not exist with this mobile number. Please enter correct mobile no.");
        }
    }

    public boolean verifyPasswordResetOTP(PasswordResetOTPRequest passwordResetOTPRequest) {
        Optional<User> dbUser = userRepository.findById(passwordResetOTPRequest.getMobileNo());
        if (dbUser.isPresent() && passwordResetOTPRequest.getMobileNo() != null && passwordResetOTPRequest.getPasswordResetOtp() != null) {
            return dbUser.get().getPasswordResetOtp().equals(passwordResetOTPRequest.getPasswordResetOtp());
        } else {
            throw new RuntimeException("Account does not exist with this mobile number. Please enter correct mobile no.");
        }
    }

    public boolean submitNewPassword(PasswordResetRequest passwordResetRequest) {
        Optional<User> dbUser = userRepository.findById(passwordResetRequest.getMobileNo());
        if (dbUser.isPresent() && passwordResetRequest.getNewPassword() != null) {
            dbUser.get().setPassword(passwordEncoder.encode(passwordResetRequest.getNewPassword()));
            userRepository.save(dbUser.get());
            return true;
        } else {
            throw new RuntimeException("Account does not exist with this mobile number. Please enter correct mobile no.");
        }
    }

    @Override
    public User registerUser(String registerOtp, User user) {
        RegisterOtp dbOtp = registerOtpRepository.findByUserMobileNo(user.getContactNo());
        if (!registerOtp.equals(dbOtp.getOtp())) {
            throw new RuntimeException("OTP is incorrect. Please enter correct OTP");
        }
        Optional<User> dbUser = userRepository.findById(user.getContactNo());
        if (dbUser.isPresent()) {
            throw new RuntimeException("User already registered with this contact number");
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(UserRole.USER);
            //create wallet with 0 balance and link it to user
            Wallet wallet = new Wallet();
            wallet.setUserContactNumber(user.getContactNo());
            wallet.setWalletBalance(0.0);
            user.setWallet(wallet);
            DealServiceImpl.sendNotificationToPickmyparcel_Internal("New user has been registered with pickmyparcel! Name : " + user.getFirstName() + " "
                    + user.getLastName() + " Contact No : " + user.getContactNo());
            return userRepository.save(user);
        }
    }

    @Override
    public User getUserByContactNumber(String contactNo) {
        Optional<User> dbUser = userRepository.findById(contactNo);
        if (dbUser.isPresent()) {
            return dbUser.get();
        } else {
            throw new RuntimeException("User does not exist with mobile number : " + contactNo + ". Please register your mobile number & then try again.");
        }
    }

    @Override
    public Boolean addFutureTrips(FutureTripRequest futureTripRequest, String userContactNo) {
        FutureTrip futureTrip = new FutureTrip();

        futureTrip.setPickupPlaceId(futureTripRequest.getPickUpLocation().getPlaceId());
        futureTrip.setPickupAddressLine(futureTripRequest.getPickUpLocation().getAddressLine());
        futureTrip.setPickupLatitude(futureTripRequest.getPickUpLocation().getLatitude());
        futureTrip.setPickupLongitude(futureTripRequest.getPickUpLocation().getLongitude());
        futureTrip.setPickupCity(futureTripRequest.getPickUpLocation().getCity());
        futureTrip.setPickupState(futureTripRequest.getPickUpLocation().getState());
        futureTrip.setPickupPinCode(futureTripRequest.getPickUpLocation().getPinCode());
        //set drop location details
        futureTrip.setDropPlaceId(futureTripRequest.getDropLocation().getPlaceId());
        futureTrip.setDropAddressLine(futureTripRequest.getDropLocation().getAddressLine());
        futureTrip.setDropLatitude(futureTripRequest.getDropLocation().getLatitude());
        futureTrip.setDropLongitude(futureTripRequest.getDropLocation().getLongitude());
        futureTrip.setDropCity(futureTripRequest.getDropLocation().getCity());
        futureTrip.setDropState(futureTripRequest.getDropLocation().getState());
        futureTrip.setDropPinCode(futureTripRequest.getDropLocation().getPinCode());
        futureTrip.setDateAndTime(futureTripRequest.getDateAndTime());
        futureTrip.setCarrierContactNo(futureTripRequest.getCarrierContactNo());
        futureTrip.setCarrierFirstName(futureTripRequest.getCarrierFirstName());
        futureTrip.setCarrierLastName(futureTripRequest.getCarrierLastName());
        LinkedHashSet<String> onRoutePinCodes = new LinkedHashSet<>();
        if (futureTripRequest.getPickUpLocation().getPinCode() != null) {
            //onRoutePinCodes.add(futureTripRequest.getPickUpLocation().getPinCode());
            dealService.findSurroundingPinCodes(onRoutePinCodes, futureTripRequest.getPickUpLocation().getPinCode());
        }
        dealService.findOnRouteVillages(onRoutePinCodes, futureTripRequest.getPickUpLocation().getLatitude(), futureTripRequest.getPickUpLocation().getLongitude(),
                futureTripRequest.getDropLocation().getLatitude(), futureTripRequest.getDropLocation().getLongitude());

        if (futureTripRequest.getDropLocation().getPinCode() != null) {
            //onRoutePinCodes.add(futureTripRequest.getDropLocation().getPinCode());
            dealService.findSurroundingPinCodes(onRoutePinCodes, futureTripRequest.getDropLocation().getPinCode());
        }
        futureTrip.setOnRoutePinCodes(String.join(",", onRoutePinCodes));
        LOGGER.info("ON ROUTE PIN CODE SET WHILE ADDING FUTURE TRIP : " + onRoutePinCodes);
        User user = getUserByContactNumber(userContactNo);
        if (user != null) {
            futureTrip.setCarrierFirstName(user.getFirstName());
            futureTrip.setCarrierLastName(user.getLastName());
            futureTrip.setCarrierContactNo(user.getContactNo());
            List<FutureTrip> futureTripsList = user.getFutureTripsList();
            futureTripsList.add(futureTrip);
            user.setFutureTripsList(futureTripsList);
            userRepository.save(user);
            String messageToCompany = "Future trip has been added by user : " + user.getFirstName() + " " + user.getLastName()
                    + ", Contact No : " + user.getContactNo() + ", Pickup Location : " + futureTripRequest.getPickUpLocation().getAddressLine() + ", " + futureTripRequest.getPickUpLocation().getCity()
                    + ", Drop Location : " + futureTripRequest.getDropLocation().getAddressLine() + ", " + futureTripRequest.getDropLocation().getCity() + ", Scheduled at time : " + futureTrip.getDateAndTime().toString();
            DealServiceImpl.sendNotificationToPickmyparcel_Internal(messageToCompany);
            // Logic to add future trip to the database

            // Trigger asynchronous notification
            notificationService.sendNotificationAsync(futureTrip);
            System.out.println("After sendNotificationAsync");
            return true;
        }
        return false;
    }

    @Override
    public Boolean deleteFutureTrip(Integer futureTripId, String userContactNo) {
        Optional<User> dbUser = userRepository.findById(userContactNo);
        if (dbUser.isPresent()) {
            User user = dbUser.get();
            List<FutureTrip> futureTripsList = user.getFutureTripsList();
            if (futureTripsList != null && futureTripsList.size() > 0) {
                futureTripsList.removeIf(trip -> Objects.equals(trip.getFutureTripId(), futureTripId));
                user.setFutureTripsList(futureTripsList);
                userRepository.save(user);
                return true;
            } else {
                return false;
            }
        } else {
            throw new RuntimeException("User does not exist with contact number : " + userContactNo);
        }
    }

    @Override
    public List<FutureTrip> getFutureTripByPinCodes(String pickupPinCode, String dropPinCode) {
        List<FutureTrip> responseList = new ArrayList<>();
        List<FutureTrip> trips = futureTripRepository.findByOnRoutePinCodesContainingAndOnRoutePinCodesContaining(pickupPinCode, dropPinCode);
        Date currentDate = Date.from(Instant.now());
        trips.forEach(trip -> {
            Date targetDate = trip.getDateAndTime();
            boolean isBefore = currentDate.before(targetDate);
            if (isBefore) {
                responseList.add(trip);
            }
        });
        return responseList;
    }

    @Override
    public FutureTrip getFutureTripById(Integer futureTripId) {
        Optional<FutureTrip> futureTripRepositoryById = futureTripRepository.findById(futureTripId);
        if (futureTripRepositoryById.isEmpty()) {
            System.out.println("No future trip exist with given id");
        } else {
            System.out.println("Future trip is :  ");
        }
        return futureTripRepositoryById.get();
    }


/*    public List<Deal> searchFutureDeals(FutureTripRequest futureTripRequest) {
        List<Deal> dealList = dealRepository.findAll();
        LinkedHashSet<String> onRoutePinCodes = findOnRouteVillages(futureTripRequest);
        boolean isPickupPinCodeExist = onRoutePinCodes.contains(futureTripRequest.getPickUpLocation().getPinCode());
        boolean isDropPinCodeExist = onRoutePinCodes.contains(futureTripRequest.getDropLocation().getPinCode());
        if (isPickupPinCodeExist && isDropPinCodeExist) {
            return dealList.stream()
                    .filter(deal -> deal.getPickUpLocation().getPinCode().equals(futureTripRequest.getPickUpLocation().getPinCode()))
                    .filter(deal -> deal.getDropLocation().getPinCode().equals(futureTripRequest.getDropLocation().getPinCode()))
                    .filter(deal -> deal.getParcel().getParcelStatus().equals(ParcelStatus.PENDING_FOR_CARRIER_ACCEPTANCE))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }*/
}
