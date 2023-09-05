package com.company.pickmyparcel.service;

import com.company.pickmyparcel.model.Deal;
import com.company.pickmyparcel.model.DealPaymentStatus;
import com.company.pickmyparcel.model.DealResponse;
import com.company.pickmyparcel.model.Wallet;
import com.company.pickmyparcel.model.requests.RazorPayPaymentOrderDetails;
import com.company.pickmyparcel.model.requests.SearchDealsRequest;

import java.util.List;

public interface WalletService {
    Wallet fetchWalletDetails(String userContactNumber);
    void creditMoneyIntoCarrierWallet(Deal deal);
}
