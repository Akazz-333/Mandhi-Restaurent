package com.mandhi.restaurant.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.mandhi.restaurant.entity.Booking;
import com.mandhi.restaurant.entity.Order;
import com.mandhi.restaurant.repository.BookingRepository;
import com.mandhi.restaurant.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@RestController
@RequestMapping("/api/receipt")
@CrossOrigin(origins = "*")
public class ReceiptController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/order/{id}")
    public ResponseEntity<byte[]> getOrderReceipt(@PathVariable Long id) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Order order = orderOpt.get();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A6, 20, 20, 20, 20); // Thermal receipt-like format
            PdfWriter.getInstance(document, out);

            document.open();

            // Font definitions
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, new BaseColor(181, 132, 30));
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.BLACK);
            Font bodyFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.DARK_GRAY);
            Font priceFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.BLACK);

            // Restaurant Title
            Paragraph title = new Paragraph("MANDHI HOUSE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            Paragraph subtitle = new Paragraph("Yemeni Slow-Roast Kitchen\nReceipt & Invoice", bodyFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(15);
            document.add(subtitle);

            // Invoice details
            document.add(new Paragraph("Order Number: " + order.getOrderNumber(), headerFont));
            document.add(new Paragraph("Date: " + order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), bodyFont));
            document.add(new Paragraph("Payment: " + order.getPaymentMethod(), bodyFont));
            document.add(new Paragraph("Status: " + order.getStatus(), bodyFont));
            document.add(new Paragraph("----------------------------------------------------------------", bodyFont));

            // Items table
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{50, 20, 30});
            table.setSpacingBefore(10);
            table.setSpacingAfter(10);

            // Table Headers
            table.addCell(new PdfPCell(new Paragraph("Item", headerFont)));
            table.addCell(new PdfPCell(new Paragraph("Qty", headerFont)));
            table.addCell(new PdfPCell(new Paragraph("Price", headerFont)));

            // Parse and render items JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(order.getItemsJson());
            if (root.isArray()) {
                for (JsonNode item : root) {
                    String name = item.path("name").asText("Mandi Dish");
                    String size = item.path("size").asText("single");
                    int qty = item.path("qty").asInt(1);
                    double price = item.path("price").asDouble(0.0);
                    
                    table.addCell(new PdfPCell(new Paragraph(name + " (" + size + ")", bodyFont)));
                    table.addCell(new PdfPCell(new Paragraph(String.valueOf(qty), bodyFont)));
                    table.addCell(new PdfPCell(new Paragraph("Rs. " + (price * qty), priceFont)));
                }
            }

            document.add(table);
            document.add(new Paragraph("----------------------------------------------------------------", bodyFont));

            Paragraph total = new Paragraph("Grand Total: Rs. " + order.getTotalPrice(), titleFont);
            total.setAlignment(Element.ALIGN_RIGHT);
            total.setSpacingAfter(15);
            document.add(total);

            // Add QR Code
            String trackingUrl = "http://localhost:8081/orders/track?number=" + order.getOrderNumber();
            byte[] qrBytes = generateQRCodeImage(trackingUrl, 100, 100);
            Image qrImage = Image.getInstance(qrBytes);
            qrImage.setAlignment(Element.ALIGN_CENTER);
            document.add(qrImage);

            Paragraph footer = new Paragraph("Scan QR code to track your order live.\nThank you for dining with us!", bodyFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(5);
            document.add(footer);

            document.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "receipt-" + order.getOrderNumber() + ".pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(out.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/booking/{id}")
    public ResponseEntity<byte[]> getBookingPass(@PathVariable Long id) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Booking booking = bookingOpt.get();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A6, 20, 20, 20, 20); // Ticket/Pass format
            PdfWriter.getInstance(document, out);

            document.open();

            // Font definitions
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, new BaseColor(181, 132, 30));
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.BLACK);
            Font bodyFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.DARK_GRAY);

            // Restaurant Title
            Paragraph title = new Paragraph("MANDHI HOUSE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            Paragraph subtitle = new Paragraph("Yemeni Slow-Roast Kitchen\nTable Reservation Pass", bodyFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(15);
            document.add(subtitle);

            // Booking Details
            document.add(new Paragraph("Guest Name: " + booking.getName(), headerFont));
            document.add(new Paragraph("Phone Number: " + booking.getPhone(), bodyFont));
            document.add(new Paragraph("Reservation Date: " + booking.getDate(), bodyFont));
            document.add(new Paragraph("Arrival Time: " + booking.getTime(), bodyFont));
            document.add(new Paragraph("Guests: " + booking.getGuests() + " People", bodyFont));
            document.add(new Paragraph("Notes: " + (booking.getNotes() != null ? booking.getNotes() : "N/A"), bodyFont));
            document.add(new Paragraph("----------------------------------------------------------------", bodyFont));

            // Add QR Code
            String passData = "Booking ID: " + booking.getId() + "\nGuest: " + booking.getName() + "\nSlot: " + booking.getDate() + " " + booking.getTime();
            byte[] qrBytes = generateQRCodeImage(passData, 100, 100);
            Image qrImage = Image.getInstance(qrBytes);
            qrImage.setAlignment(Element.ALIGN_CENTER);
            qrImage.setSpacingBefore(10);
            document.add(qrImage);

            Paragraph footer = new Paragraph("Present this Reservation Pass at entry.\nWe look forward to serving you!", bodyFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(10);
            document.add(footer);

            document.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "booking-pass-" + booking.getId() + ".pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(out.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    private byte[] generateQRCodeImage(String text, int width, int height) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }
}
