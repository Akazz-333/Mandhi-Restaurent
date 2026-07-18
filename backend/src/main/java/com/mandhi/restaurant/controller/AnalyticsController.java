package com.mandhi.restaurant.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mandhi.restaurant.entity.Booking;
import com.mandhi.restaurant.entity.Order;
import com.mandhi.restaurant.repository.BookingRepository;
import com.mandhi.restaurant.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/bookings-per-day")
    public ResponseEntity<Map<String, Long>> getBookingsPerDay() {
        List<Booking> bookings = bookingRepository.findAll();
        // Use TreeMap to keep dates sorted chronologically
        Map<String, Long> aggregation = new TreeMap<>();
        
        for (Booking b : bookings) {
            String date = b.getDate(); // format: YYYY-MM-DD
            if (date != null && !date.isEmpty()) {
                aggregation.put(date, aggregation.getOrDefault(date, 0L) + 1);
            }
        }
        return ResponseEntity.ok(aggregation);
    }

    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Double>> getRevenuePerDay() {
        List<Order> orders = orderRepository.findAll();
        Map<String, Double> aggregation = new TreeMap<>();

        for (Order o : orders) {
            if (o.getCreatedAt() != null) {
                String date = o.getCreatedAt().toLocalDate().toString(); // format: YYYY-MM-DD
                double total = o.getTotalPrice() != null ? o.getTotalPrice() : 0.0;
                aggregation.put(date, aggregation.getOrDefault(date, 0.0) + total);
            }
        }
        return ResponseEntity.ok(aggregation);
    }

    @GetMapping("/orders-distribution")
    public ResponseEntity<Map<String, Long>> getOrdersDistribution() {
        List<Order> orders = orderRepository.findAll();
        Map<String, Long> distribution = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        for (Order o : orders) {
            if (o.getItemsJson() != null) {
                try {
                    JsonNode root = mapper.readTree(o.getItemsJson());
                    if (root.isArray()) {
                        for (JsonNode item : root) {
                            String name = item.path("name").asText("Other");
                            int qty = item.path("qty").asInt(1);
                            distribution.put(name, distribution.getOrDefault(name, 0L) + qty);
                        }
                    }
                } catch (Exception e) {
                    // Ignore parsing error for corrupted JSON
                }
            }
        }
        return ResponseEntity.ok(distribution);
    }
}
