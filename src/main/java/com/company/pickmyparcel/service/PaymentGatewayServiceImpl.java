package com.company.pickmyparcel.service;

import com.cashfree.lib.constants.Constants;
import com.cashfree.lib.pg.clients.Order;
import com.cashfree.lib.pg.clients.Pg;
import com.cashfree.lib.pg.domains.request.CreateOrderRequest;
import com.cashfree.lib.pg.domains.response.CreateOrderResponse;
import com.company.pickmyparcel.model.requests.OrderDetails;
import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;

@Service
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

    @Override
    public CreateOrderResponse createOrder(OrderDetails orderDetails) {
        /*CreateOrderRequest cfOrderRequest = new CreateOrderRequest();
        if (orderDetails.getOrderId()==null){
            cfOrderRequest.setOrderId(UUID.randomUUID().toString());
        }else {
            cfOrderRequest.setOrderId(orderDetails.getOrderId());
        }
       *//* if (orderDetails.getOrderNote()==null){
            cfOrderRequest.setOrderNote("This is order note for : " + cfOrderRequest.getOrderId());
        }else {
            cfOrderRequest.setOrderNote(orderDetails.getOrderNote());
        }*//*
        if (orderDetails.getCustomerEmail()==null){
            cfOrderRequest.setCustomerEmail("testemail@gmail.com");
        }else {
            cfOrderRequest.setCustomerEmail(orderDetails.getCustomerEmail());
        }
        if (orderDetails.getCustomerName()==null){
            cfOrderRequest.setCustomerName("DUMMY CUSTOMER NAME");
        }else {
            cfOrderRequest.setCustomerName(orderDetails.getCustomerName());
        }
        cfOrderRequest.setOrderAmount(orderDetails.getOrderAmount());
        cfOrderRequest.setOrderCurrency("INR");
        cfOrderRequest.setCustomerPhone(orderDetails.getCustomerPhone());
        cfOrderRequest.setReturnUrl(orderDetails.getReturnUrl());
        Pg pg = Pg.getInstance(Constants.Environment.TEST, "32162621dcb0e2b7dce6aa5d90626123", "995a24ce7839c407c0adfac2970180e6fef12069");
        Order order = new Order(pg);
        return order.createOrder(cfOrderRequest);*/

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("appId", "32162621dcb0e2b7dce6aa5d90626123")
                .addFormDataPart("secretKey", "995a24ce7839c407c0adfac2970180e6fef12069")
                .addFormDataPart("orderId", "GENERATED_ID6")
                .addFormDataPart("orderAmount", "200")
                .addFormDataPart("orderCurrency", "INR")
                .addFormDataPart("customerEmail", "testemail@gmail.com")
                .addFormDataPart("customerName", "VISHAL")
                .addFormDataPart("customerPhone", "9960743366")
                .addFormDataPart("returnUrl", "http://localhost:8008/payment/handlePaymentResponse")
                .addFormDataPart("notifyUrl", "http://localhost:4200/")
                .addFormDataPart("orderNote", "").build();

        Request request = new Request.Builder()
                .url("https://test.cashfree.com/api/v1/order/create")
                .method("POST", body).build();

        try {
            Response response = client.newCall(request).execute();
            System.out.println(response.body().string());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public String getPaymentLink(String orderId) {
        String stage = "TEST"; // or "PROD" for production environment
        return "https://" + stage + ".cashfree.com/payment/#/pay/" + orderId;
    }

    private final String YOUR_SALT_KEY = "your_salt_key_here";
    private final String YOUR_SALT_INDEX = "your_salt_index_here";

    public boolean verifyCallback(String verificationHeader, String base64EncodedPayload) {
        String calculatedSignature = calculateSignature(base64EncodedPayload);
        String providedSignature = verificationHeader.split("###")[0];
        return calculatedSignature.equals(providedSignature);
    }

    private String calculateSignature(String payload) {
        try {
            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(YOUR_SALT_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256HMAC.init(secretKey);

            byte[] signatureBytes = sha256HMAC.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(signatureBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
