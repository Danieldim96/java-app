package org.service.impl;

import org.data.Product;
import org.exception.NegativePercentageException;
import org.exception.ProductNotFoundException;
import org.service.ProductService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductServiceImpl implements ProductService {
    private final Map<Integer, Product> products;
    private final int expirationThreshold;
    private final double expirationDiscount;
    private double totalDeliveryExpenses = 0.0;

    public ProductServiceImpl(int expirationThreshold, double expirationDiscount) {
        if (expirationDiscount < 0) {
            throw new NegativePercentageException(expirationDiscount);
        }
        this.products = new HashMap<>();
        this.expirationThreshold = expirationThreshold;
        this.expirationDiscount = expirationDiscount;
    }

    @Override
    public void addProduct(Product product) {
        products.put(product.getId(), product);
        totalDeliveryExpenses += product.getDeliveryPrice() * product.getQuantity();
    }

    @Override
    public Product getProduct(int id) {
        return products.get(id);
    }

    @Override
    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    @Override
    public void updateProductQuantity(int id, int newQuantity) {
        Product product = products.get(id);
        if (product != null) {
            product.setQuantity(newQuantity);
        }
    }

    @Override
    public boolean isProductExpired(int id) {
        Product product = products.get(id);
        if (product == null) {
            return false;
        }
        return LocalDate.now().isAfter(product.getExpirationDate());
    }

    @Override
    public boolean isProductNearExpiration(int id) {
        Product product = products.get(id);
        if (product == null) {
            return false;
        }
        return product.getExpirationDate().minusDays(expirationThreshold)
                .isBefore(LocalDate.now());
    }

    @Override
    public double calculateSellingPrice(int id, double markup) {
        if (markup < 0) {
            throw new NegativePercentageException(markup);
        }

        Product product = products.get(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }

        double basePrice = product.getDeliveryPrice();
        double priceWithMarkup = basePrice * (1 + markup);

        if (isProductNearExpiration(id)) {
            priceWithMarkup *= (1.0 - expirationDiscount);
        }

        return Math.round(priceWithMarkup * 100.0) / 100.0;
    }

    public double getTotalDeliveryExpenses() {
        return totalDeliveryExpenses;
    }
}
