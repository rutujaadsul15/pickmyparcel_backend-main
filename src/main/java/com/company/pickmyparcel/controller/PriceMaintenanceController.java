package com.company.pickmyparcel.controller;

import com.company.pickmyparcel.model.PriceMaintenance;
import com.company.pickmyparcel.service.PriceMaintenenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/priceMaintenance")
public class PriceMaintenanceController {

    @Autowired
    private PriceMaintenenceService priceMaintenenceService;


    @PostMapping("/")
    ResponseEntity<PriceMaintenance> savePriceDetails(@RequestBody PriceMaintenance priceMaintenance) {
        PriceMaintenance response = priceMaintenenceService.savePriceDetails(priceMaintenance);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/")
    ResponseEntity<PriceMaintenance> updatePriceDetails(@RequestBody PriceMaintenance priceMaintenance) {
        PriceMaintenance response = priceMaintenenceService.updatePriceDetails(priceMaintenance);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/processPriceEngine")
    ResponseEntity<String> updatePriceDetails() {
        String result = priceMaintenenceService.processPriceEngine();
        return ResponseEntity.ok(result);
    }
}
