package com.mandhi.restaurant.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;

    @Column(name = "items_json", columnDefinition = "TEXT", nullable = false)
    private String itemsJson;

    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    @Column(name = "payment_method")
    private String paymentMethod = "CASH";

    @Column(nullable = false)
    private String status = "STANDBY"; // STANDBY, COOKING, READY

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price")
    private Double price;

    @Column(name = "total_amount")
    private Double totalAmount;

    @PrePersist
    @PreUpdate
    public void calculateTotals() {
        if (this.totalAmount == null && this.totalPrice != null) {
            this.totalAmount = this.totalPrice;
        }
        if (this.totalPrice == null && this.totalAmount != null) {
            this.totalPrice = this.totalAmount;
        }
        if (this.itemsJson != null) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(this.itemsJson);
                int totalQty = 0;
                double firstPrice = 0.0;
                if (root.isArray()) {
                    for (int i = 0; i < root.size(); i++) {
                        com.fasterxml.jackson.databind.JsonNode item = root.get(i);
                        int q = item.path("qty").asInt(1);
                        double p = item.path("price").asDouble(0.0);
                        totalQty += q;
                        if (i == 0) {
                            firstPrice = p;
                        }
                    }
                }
                this.quantity = totalQty;
                this.price = firstPrice;
            } catch (Exception e) {
                this.quantity = 1;
                this.price = this.totalPrice;
            }
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getItemsJson() {
        return itemsJson;
    }

    public void setItemsJson(String itemsJson) {
        this.itemsJson = itemsJson;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
