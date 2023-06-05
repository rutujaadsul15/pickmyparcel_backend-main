package com.company.pickmyparcel.model;

import lombok.*;

import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DealResponse {
    private List<Deal> selfCreatedDealList;
    private List<Deal> pickUpDealList;

    public List<Deal> getSelfCreatedDealList() {
        return selfCreatedDealList;
    }

    public void setSelfCreatedDealList(List<Deal> selfCreatedDealList) {
        this.selfCreatedDealList = selfCreatedDealList;
    }

    public List<Deal> getPickUpDealList() {
        return pickUpDealList;
    }

    public void setPickUpDealList(List<Deal> pickUpDealList) {
        this.pickUpDealList = pickUpDealList;
    }
}
