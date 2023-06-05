package com.company.pickmyparcel.repository;

import com.company.pickmyparcel.model.ReceiverOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceiverOtpRepository extends JpaRepository<ReceiverOtp, Integer> {
}
