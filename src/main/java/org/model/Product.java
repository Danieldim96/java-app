package org.model;

import java.io.Serializable;
import java.time.LocalDate;

public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private double deliveryPrice;
    private ProductCategory category;
    private LocalDate expirationDate;
    private int quantity;

    public Product(int id, String name, double deliveryPrice, ProductCategory category, 
                  LocalDate expirationDate, int quantity) {
        this.id = id;
        this.name = name;
        this.deliveryPrice = deliveryPrice;
        this.category = category;
        this.expirationDate = expirationDate;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getDeliveryPrice() {
        return deliveryPrice;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(expirationDate);
    }

    public boolean isNearExpiration(int daysThreshold) {
        return LocalDate.now().plusDays(daysThreshold).isAfter(expirationDate);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", deliveryPrice=" + deliveryPrice +
                ", category=" + category +
                ", expirationDate=" + expirationDate +
                ", quantity=" + quantity +
                '}';
    }
} 