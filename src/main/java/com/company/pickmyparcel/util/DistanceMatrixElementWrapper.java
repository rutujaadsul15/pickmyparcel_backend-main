package com.company.pickmyparcel.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DistanceMatrixElementWrapper {
    @JsonProperty("distance")
    public DistanceWrapper distance;
    @JsonProperty("duration")
    public DurationWrapper duration;
}
