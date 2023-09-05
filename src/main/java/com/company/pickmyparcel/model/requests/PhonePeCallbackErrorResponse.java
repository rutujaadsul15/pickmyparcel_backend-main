package com.company.pickmyparcel.model.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PhonePeCallbackErrorResponse {
    @JsonProperty("id")
    private String id;

    @JsonProperty("error")
    private PhonePeCallbackError error;

    public PhonePeCallbackErrorResponse(String id, String code, String message) {
        this.id = id;
        this.error = new PhonePeCallbackError(code, message);
    }
}
