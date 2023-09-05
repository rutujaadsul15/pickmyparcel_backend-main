package com.company.pickmyparcel.service;

import com.cashfree.lib.pg.domains.response.CreateOrderResponse;
import com.company.pickmyparcel.model.requests.OrderDetails;

public interface PaymentGatewayService {
    CreateOrderResponse createOrder(OrderDetails orderDetails);
    String getPaymentLink(String orderId);
    boolean verifyCallback(String verificationHeader, String base64EncodedPayload);
    /*boolean verifySignature(String responseSignature, String responseData);*/
}
