package com.company.pickmyparcel.repository;

import com.company.pickmyparcel.model.PriceMaintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceMaintenanceRepository extends JpaRepository<PriceMaintenance, String> {
}
