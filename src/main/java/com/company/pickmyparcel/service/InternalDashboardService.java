package com.company.pickmyparcel.service;

import com.company.pickmyparcel.model.Deal;
import com.company.pickmyparcel.model.DealResponse;
import com.company.pickmyparcel.model.response.DealAndPaymentResponse;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

public interface InternalDashboardService {
    DealAndPaymentResponse getDealsAndPaymentInfoByContactNumber(String contactNumber);
    ResponseEntity<List<DealAndPaymentResponse>> getDealsByDate(String date);
}
