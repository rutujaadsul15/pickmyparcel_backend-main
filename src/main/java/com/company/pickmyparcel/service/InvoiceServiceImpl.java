package com.company.pickmyparcel.service;

import com.company.pickmyparcel.model.Deal;
import com.company.pickmyparcel.model.DealPaymentInfo;
import com.company.pickmyparcel.model.DealPaymentStatus;
import com.company.pickmyparcel.repository.DealPaymentInfoRepository;
import com.company.pickmyparcel.repository.DealRepository;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private static final Logger LOGGER = Logger.getLogger(InvoiceServiceImpl.class.getName());

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private DealPaymentInfoRepository dealPaymentInfoRepository;

    @Override
    public ByteArrayInputStream generateInvoice(Integer dealId) {
        try {
            Optional<Deal> optionalDealById = dealRepository.findById(dealId);
            Deal deal = optionalDealById.orElse(null);
            DealPaymentInfo dealPaymentInfo = dealPaymentInfoRepository.findDealPaymentInfoByDealId(dealId);
            if (deal != null && DealPaymentStatus.PAID.equals(optionalDealById.get().getDealPaymentStatus())) {
                LOGGER.info("Got the deal for invoice generation : " + dealId);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Document document = new Document();
                PdfWriter.getInstance(document, out);
                document.open();
                // Set up fonts
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD);
                Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
                Font contentBoldFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);

                // Add company name in orange color and larger font
                Font companyNameFont = FontFactory.getFont(FontFactory.HELVETICA, 22, Font.BOLD, new Color(255, 94, 20));
                Chunk companyName = new Chunk("pickmyparcel.in", companyNameFont);
                Paragraph companyParagraph = new Paragraph(companyName);
                companyParagraph.setAlignment(Element.ALIGN_RIGHT);
                document.add(companyParagraph);


                // Add header information
                Paragraph header = new Paragraph("Invoice", headerFont);
                header.setAlignment(Element.ALIGN_CENTER);
                document.add(header);

                Paragraph invoiceDetails1 = new Paragraph("Invoice No # : ", contentFont);
                Chunk invoiceNoValue = new Chunk(dealPaymentInfo.getOrderId(), contentBoldFont);
                invoiceDetails1.add(invoiceNoValue);
                document.add(invoiceDetails1);

                Paragraph invoiceDetails2 = new Paragraph("Invoice Date # : ", contentFont);
                Chunk invoiceDateValue = new Chunk(dealPaymentInfo.getCreatedDate().toString(), contentBoldFont);
                invoiceDetails2.add(invoiceDateValue);
                document.add(invoiceDetails2);

                Paragraph invoiceDetails3 = new Paragraph("Transaction Id # : ", contentFont);
                Chunk dealIdValue = new Chunk(dealPaymentInfo.getTransactionId(), contentBoldFont);
                invoiceDetails3.add(dealIdValue);
                document.add(invoiceDetails3);


                // Modify the Billed By and Billed To details
                PdfPTable billedByAndToTable = new PdfPTable(2);
                billedByAndToTable.setWidthPercentage(100);
                billedByAndToTable.setSpacingBefore(15f);
                billedByAndToTable.setSpacingAfter(15f);
                billedByAndToTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);


                Paragraph billedBy = new Paragraph("Billed By", contentBoldFont);
                Paragraph billedByDetails = new Paragraph(
                        "pickmyparcel.in\n" +
                                "Phase 1, Hinjewadi,\n" +
                                "Pune 411057\n" +
                                "info@pickmyparcel.in",
                        contentFont
                );


                Paragraph billedTo = new Paragraph("Billed To", contentBoldFont);
                Paragraph billedToDetails = new Paragraph(
                        deal.getSender().getSenderFirstName() + " " + deal.getSender().getSenderLastName() + "\n" +
                                deal.getPickUpLocation().getAddressLine() + "\n" +
                                deal.getPickUpLocation().getCity() + "\n" +
                                "India\n" +
                                "Phone: " + deal.getSender().getSenderContactNo(),
                        contentFont
                );
                billedByAndToTable.addCell(billedBy);
                billedByAndToTable.addCell(billedTo);
                billedByDetails.setIndentationRight(20);
                billedByAndToTable.addCell(billedByDetails);
                billedToDetails.setIndentationLeft(20);
                billedByAndToTable.addCell(billedToDetails);


                document.add(billedByAndToTable);

                // Add table for items
                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                table.setSpacingAfter(10f);

                // Add table headers
                PdfPCell cell = new PdfPCell(new Phrase("Item"));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(Color.lightGray);
                cell.setPadding(5f);
                table.addCell(cell);
                cell = new PdfPCell(new Phrase("Quantity"));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(Color.lightGray);
                cell.setPadding(5f);
                table.addCell(cell);
                cell = new PdfPCell(new Phrase("Unit Price"));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(Color.lightGray);
                cell.setPadding(5f);
                table.addCell(cell);
                cell = new PdfPCell(new Phrase("GST"));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(Color.lightGray);
                cell.setPadding(5f);
                table.addCell(cell);
                cell = new PdfPCell(new Phrase("Total"));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(Color.lightGray);
                cell.setPadding(5f);
                table.addCell(cell);

                // Add sample data for demonstration
                String[][] items = {
                        {"Send Parcel Order", "1", deal.getDealTotal().toString(), "0", deal.getDealTotal().toString()}
                };

                for (String[] item : items) {
                    for (String value : item) {
                        PdfPCell cell2 = new PdfPCell(new Phrase(value, contentFont));
                        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell2.setPadding(5f);
                        table.addCell(cell2);
                    }
                }
                document.add(table);

                Paragraph grandTotal = new Paragraph("Total Rs " + deal.getDealTotal().toString(), contentBoldFont);
                grandTotal.setAlignment(Element.ALIGN_RIGHT);
                //document.add(grandTotal);

                // Add a horizontal line above the "Total Rs"
                PdfPTable topLineTable = new PdfPTable(1);
                topLineTable.setWidthPercentage(100);
                PdfPCell topLineCell = new PdfPCell(new Phrase(" "));
                topLineCell.setBorder(Rectangle.BOTTOM);
                topLineCell.setBorderColor(new Color(0, 0, 0));
                topLineCell.setMinimumHeight(2f);
                topLineTable.addCell(topLineCell);
                document.add(topLineTable);

                document.add(grandTotal);


                // Combine the Phrases in the footer paragraph
                Paragraph footer = new Paragraph();
                //footer.setFont(contentBoldFont); // Make "Terms and Conditions" bold
                footer.add(new Phrase("Terms and Conditions", FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD)));
                footer.add(new Phrase("\n1. Payment Confirmation: This invoice serves as a confirmation of the successful payment received for the specified services/goods. " +
                        "The payment has been received in full and the transaction is considered complete.\n" +
                        "2. Receipt of Payment: The payment made for this invoice has been received and recorded. " +
                        "A valid proof of payment is available upon request. Please keep this invoice as a record of your payment.\n", FontFactory.getFont(FontFactory.HELVETICA, 10)));
                footer.add(new Phrase("\n"));
                document.add(footer);


                String boldText = "info@pickmyparcel.in";
                String regularText = "For any enquiry, reach out via email at ";
                Paragraph emailParagraph = new Paragraph();
                emailParagraph.setFont(contentFont);
                emailParagraph.add(new Phrase(regularText, FontFactory.getFont(FontFactory.HELVETICA, 10)));
                emailParagraph.add(new Phrase(boldText, FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD)));
                boldText = "+91 9960743366, +91 9404771200";
                regularText = " or call on : ";
                emailParagraph.add(new Phrase(regularText, FontFactory.getFont(FontFactory.HELVETICA, 10)));
                emailParagraph.add(new Phrase(boldText, FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD)));
                document.add(emailParagraph);
                document.close();
                return new ByteArrayInputStream(out.toByteArray());
            } else {
                LOGGER.warning("Something went wrong while generating invoice");
                throw new RuntimeException("Deal payment is not done yet");
            }
        } catch (Exception e) {
            LOGGER.warning("Exception occurred while generating payment invoice : " + e.getMessage());
            return null;
        }
    }
}
