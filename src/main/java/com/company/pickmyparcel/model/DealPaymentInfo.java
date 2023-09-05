package com.company.pickmyparcel.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DealPaymentInfo extends AuditModel {
    @Id
    private String orderId;
    private String transactionId;
    private Integer dealId;
    private Double amount;
    private String paymentStatus;
}
