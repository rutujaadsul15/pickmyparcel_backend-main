package com.company.pickmyparcel.model.response;

import com.company.pickmyparcel.model.Wallet;
import lombok.*;

import java.util.List;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DealAndPaymentResponse {
    private List<DealAndPaymentInfo> dealAndPaymentInfoList;
    private Wallet wallet;

    public List<DealAndPaymentInfo> getDealAndPaymentInfoList() {
        return dealAndPaymentInfoList;
    }

    public Wallet getWallet() {
        return wallet;
    }




}
