package com.company.pickmyparcel.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DistanceMatrixRowWrapper {

    @JsonProperty("elements")
    public DistanceMatrixElementWrapper[] elements;

}
