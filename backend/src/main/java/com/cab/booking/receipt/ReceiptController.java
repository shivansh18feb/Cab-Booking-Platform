package com.cab.booking.receipt;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rides")
@Tag(name = "Receipts & Invoices", description = "Endpoints for downloading trip invoices in PDF format")
public class ReceiptController {

    private final PdfReceiptGeneratorService receiptGeneratorService;

    public ReceiptController(PdfReceiptGeneratorService receiptGeneratorService) {
        this.receiptGeneratorService = receiptGeneratorService;
    }

    @GetMapping("/{rideId}/receipt")
    @Operation(summary = "Download official PDF receipt for a trip")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Long rideId) {
        byte[] pdfBytes = receiptGeneratorService.generateRideReceiptPdf(rideId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"CabFlow_Receipt_" + rideId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
