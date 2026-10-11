package com.cab.booking.receipt;

import com.cab.booking.common.ResourceNotFoundException;
import com.cab.booking.ride.Ride;
import com.cab.booking.ride.RideRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfReceiptGeneratorService {

    private final RideRepository rideRepository;

    public PdfReceiptGeneratorService(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    @Transactional(readOnly = true)
    public byte[] generateRideReceiptPdf(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found: " + rideId));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 50, 50);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new Color(16, 185, 129));
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(15, 23, 42));
            Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);

            // Header Title
            Paragraph title = new Paragraph("CABFLOW MOBILITY", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("Official Trip Receipt & Invoice", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            // Receipt Metadata Table
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setSpacingAfter(15);

            metaTable.addCell(createCleanCell("Receipt ID: INV-" + ride.getId() + "-" + (ride.getCompletedAt() != null ? ride.getCompletedAt().toEpochMilli() : System.currentTimeMillis()), boldFont));
            metaTable.addCell(createCleanCell("Date: " + DateTimeFormatter.ISO_INSTANT.format(ride.getCompletedAt() != null ? ride.getCompletedAt() : ride.getRequestedAt()), textFont));
            metaTable.addCell(createCleanCell("Rider: " + ride.getRider().getFullName(), textFont));
            metaTable.addCell(createCleanCell("Phone: " + ride.getRider().getPhoneNumber(), textFont));

            if (ride.getDriver() != null) {
                metaTable.addCell(createCleanCell("Driver: " + ride.getDriver().getUser().getFullName(), textFont));
                String vehicleName = "Sedan (Standard)";
                try {
                    if (ride.getDriver().getVehicles() != null && !ride.getDriver().getVehicles().isEmpty()) {
                        vehicleName = ride.getDriver().getVehicles().get(0).getMake() + " " + ride.getDriver().getVehicles().get(0).getModel();
                    }
                } catch (Exception ignored) {}
                metaTable.addCell(createCleanCell("Vehicle: " + vehicleName, textFont));
            } else {
                metaTable.addCell(createCleanCell("Driver: Unassigned", textFont));
                metaTable.addCell(createCleanCell("Vehicle: Standard", textFont));
            }

            document.add(metaTable);

            // Route Details Table
            Paragraph routeHeader = new Paragraph("Trip Route & Itinerary", boldFont);
            routeHeader.setSpacingAfter(8);
            document.add(routeHeader);

            PdfPTable routeTable = new PdfPTable(1);
            routeTable.setWidthPercentage(100);
            routeTable.setSpacingAfter(20);

            PdfPCell pickupCell = new PdfPCell(new Phrase("Pickup: " + ride.getPickupAddress(), textFont));
            pickupCell.setPadding(8);
            pickupCell.setBackgroundColor(new Color(241, 245, 249));
            pickupCell.setBorder(Rectangle.NO_BORDER);
            routeTable.addCell(pickupCell);

            PdfPCell dropoffCell = new PdfPCell(new Phrase("Dropoff: " + ride.getDropoffAddress(), textFont));
            dropoffCell.setPadding(8);
            dropoffCell.setBackgroundColor(new Color(248, 250, 252));
            dropoffCell.setBorder(Rectangle.NO_BORDER);
            routeTable.addCell(dropoffCell);

            document.add(routeTable);

            // Fare Breakdown Table
            PdfPTable fareTable = new PdfPTable(2);
            fareTable.setWidthPercentage(100);
            fareTable.setWidths(new float[]{70f, 30f});

            // Table Header
            PdfPCell h1 = new PdfPCell(new Phrase("Item Description", headerFont));
            h1.setBackgroundColor(new Color(15, 23, 42));
            h1.setPadding(8);
            PdfPCell h2 = new PdfPCell(new Phrase("Amount (" + ride.getCurrency() + ")", headerFont));
            h2.setBackgroundColor(new Color(15, 23, 42));
            h2.setPadding(8);
            h2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            fareTable.addCell(h1);
            fareTable.addCell(h2);

            fareTable.addCell(createTableRow("Trip Distance (" + ride.getDistanceKm() + " km)", textFont));
            fareTable.addCell(createTableRowRight(ride.getCurrency() + " " + ride.getDistanceKm(), textFont));

            fareTable.addCell(createTableRow("Estimated Duration (" + ride.getEstimatedDurationMinutes() + " mins)", textFont));
            fareTable.addCell(createTableRowRight(ride.getEstimatedDurationMinutes() + " min", textFont));

            fareTable.addCell(createTableRow("Total Trip Fare (" + ride.getVehicleCategory() + ")", boldFont));
            fareTable.addCell(createTableRowRight(ride.getCurrency() + " " + (ride.getFinalFare() != null ? ride.getFinalFare() : ride.getEstimatedFare()), boldFont));

            document.add(fareTable);

            // Footer note
            Paragraph footer = new Paragraph("\nThank you for riding with CabFlow! For support, visit support@cabflow.app.", textFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF receipt", e);
        }
    }

    private PdfPCell createCleanCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(4);
        return cell;
    }

    private PdfPCell createTableRow(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        return cell;
    }

    private PdfPCell createTableRowRight(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }
}
