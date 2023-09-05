package com.company.pickmyparcel.controller;

import com.company.pickmyparcel.service.InvoiceService;
import com.company.pickmyparcel.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.logging.Logger;

@RestController
@RequestMapping("/invoice")
@CrossOrigin(origins = "*")
public class InvoiceController {
    private static final Logger LOGGER = Logger.getLogger(InvoiceController.class.getName());

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping("/generateInvoice")
    public ResponseEntity<InputStreamResource> generateInvoice(@RequestParam("dealId") Integer dealId) {
        LOGGER.info("Generating invoice for deal id : " + dealId);
        ByteArrayInputStream pdf = invoiceService.generateInvoice(dealId);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Disposition", "inline;file=lcdw.pdf");
        return ResponseEntity.ok()
                .headers(httpHeaders)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdf));
    }
}

