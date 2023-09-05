package com.company.pickmyparcel.repository;


import com.company.pickmyparcel.model.Deal;
import com.company.pickmyparcel.model.DealPaymentStatus;
import com.company.pickmyparcel.model.ParcelStatus;
import com.company.pickmyparcel.model.response.DealAndPaymentResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface DealRepository extends JpaRepository<Deal, Integer> {
    List<Deal> findBySenderSenderContactNoOrderByCreatedDateDesc(String senderContactNo);
    List<Deal> findByCarrierCarrierContactNoOrderByCreatedDateDesc(String carrierContactNo);
    List<Deal> findByPickUpLocation_PinCodeInAndDropLocation_PinCodeInAndParcel_ParcelStatusInAndDealPaymentStatus(
            Set<String> pickUpPinCode, Set<String> dropPinCode, List<ParcelStatus> parcelStatuses, DealPaymentStatus dealPaymentStatus);
    List<Deal> findByPickUpLocation_PinCodeAndDropLocation_PinCode(String pickUpPinCode, String dropPinCode);
    @Query("SELECT d FROM Deal d WHERE DATE_FORMAT(d.date, '%Y-%m-%d') = :formattedDate")
    List<DealAndPaymentResponse> findDealsByDate(@Param("formattedDate") String formattedDate);
}

