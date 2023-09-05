package com.company.pickmyparcel.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiverOtp {
    @Id
    private Integer dealId;
    private String receiverContactNo;
    private String otp;
}
