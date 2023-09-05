package com.company.pickmyparcel.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Receiver {
    @Id
    @GeneratedValue
    private Integer receiverId;

    @Size(min = 10, max = 10, message = "Receiver contact number should be of 10 digits")
    private String receiverContactNo;

    @NotBlank(message = "receiver FirstName name should present")
    private String receiverFirstName;

    @NotBlank(message = "receiver LastName name should present")
    private String receiverLastName;

    private Integer rating = 4;

    @OneToOne(mappedBy = "receiver")
    @JsonIgnore
    private Deal deal;

    @Override
    public String toString() {
        return "Receiver{" +
                "receiverId=" + receiverId +
                ", receiverContactNo='" + receiverContactNo + '\'' +
                ", receiverFirstName='" + receiverFirstName + '\'' +
                ", receiverLastName='" + receiverLastName + '\'' +
                ", rating=" + rating +
                '}';
    }
}
