package com.company.pickmyparcel.service;

import com.company.pickmyparcel.exception.EntityNotFoundException;
import com.company.pickmyparcel.model.*;
import com.company.pickmyparcel.model.response.DealAndPaymentInfo;
import com.company.pickmyparcel.model.response.DealAndPaymentResponse;
import com.company.pickmyparcel.repository.DealPaymentInfoRepository;
import com.company.pickmyparcel.repository.DealRepository;
import com.company.pickmyparcel.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service

public class InternalDashboardServiceImpl implements InternalDashboardService {

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private DealPaymentInfoRepository dealPaymentInfoRepository;
    @Override
    public DealAndPaymentResponse getDealsAndPaymentInfoByContactNumber(String contactNumber) {
         List<DealAndPaymentInfo> dealAndPaymentInfoList = new ArrayList<>();
         DealAndPaymentResponse dealAndPaymentResponse = new DealAndPaymentResponse();

            List<Deal> dealList = dealRepository.findBySenderSenderContactNoOrderByCreatedDateDesc(contactNumber);
        if (dealList.isEmpty()) {
            throw new EntityNotFoundException("Deal", "Submitted deals not found in database with this contact number");
        } else {
            List<Deal> updatedDealList = dealList.stream().
                    filter(deal -> DealPaymentStatus.PAID.equals(deal.getDealPaymentStatus())).collect(Collectors.toList());

            updatedDealList.forEach(deal -> {
                DealAndPaymentInfo dealAndPaymentInfo = new DealAndPaymentInfo();  //made obj
                dealAndPaymentInfo.setDeal(deal);                                  //set database deal into deal obj
                DealPaymentInfo paymentInfo =  dealPaymentInfoRepository.findDealPaymentInfoByDealId(deal.getDealId());  //find payment info from db by dealid
                dealAndPaymentInfo.setDealPaymentInfo(paymentInfo);   //set database payment info into paymentinfo obj of Dealpaymentinfo
                dealAndPaymentInfoList.add(dealAndPaymentInfo);

            });

            Optional<Wallet> wallet = walletRepository.findById(contactNumber);
            if(wallet.isPresent()) {
                dealAndPaymentResponse.setWallet(wallet.get());
            }
            dealAndPaymentResponse.setDealAndPaymentInfoList(dealAndPaymentInfoList);
        }

        return dealAndPaymentResponse;

    }




    public ResponseEntity<List<DealAndPaymentResponse>> getDealsByDate(String date) {
        try {
            String formattedDate = convertToDatabaseFormat(date);

            List<DealAndPaymentResponse> deals = dealRepository.findDealsByDate(formattedDate);
            return ResponseEntity.ok(deals);

        } catch (ParseException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    private String convertToDatabaseFormat(String inputDateStr) throws ParseException {
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = inputDateFormat.parse(inputDateStr);
        return outputDateFormat.format(date);
    }

    }


