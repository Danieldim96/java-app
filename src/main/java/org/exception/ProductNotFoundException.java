package org.exception;

public class ProductNotFoundException extends RuntimeException {
    private final int productId;

    public ProductNotFoundException(int productId) {
        super(String.format("Product not found: %d", productId));
        this.productId = productId;
    }

    public int getProductId() {
        return productId;
    }
}