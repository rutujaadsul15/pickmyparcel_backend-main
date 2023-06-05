package com.company.pickmyparcel.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;


@Entity
@Table(name = "PRICE_MAINTENANCE")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PriceMaintenance extends AuditModel {
    @Id
    private String id = UUID.randomUUID().toString();
    @Column(name = "FROM_GRAMS")
    private String fromGms;
    @Column(name = "TO_GRAMS")
    private String toGms;
    @Column(name = "FROM_KMS")
    private String fromKms;
    @Column(name = "TO_KMS")
    private String toKms;
    @Column(name = "PRICE")
    private String price;
    @Column(name = "CURRENCY")
    private String currency;
    @Column(name = "SERVICE_CHARGE_PERCENT")
    private String serviceChargePercent;
}
