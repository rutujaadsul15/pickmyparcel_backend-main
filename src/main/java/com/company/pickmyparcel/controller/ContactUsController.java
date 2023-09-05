package com.company.pickmyparcel.controller;

import com.company.pickmyparcel.model.ContactUs;
import com.company.pickmyparcel.model.UserRole;
import com.company.pickmyparcel.service.ContactUsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contactUs")
@CrossOrigin(origins = "*")
public class ContactUsController {

    @Autowired
    private ContactUsServiceImpl contactUsServiceImpl;


    @GetMapping("/sendEnquiry")
    public ResponseEntity<ContactUs> sendEnquiry(@RequestParam("mobileNo") String mobileNo, @RequestParam("message") String message) {
        ContactUs contactUs = contactUsServiceImpl.insertEnquiryMessageFromUser(mobileNo, message);
        if (contactUs != null) {
            return ResponseEntity.ok(contactUs);
        } else {
            return ResponseEntity.internalServerError().body(contactUs);
        }
    }
}
