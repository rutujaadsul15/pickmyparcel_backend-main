package com.company.pickmyparcel.model.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetOTPRequest {
    private String passwordResetOtp;
    private String mobileNo;
}
