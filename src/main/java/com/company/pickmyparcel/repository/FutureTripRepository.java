package com.company.pickmyparcel.repository;

import com.company.pickmyparcel.model.FutureTrip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FutureTripRepository extends JpaRepository<FutureTrip, Integer> {
    List<FutureTrip> findByOnRoutePinCodesContainingAndOnRoutePinCodesContaining(String pickupPinCode, String dropPinCode);
}
