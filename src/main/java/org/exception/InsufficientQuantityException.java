package org.exception;

import org.data.Product;

public class InsufficientQuantityException extends RuntimeException {
    private final Product product;
    private final int requestedQuantity;

    public InsufficientQuantityException(Product product, int requestedQuantity) {
        super(String.format("Insufficient quantity for %s. Requested: %d, Available: %d",
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