package org.exception;

import org.data.Product;

public class ExpiredProductException extends RuntimeException {
    private final Product product;

    public ExpiredProductException(Product product) {
        super(String.format("Cannot sell expired product: %s", product.getName()));
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }
}