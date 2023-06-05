package com.company.pickmyparcel.service;


import com.company.pickmyparcel.model.Deal;
import com.company.pickmyparcel.model.FutureTrip;
import com.company.pickmyparcel.model.User;
import com.company.pickmyparcel.model.requests.FutureTripRequest;
import com.company.pickmyparcel.model.requests.SearchDealsRequest;

import java.util.List;

public interface UserService {

    User registerUser(User user);

    User getUserByContactNumber(String contactNo);

    Boolean addFutureTrips(FutureTripRequest futureTripRequest, String userContactNo);
    Boolean deleteFutureTrip(Integer futureTripId, String userContactNo);

    List<FutureTrip> getFutureTripByPinCodes(String pickupPinCode, String dropPinCode);

    FutureTrip getFutureTripById(Integer futureTripId);

   /* List<Deal> searchFutureDeals(FutureTripRequest futureTripRequest);*/
}
