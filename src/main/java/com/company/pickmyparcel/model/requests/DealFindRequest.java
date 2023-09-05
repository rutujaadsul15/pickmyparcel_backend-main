package com.company.pickmyparcel.model.requests;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealFindRequest {
    @NotBlank
    private String entityName;
    @NotBlank
    private String contactNumber;
}
