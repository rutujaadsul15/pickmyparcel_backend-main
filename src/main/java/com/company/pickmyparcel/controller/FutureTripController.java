package com.company.pickmyparcel.controller;

import com.company.pickmyparcel.model.Deal;
import com.company.pickmyparcel.model.FutureTrip;
import com.company.pickmyparcel.model.User;
import com.company.pickmyparcel.model.requests.FutureTripRequest;
import com.company.pickmyparcel.model.requests.SearchDealsRequest;
import com.company.pickmyparcel.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/futureTrip")
@CrossOrigin(origins = "*")
public class FutureTripController {

    @Autowired
    private UserServiceImpl userServiceimpl;

    @PostMapping("/addTrip")
    public ResponseEntity<Boolean> addFutureTrips(@RequestBody FutureTripRequest futureTripRequest, @RequestParam("userContactNo") String userContactNo) {
        Boolean result = userServiceimpl.addFutureTrips(futureTripRequest, userContactNo);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/getFutureTrips")
    public ResponseEntity<List<FutureTrip>> getFutureTrips(@RequestParam("userContactNo") String userContactNo) {
        User user = userServiceimpl.getUserByContactNumber(userContactNo);
        List<FutureTrip> futureTripsList = user.getFutureTripsList();
        return ResponseEntity.ok(futureTripsList);
    }

    @DeleteMapping("/deleteFutureTrip")
    public ResponseEntity<Boolean> deleteFutureTrip(@RequestParam("futureTripId") Integer futureTripId, @RequestParam("userContactNo") String userContactNo) {
        Boolean result = userServiceimpl.deleteFutureTrip(futureTripId, userContactNo);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/getFutureTripByPinCodes")
    public ResponseEntity<List<FutureTrip>> getFutureTripByPinCodes(@RequestParam("pickupPinCode") String pickupPinCode, @RequestParam("dropPinCode") String dropPinCode) {
        List<FutureTrip> trips = userServiceimpl.getFutureTripByPinCodes(pickupPinCode, dropPinCode);
        if (trips.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(trips);
    }

    @GetMapping("/getFutureTripById")
    public ResponseEntity<FutureTrip> getFutureTripById(@RequestParam("futureTripId") Integer futureTripId ) {

        FutureTrip trip = userServiceimpl.getFutureTripById(futureTripId);

        return ResponseEntity.ok(trip);
    }

  /*  @PostMapping("/searchFutureDeals")
    public ResponseEntity<List<Deal>> searchFutureDeals(@RequestBody @Valid FutureTripRequest futureTripRequest) {
        List<Deal> deals = userServiceimpl.searchFutureDeals(futureTripRequest);
        return ResponseEntity.ok(deals);
    }*/
}
