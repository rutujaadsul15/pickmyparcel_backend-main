package com.company.pickmyparcel.service;

import com.company.pickmyparcel.model.ContactUs;

public interface ContactUsService {
    ContactUs insertEnquiryMessageFromUser(String mobileNo, String message);
}
