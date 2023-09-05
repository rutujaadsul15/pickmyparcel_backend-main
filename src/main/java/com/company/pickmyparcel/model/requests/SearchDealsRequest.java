package com.company.pickmyparcel.model.requests;

import com.company.pickmyparcel.model.DropLocation;
import com.company.pickmyparcel.model.PickUpLocation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Size;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchDealsRequest {
    @Size(min = 10, max = 10, message = "Contact number should be of 10 digits")
    private String carrierContactNumber;
    private PickUpLocation pickUpLocation;
    private DropLocation dropLocation;

}
