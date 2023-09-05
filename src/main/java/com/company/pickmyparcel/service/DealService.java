package com.company.pickmyparcel.service;

import com.company.pickmyparcel.model.Deal;
import com.company.pickmyparcel.model.DealPaymentInfo;
import com.company.pickmyparcel.model.DealPaymentStatus;
import com.company.pickmyparcel.model.DealResponse;
import com.company.pickmyparcel.model.requests.RazorPayPaymentOrderDetails;
import com.company.pickmyparcel.model.requests.SearchDealsRequest;

import java.util.List;

public interface DealService {
    Deal saveDeal(Deal deal);
    Deal saveDealViaPaytm(Deal deal, DealPaymentStatus dealPaymentStatus);
    Deal saveDealViaPhonePe(Deal deal, DealPaymentStatus dealPaymentStatus);
    Deal calculateDealDistanceAndDealTotal(Deal deal);
    Deal getDealByDealId(Integer id);
    List<Deal> searchDeals(SearchDealsRequest searchDealsRequest);
    Deal requestSenderForConfirmation(Deal acceptDealRequest);
    DealResponse getSubmittedDealsByContactNumber(String contactNumber);
    DealResponse getPickupDealsByContactNumber(String contactNumber);
    Deal updateParcelStatus(Integer dealId, String parcelStatus);
    Boolean verifyReceiverOTP(Integer dealId, String otp);
    Boolean updateDealPaymentStatus(RazorPayPaymentOrderDetails razorPayPaymentOrderDetails);
    DealPaymentInfo getDealPaymentInfoByDealId(Integer dealId);
}
