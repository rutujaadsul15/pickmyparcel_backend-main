package com.company.pickmyparcel.repository;

import com.company.pickmyparcel.model.RegisterOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegisterOtpRepository extends JpaRepository<RegisterOtp, Integer> {
    RegisterOtp findByUserMobileNo(String mobileNo);
}
