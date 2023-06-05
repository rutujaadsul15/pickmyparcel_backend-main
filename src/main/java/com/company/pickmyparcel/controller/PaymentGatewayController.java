package com.company.pickmyparcel.controller;

import com.cashfree.lib.pg.domains.response.CreateOrderResponse;
import com.company.pickmyparcel.model.requests.OrderDetails;
import com.company.pickmyparcel.service.PaymentGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/payment")
@CrossOrigin(origins = "*")
public class PaymentGatewayController {

    @Autowired
    private PaymentGatewayService paymentGatewayService;

    @PostMapping("/initiatePayment")
    public CreateOrderResponse initiatePayment(@RequestBody OrderDetails orderDetails) {
        return paymentGatewayService.createOrder(orderDetails);
    }

    @PostMapping("/handlePaymentResponse")
    public String handlePaymentResponse(@RequestBody String responseData, @RequestHeader("X-CF-Signature") String responseSignature) {
        System.out.println(responseData);
        System.out.println(responseSignature);
        return "tested";
    }
}