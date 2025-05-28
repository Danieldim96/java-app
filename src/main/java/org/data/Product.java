package org.data;

import java.io.Serializable;
import java.time.LocalDate;

import org.exception.NegativeQuantityException;

public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    private static volatile int nextProductId = 1;
    private static final Object productIdLock = new Object();

    private final int id;
    private final String name;
    private final double deliveryPrice;
    private final ProductCategory category;
    private final LocalDate expirationDate;
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

    public Product(String name, double deliveryPrice, ProductCategory category,
            LocalDate expirationDate, int quantity) {
        synchronized (productIdLock) {
            this.id = nextProductId++;
        }
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
        if (quantity < 0) {
            throw new NegativeQuantityException(quantity);
        }
        this.quantity = quantity;
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(expirationDate);
    }

    public boolean isNearExpiration(int daysThreshold) {
        return LocalDate.now().plusDays(daysThreshold).isAfter(expirationDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Product product = (Product) o;
        return id == product.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("%s (ID: %d, Price: %.2f, Quantity: %d, Expires: %s)",
                name, id, deliveryPrice, quantity, expirationDate);
    }

    public static void resetProductCounter() {
        synchronized (productIdLock) {
            nextProductId = 1;
        }
    }
}
