package com.mandhi.restaurant.controller;

import com.mandhi.restaurant.entity.Order;
import com.mandhi.restaurant.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

import com.mandhi.restaurant.config.NotificationWebSocketHandler;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private NotificationWebSocketHandler webSocketHandler;

    @PostMapping("/orders")
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        if (order.getItemsJson() == null || order.getTotalPrice() == null) {
            return ResponseEntity.badRequest().build();
        }

        // Generate unique order number (format: ORD-YYYYMMDD-XXXX)
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd");
        String dateStr = java.time.LocalDate.now().format(formatter);
        int randomCode = 1000 + new Random().nextInt(9000);
        order.setOrderNumber("ORD-" + dateStr + "-" + randomCode);
        order.setStatus("COOKING"); // STANDBY, COOKING, READY

        Order savedOrder = orderRepository.save(order);

        // Broadcast WebSocket event
        try {
            webSocketHandler.broadcast("NEW_ORDER");
        } catch (Exception e) {
            System.err.println("Failed to broadcast order update: " + e.getMessage());
        }

        return ResponseEntity.ok(savedOrder);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(orderOpt.get());
    }

    @GetMapping("/orders/number/{orderNumber}")
    public ResponseEntity<Order> getOrderByOrderNumber(@PathVariable String orderNumber) {
        Optional<Order> orderOpt = orderRepository.findByOrderNumber(orderNumber);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(orderOpt.get());
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/orders/active")
    public ResponseEntity<List<Order>> getActiveOrders() {
        List<Order> activeOrders = orderRepository.findByStatusNotOrderByCreatedAtDesc("READY");
        return ResponseEntity.ok(activeOrders);
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Order order = orderOpt.get();
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/orders/date/{date}")
    public ResponseEntity<List<Order>> getOrdersByDate(@PathVariable String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            LocalDateTime startOfDay = localDate.atStartOfDay();
            LocalDateTime endOfDay = localDate.atTime(23, 59, 59, 999999999);
            List<Order> orders = orderRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startOfDay, endOfDay);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/orders/summary")
    public ResponseEntity<?> getDailySummary(@RequestParam(required = false) String date) {
        try {
            LocalDate localDate = (date != null) ? LocalDate.parse(date) : LocalDate.now();
            LocalDateTime startOfDay = localDate.atStartOfDay();
            LocalDateTime endOfDay = localDate.atTime(23, 59, 59, 999999999);
            List<Order> orders = orderRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startOfDay, endOfDay);

            int totalOrders = orders.size();
            double totalEarnings = 0.0;
            int completedOrders = 0;
            int pendingOrders = 0;
            Map<String, Integer> itemCounts = new HashMap<>();

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

            for (Order order : orders) {
                totalEarnings += (order.getTotalAmount() != null) ? order.getTotalAmount() : (order.getTotalPrice() != null ? order.getTotalPrice() : 0.0);
                if ("READY".equalsIgnoreCase(order.getStatus())) {
                    completedOrders++;
                } else {
                    pendingOrders++;
                }

                if (order.getItemsJson() != null) {
                    try {
                        com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(order.getItemsJson());
                        if (root.isArray()) {
                            for (com.fasterxml.jackson.databind.JsonNode item : root) {
                                String name = item.path("name").asText();
                                int qty = item.path("qty").asInt(1);
                                if (name != null && !name.isEmpty()) {
                                    itemCounts.put(name, itemCounts.getOrDefault(name, 0) + qty);
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }

            String mostOrderedItem = "N/A";
            int maxQty = 0;
            for (Map.Entry<String, Integer> entry : itemCounts.entrySet()) {
                if (entry.getValue() > maxQty) {
                    maxQty = entry.getValue();
                    mostOrderedItem = entry.getKey() + " (x" + maxQty + ")";
                }
            }

            Map<String, Object> summary = new HashMap<>();
            summary.put("date", localDate.toString());
            summary.put("totalOrders", totalOrders);
            summary.put("totalEarnings", totalEarnings);
            summary.put("completedOrders", completedOrders);
            summary.put("pendingOrders", pendingOrders);
            summary.put("mostOrderedItem", mostOrderedItem);

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
