package com.company.pickmyparcel.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Parcel {
    @Id
    @GeneratedValue
    private Integer parcelId;
    @Min(value = 0L, message = "Parcel weight should be greater than 0 grams")
    @Max(value = 10000L, message = "Parcel weight should not be greater than 10 Kg")
    private Long parcelWeight;

    @Min(value = 1L, message = "Parcel length should be at least 1 Centimeter")
    @Max(value = 1000L, message = "Parcel length should be less than than 200 Centimeters")
    private Long parcelLength;

    @Min(value = 1L, message = "Parcel height should be at least 1 Centimeter")
    @Max(value = 1000L, message = "Parcel height should be less than than 200 Centimeters")
    private Long parcelHeight;

    private String specialInstruction;
    private ParcelStatus parcelStatus;

    @OneToOne(mappedBy = "parcel")
    @JsonIgnore
    private Deal deal;

    @Override
    public String toString() {
        return "Parcel{" +
                "parcelId=" + parcelId +
                ", parcelWeight=" + parcelWeight +
                ", parcelLength=" + parcelLength +
                ", parcelHeight=" + parcelHeight +
                ", specialInstruction='" + specialInstruction + '\'' +
                ", parcelStatus=" + parcelStatus +
                '}';
    }
}
