package com.company.pickmyparcel.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Sender {
    @Id
    @GeneratedValue
    private Integer senderId;

    @Size(min = 10, max = 10, message = "Sender contact number should be of 10 digits")
    private String senderContactNo;

    @NotBlank(message = "sender FirstName name should present")
    private String senderFirstName;

    @NotBlank(message = "sender LastName name should present")
    private String senderLastName;

    private Integer rating = 4;

    @OneToOne(mappedBy = "sender")
    @JsonIgnore
    private Deal deal;

    @Override
    public String toString() {
        return "Sender{" +
                "senderId=" + senderId +
                ", senderContactNo='" + senderContactNo + '\'' +
                ", senderFirstName='" + senderFirstName + '\'' +
                ", senderLastName='" + senderLastName + '\'' +
                ", rating=" + rating +
                '}';
    }
}
