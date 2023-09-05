package com.company.pickmyparcel.model.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhonePeCallbackRequest {
    @JsonProperty("id")
    private String id;

    @JsonProperty("result")
    private PhonePeCallbackResult result;

}
