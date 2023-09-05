package com.company.pickmyparcel.model;


public enum ParcelStatus {
    PENDING_FOR_CARRIER_ACCEPTANCE,
    PENDING_FOR_SENDER_CONFIRMATION,
    LOCKED_BY_SENDER,
    DELIVERED,
    CANCELLED_BY_SENDER,
    CANCELLED_BY_CARRIER
}
