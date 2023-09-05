package com.company.pickmyparcel.service;


import com.company.pickmyparcel.model.FutureTrip;
import com.company.pickmyparcel.model.User;
import com.company.pickmyparcel.model.requests.FutureTripRequest;
import com.company.pickmyparcel.model.requests.PasswordResetOTPRequest;
import com.company.pickmyparcel.model.requests.PasswordResetRequest;

import java.util.List;

public interface UserService {

    boolean sendRegisterOtp(String mobileNo);

    boolean sendPasswordResetOTP(String mobileNo);

    boolean verifyPasswordResetOTP(PasswordResetOTPRequest passwordResetOTPRequest);

    boolean submitNewPassword(PasswordResetRequest passwordResetRequest);

    User registerUser(String registerOtp, User user);

    User getUserByContactNumber(String contactNo);

    Boolean addFutureTrips(FutureTripRequest futureTripRequest, String userContactNo);

    Boolean deleteFutureTrip(Integer futureTripId, String userContactNo);

    List<FutureTrip> getFutureTripByPinCodes(String pickupPinCode, String dropPinCode);

    FutureTrip getFutureTripById(Integer futureTripId);
}
