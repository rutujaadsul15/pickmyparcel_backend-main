package com.company.pickmyparcel.repository;

import com.company.pickmyparcel.model.DealPaymentInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DealPaymentInfoRepository extends JpaRepository<DealPaymentInfo, String> {
    DealPaymentInfo findDealPaymentInfoByDealId(Integer dealId);
}
