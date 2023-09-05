package com.company.pickmyparcel.service;

import com.company.pickmyparcel.model.Deal;
import com.company.pickmyparcel.model.DealPaymentStatus;
import com.company.pickmyparcel.model.FutureTrip;
import com.company.pickmyparcel.model.ParcelStatus;
import com.company.pickmyparcel.repository.DealRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private DealRepository dealRepository;

    @Async
    public void sendNotificationAsync(FutureTrip futureTrip) {

        List<Deal> deals = dealRepository.findByPickUpLocation_PinCodeAndDropLocation_PinCode(
                futureTrip.getPickupPinCode(),
                futureTrip.getDropPinCode());

        Set<String> senderContactNos = deals.stream()
                .map(deal -> deal.getSender().getSenderContactNo())
                .collect(Collectors.toSet());

        System.out.println("SHORTLISTED SENDERS : " + senderContactNos);

        // Logic to send WhatsApp notification
        // Use the messaging service or API of your choice
    }
}