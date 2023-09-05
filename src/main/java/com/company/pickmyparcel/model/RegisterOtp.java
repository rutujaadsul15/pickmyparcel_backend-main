package com.company.pickmyparcel.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterOtp {
    @Id
    private String id = UUID.randomUUID().toString();
    private String userMobileNo;
    private String otp;
}
