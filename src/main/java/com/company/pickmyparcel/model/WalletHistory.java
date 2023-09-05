package com.company.pickmyparcel.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WalletHistory extends AuditModel {
    @Id
    @GeneratedValue
    private Integer walletHistoryId;
    private Integer dealId;
    private Double transactionAmount;
}
