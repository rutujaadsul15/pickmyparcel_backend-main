package com.company.pickmyparcel.model.requests;

import com.company.pickmyparcel.model.DropLocation;
import com.company.pickmyparcel.model.PickUpLocation;
import lombok.*;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FutureTripRequest {
    private PickUpLocation pickUpLocation;
    private DropLocation dropLocation;
    private Date dateAndTime;
    private String carrierContactNo;
    private String carrierFirstName;
    private String carrierLastName;
}
