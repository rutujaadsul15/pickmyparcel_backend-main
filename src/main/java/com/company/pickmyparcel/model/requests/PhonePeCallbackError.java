package com.company.pickmyparcel.model.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PhonePeCallbackError {
    @JsonProperty("code")
    private String code;

    @JsonProperty("message")
    private String message;

    public PhonePeCallbackError(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
