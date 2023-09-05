package com.company.pickmyparcel.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DistanceWrapper {
    @JsonProperty("text")
    public String text;
    @JsonProperty("value")
    public String value;
}
