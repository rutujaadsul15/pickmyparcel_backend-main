package com.company.pickmyparcel.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.List;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Wallet extends AuditModel {
    @Id
    @Size(min = 10, max = 10)
    private String userContactNumber;

    private Double walletBalance;

    @OneToOne(mappedBy = "wallet")
    @JsonIgnore
    private User user;

    @OneToMany(cascade = CascadeType.ALL)
    private List<WalletHistory> walletHistoryList;
}
