package com.company.pickmyparcel.service;

import com.company.pickmyparcel.model.PriceMaintenance;
import com.company.pickmyparcel.repository.PriceMaintenanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PriceMaintenanceServiceImpl implements PriceMaintenenceService {

    @Autowired
    private PriceMaintenanceRepository priceMaintenanceRepository;

    @Override
    public PriceMaintenance savePriceDetails(PriceMaintenance priceMaintenance) {
        return priceMaintenanceRepository.save(priceMaintenance);
    }

    @Override
    public PriceMaintenance updatePriceDetails(PriceMaintenance priceMaintenance) {
        if (priceMaintenance!=null && priceMaintenance.getId()!=null){
            Optional<PriceMaintenance> byId = priceMaintenanceRepository.findById(priceMaintenance.getId());
            if (byId.isPresent()){
                PriceMaintenance dbRecord = byId.get();
                dbRecord.setFromGms(dbRecord.getFromGms());
                dbRecord.setToGms(dbRecord.getToGms());
                dbRecord.setFromKms(dbRecord.getFromKms());
                dbRecord.setToKms(dbRecord.getToKms());
                dbRecord.setCurrency(dbRecord.getCurrency());
                dbRecord.setPrice(priceMaintenance.getPrice());
                dbRecord.setServiceChargePercent(priceMaintenance.getServiceChargePercent());
                return priceMaintenanceRepository.save(dbRecord);
            }
            throw new RuntimeException("Record with ID : " + priceMaintenance.getId() + " not present in DB");
        }
        throw new RuntimeException("Price maintenance record is not correct. Please enter details correctly");
    }
}
