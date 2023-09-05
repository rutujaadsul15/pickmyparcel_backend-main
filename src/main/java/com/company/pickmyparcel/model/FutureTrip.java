package com.company.pickmyparcel.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.LinkedHashSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Getter
@Setter
public class FutureTrip {

    @Id
    @GeneratedValue
    private Integer futureTripId;
    @NotBlank(message = "street name should present")
    private String pickupAddressLine;

    @NotNull(message = "latitude should present")
    private Double pickupLatitude;

    @NotNull(message = "longitude should present")
    private Double pickupLongitude;

    @NotBlank(message = "city name should present")
    private String pickupCity;

    @NotBlank(message = "state name should present")
    private String pickupState;

    private String pickupPinCode;

    @NotBlank(message = "pinCode name should present")
    private String pickupPlaceId;

    @NotBlank(message = "street name should present")
    private String dropAddressLine;

    @NotNull(message = "latitude should present")
    private Double dropLatitude;

    @NotNull(message = "longitude should present")
    private Double dropLongitude;

    @NotBlank(message = "city name should present")
    private String dropCity;

    @NotBlank(message = "state name should present")
    private String dropState;

    private String dropPinCode;

    @NotBlank(message = "pinCode name should present")
    private String dropPlaceId;

    @Lob
    private String onRoutePinCodes;

    private Date dateAndTime;
    private String carrierContactNo;
    private String carrierFirstName;
    private String carrierLastName;


}
