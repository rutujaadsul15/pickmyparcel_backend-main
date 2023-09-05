package com.company.pickmyparcel.service;

import com.company.pickmyparcel.model.PriceMaintenance;

public interface PriceMaintenenceService {
    PriceMaintenance savePriceDetails(PriceMaintenance priceMaintenance);
    PriceMaintenance updatePriceDetails(PriceMaintenance priceMaintenance);
    String processPriceEngine();

}
