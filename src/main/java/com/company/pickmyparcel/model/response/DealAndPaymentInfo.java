package com.company.pickmyparcel.model.response;

import com.company.pickmyparcel.model.Deal;
import com.company.pickmyparcel.model.DealPaymentInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DealAndPaymentInfo {

    private Deal deal;
    private DealPaymentInfo dealPaymentInfo;


}
