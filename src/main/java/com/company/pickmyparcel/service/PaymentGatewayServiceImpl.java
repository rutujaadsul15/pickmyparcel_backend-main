package com.company.pickmyparcel.service;

import com.cashfree.lib.constants.Constants;
import com.cashfree.lib.pg.clients.Order;
import com.cashfree.lib.pg.clients.Pg;
import com.cashfree.lib.pg.domains.request.CreateOrderRequest;
import com.cashfree.lib.pg.domains.response.CreateOrderResponse;
import com.company.pickmyparcel.model.requests.OrderDetails;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

   /* @Override
    public boolean verifySignature(String responseSignature, String responseData) {
        CfClient cfClient = new CfClient();
        cfClient.appId = appId;
        cfClient.secretKey = secretKey;
        CfSignatureService cfSignatureService = new CfSignatureServiceImpl(cfClient);
        return cfSignatureService.verifySignature(responseData, responseSignature);
    }*/
}
