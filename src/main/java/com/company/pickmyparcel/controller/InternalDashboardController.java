package com.company.pickmyparcel.controller;

import com.company.pickmyparcel.model.Deal;
import com.company.pickmyparcel.model.response.DealAndPaymentResponse;
import com.company.pickmyparcel.repository.DealRepository;
import com.company.pickmyparcel.service.InternalDashboardServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/internalDashboard")
@CrossOrigin(origins = "*")
public class InternalDashboardController {

    @Autowired
    private InternalDashboardServiceImpl internalDashboardServiceimpl;

    public DealRepository dealRepository;


    @GetMapping("/getDealsAndPaymentInfoByContactNumber")
    //todo : use preAuthorise Annotation. to authenticate roles
    public ResponseEntity<DealAndPaymentResponse> getDealsAndPaymentInfoByContactNumber(@RequestParam("contactNumber") String contactNumber) {
        DealAndPaymentResponse getDealsAndPaymentInfoByContactNumber = internalDashboardServiceimpl.getDealsAndPaymentInfoByContactNumber(contactNumber);
        return ResponseEntity.ok(getDealsAndPaymentInfoByContactNumber);
    }

    @GetMapping("/byDate")
    public ResponseEntity<List<DealAndPaymentResponse>> getDealsByDate(@RequestParam("date") String date) {
        return internalDashboardServiceimpl.getDealsByDate(date);
    }
}
