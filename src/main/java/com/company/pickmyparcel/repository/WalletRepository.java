package com.company.pickmyparcel.repository;


import com.company.pickmyparcel.model.FutureTrip;
import com.company.pickmyparcel.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, String> {
}
