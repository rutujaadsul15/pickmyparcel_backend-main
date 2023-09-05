package com.company.pickmyparcel.controller;

import com.company.pickmyparcel.model.Deal;
import com.company.pickmyparcel.model.OrderResponse;
import com.company.pickmyparcel.service.DealServiceImpl;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/pg")
public class RazorPayController {

    @Autowired
    private DealServiceImpl dealService;
    @PostMapping("/createOrder")
    @ResponseBody
    public ResponseEntity<OrderResponse> initiateOrder(@RequestBody Deal deal) {
        String orderId = null;
        OrderResponse orderResponse = new OrderResponse();
        try {
            RazorpayClient razorpay = new RazorpayClient("rzp_test_l7uqE41oThaWg9", "bdGmKft1G42tIJ2urmXssSS1");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("amount", deal.getDealTotal());
            jsonObject.put("currency", "INR");
            jsonObject.put("receipt", UUID.randomUUID().toString());

            Order order = razorpay.orders.create(jsonObject);
			orderResponse.setSecretId("rzp_test_l7uqE41oThaWg9");
            orderResponse.setSecretKey("bdGmKft1G42tIJ2urmXssSS1");
            orderResponse.setPgName("razor2");
            orderResponse.setRazorpayOrderId(order.get("id"));
            orderResponse.setApplicationFee(String.valueOf(deal.getDealTotal()*100)); // convert to paise

            System.out.println("ORDER ID : " + order.get("id"));
            Deal savedDeal = dealService.saveDeal(deal);
            dealService.addDealPaymentInfo(savedDeal, order);
            orderResponse.setDealId(savedDeal.getDealId());
		} catch (RazorpayException e) {
            // Handle Exception
            System.out.println(e.getMessage());
        }
        return ResponseEntity.ok(orderResponse);
    }
}
