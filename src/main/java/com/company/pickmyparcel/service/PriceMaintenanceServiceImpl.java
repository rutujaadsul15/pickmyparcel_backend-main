package com.company.pickmyparcel.service;

import com.company.pickmyparcel.model.PriceMaintenance;
import com.company.pickmyparcel.repository.PriceMaintenanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;
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
        if (priceMaintenance != null && priceMaintenance.getId() != null) {
            Optional<PriceMaintenance> byId = priceMaintenanceRepository.findById(priceMaintenance.getId());
            if (byId.isPresent()) {
                PriceMaintenance dbRecord = byId.get();
                dbRecord.setFromGms(priceMaintenance.getFromGms());
                dbRecord.setToGms(priceMaintenance.getToGms());
                dbRecord.setFromKms(priceMaintenance.getFromKms());
                dbRecord.setToKms(priceMaintenance.getToKms());
                dbRecord.setCurrency(priceMaintenance.getCurrency());
                dbRecord.setPrice(priceMaintenance.getPrice());
                dbRecord.setServiceChargePercent(priceMaintenance.getServiceChargePercent());
                return priceMaintenanceRepository.save(dbRecord);
            }
            throw new RuntimeException("Record with ID : " + priceMaintenance.getId() + " not present in DB");
        }
        throw new RuntimeException("Price maintenance record is not correct. Please enter details correctly");
    }

    public String processPriceEngine() {
        String filePath = Objects.requireNonNull(getClass().getResource("/PriceEngine.text")).getPath();
        priceMaintenanceRepository.deleteAll();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(filePath)))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    processLine(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "success";
    }

    private void processLine(String line) {
        // Split the line into individual parts
        String[] parts = line.split(":");

        // Extract the values from each part
        String gramsRange = parts[0].trim();
        String kmsRange = parts[1].trim();
        String priceRange = parts[2].trim();

        // Extract the values from gramsRange and kmsRange
        String fromGms = gramsRange.split("to")[0].trim().replaceAll("\\D+", "");
        String toGms = gramsRange.split("to")[1].trim().replaceAll("\\D+", "");
        String fromKms = kmsRange.split("to")[0].trim().replaceAll("\\D+", "");
        String toKms = kmsRange.split("to")[1].trim().replaceAll("\\D+", "");
        String price = priceRange.replaceAll("\\D+", "");

        // Create a PriceMaintenance object and populate the values
        PriceMaintenance priceMaintenance = new PriceMaintenance();
        priceMaintenance.setFromGms(fromGms);
        priceMaintenance.setToGms(toGms);
        priceMaintenance.setFromKms(fromKms);
        priceMaintenance.setToKms(toKms);
        priceMaintenance.setPrice(price);
        priceMaintenance.setCurrency("INR");
        priceMaintenance.setServiceChargePercent("0");
        priceMaintenanceRepository.save(priceMaintenance);
    }
}
