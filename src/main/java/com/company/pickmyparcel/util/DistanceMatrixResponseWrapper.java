package com.company.pickmyparcel.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.maps.model.DistanceMatrixElementStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DistanceMatrixResponseWrapper {
    @JsonProperty("rows")
    public DistanceMatrixRowWrapper[] rows;
    @JsonProperty("status")
    public DistanceMatrixElementStatus status;

}
