package com.company.pickmyparcel.service;

import java.io.ByteArrayInputStream;

public interface InvoiceService {
    ByteArrayInputStream generateInvoice(Integer dealId);
}
