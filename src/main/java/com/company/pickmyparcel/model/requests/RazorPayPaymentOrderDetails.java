package com.company.pickmyparcel.model.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RazorPayPaymentOrderDetails {
    @NonNull
    private String razorpay_payment_id;
    private Integer dealId;
    private String razorpay_order_id;
    @NonNull
    private String paymentStatus;

}
