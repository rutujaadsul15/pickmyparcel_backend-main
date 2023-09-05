package com.company.pickmyparcel.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
public class Address {
    @Id
    @GeneratedValue
    private Integer addressId;

    private String street;

    private String city;

    @NotBlank(message = "district name should present")
    private String district;

    @NotBlank(message = "pinCode name should present")
    private String pinCode;

}
