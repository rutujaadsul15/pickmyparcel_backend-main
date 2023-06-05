package com.company.pickmyparcel.model.requests;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @Size(min = 10, max = 10)
    private String contactNo;

    @NotBlank(message = "Password cannot be blank")
    private String password;

}
