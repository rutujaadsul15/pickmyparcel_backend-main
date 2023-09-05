package com.company.pickmyparcel.model.requests;

import com.cashfree.lib.annotations.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetails {
    @NotNull
    private String orderId;

    @NotNull
    private BigDecimal orderAmount;

    private String orderNote;

    private String customerName;

    @NotNull
    private String customerEmail;

    @NotNull
    private String customerPhone;

    @NotNull
    private String returnUrl;
}
