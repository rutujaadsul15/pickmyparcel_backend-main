package com.company.pickmyparcel.service;

import com.company.pickmyparcel.model.ContactUs;
import com.company.pickmyparcel.repository.ContactUsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class ContactUsServiceImpl implements ContactUsService {
    private static final Logger LOGGER = Logger.getLogger(ContactUsServiceImpl.class.getName());

    @Autowired
    private ContactUsRepository contactUsRepository;

    @Autowired
    private DealServiceImpl dealServiceImpl;


    @Override
    public ContactUs insertEnquiryMessageFromUser(String mobileNo, String message) {
        ContactUs contactUs = new ContactUs();
        contactUs.setMobileNo(mobileNo);
        contactUs.setMessage(message);
        String telegramMsgForEnquiry = "Enquiry received from user with mobile number : " + mobileNo + " and message : " + message;
        DealServiceImpl.sendNotificationToPickmyparcel_Internal(telegramMsgForEnquiry);
        return contactUsRepository.save(contactUs);
    }
}
