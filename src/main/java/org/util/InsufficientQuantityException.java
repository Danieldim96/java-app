package org.util;

import org.model.Product;

public class InsufficientQuantityException extends Exception {
    private Product product;
    private int requestedQuantity;

    public InsufficientQuantityException(Product product, int requestedQuantity) {
        super(String.format("Insufficient quantity for product %s. Requested: %d, Available: %d",
                product.getName(), requestedQuantity, product.getQuantity()));
        this.product = product;
        this.requestedQuantity = requestedQuantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }
} 