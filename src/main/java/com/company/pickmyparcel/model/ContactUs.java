package com.company.pickmyparcel.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Size;
import java.util.UUID;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ContactUs extends AuditModel {
    @Id
    private String id = UUID.randomUUID().toString();

    @Size(min = 10, max = 10)
    private String mobileNo;

    private String message;
}
