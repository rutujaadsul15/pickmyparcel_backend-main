package com.company.pickmyparcel.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class DropLocation {
    @Id
    @GeneratedValue
    private Integer dropLocationId;

    @NotBlank(message = "street name should present")
    private String addressLine;

    @NotNull(message = "latitude should present")
    private Double latitude;

    @NotNull(message = "longitude should present")
    private Double longitude;

    private String city;

    private String state;

    private String pinCode;

    @NotBlank(message = "placeId name should present")
    private String placeId;

    @OneToOne(mappedBy = "dropLocation")
    @JsonIgnore
    private Deal deal;

    @Override
    public String toString() {
        return "DropLocation{" +
                "dropLocationId=" + dropLocationId +
                ", addressLine='" + addressLine + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", pinCode='" + pinCode + '\'' +
                ", placeId='" + placeId + '\'' +
                '}';
    }
}
