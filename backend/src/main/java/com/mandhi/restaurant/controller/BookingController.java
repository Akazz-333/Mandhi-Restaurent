package com.mandhi.restaurant.controller;

import com.mandhi.restaurant.entity.Booking;
import com.mandhi.restaurant.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.mandhi.restaurant.config.NotificationWebSocketHandler;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private NotificationWebSocketHandler webSocketHandler;

    @GetMapping("/bookings/availability")
    public ResponseEntity<String> checkAvailability(
            @RequestParam String tableId,
            @RequestParam String date,
            @RequestParam String time) {
        
        List<Booking> bookings = bookingRepository.findByDateAndTime(date, time);
        for (Booking b : bookings) {
            if (b.getNotes() != null && b.getNotes().contains("Table: " + tableId)) {
                return ResponseEntity.ok("RESERVED");
            }
        }
        return ResponseEntity.ok("AVAILABLE");
    }

    @PostMapping("/bookings")
    public ResponseEntity<?> createBooking(@RequestBody Booking booking) {
        if (booking.getName() == null || booking.getPhone() == null || booking.getDate() == null || booking.getTime() == null || booking.getGuests() == null) {
            return ResponseEntity.badRequest().body("Required booking fields are missing.");
        }

        // Perform backend validation to prevent double booking of the exact table
        String tableId = "";
        if (booking.getNotes() != null && booking.getNotes().contains("Table: ")) {
            int index = booking.getNotes().indexOf("Table: ");
            tableId = booking.getNotes().substring(index + 7).trim();
        }

        if (!tableId.isEmpty()) {
            List<Booking> existing = bookingRepository.findByDateAndTime(booking.getDate(), booking.getTime());
            for (Booking b : existing) {
                if (b.getNotes() != null && b.getNotes().contains("Table: " + tableId)) {
                    return ResponseEntity.badRequest().body("Table " + tableId + " is already reserved for this date and time slot.");
                }
            }
        }

        Booking savedBooking = bookingRepository.save(booking);

        // Broadcast WebSocket event for real-time notification
        try {
            webSocketHandler.broadcast("NEW_BOOKING");
        } catch (Exception e) {
            System.err.println("Failed to broadcast booking update: " + e.getMessage());
        }

        return ResponseEntity.ok(savedBooking);
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return ResponseEntity.ok(bookings);
    }
}
