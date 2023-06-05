package com.company.pickmyparcel.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Carrier {
    @Id
    @GeneratedValue
    private Integer carrierId;
    private String carrierContactNo;
    private String carrierFirstName;
    private String carrierLastName;
    private String vehicleDetails;
    private Integer rating = 4;
    @OneToOne(mappedBy = "carrier")
    @JsonIgnore
    private Deal deal;

    @Override
    public String toString() {
        return "Carrier{" +
                "carrierId=" + carrierId +
                ", carrierContactNo='" + carrierContactNo + '\'' +
                ", carrierFirstName='" + carrierFirstName + '\'' +
                ", carrierLastName='" + carrierLastName + '\'' +
                ", vehicleDetails='" + vehicleDetails + '\'' +
                ", rating=" + rating +
                '}';
    }
}
