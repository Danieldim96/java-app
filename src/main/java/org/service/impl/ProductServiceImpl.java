package org.service.impl;

import org.data.Product;
import org.service.ProductService;
import org.service.PricingService;
import org.exception.NegativePercentageException;
import org.exception.ProductNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductServiceImpl implements ProductService {
    private final Map<Integer, Product> products;
    private final PricingService pricingService;

    public ProductServiceImpl() {
        this.products = new HashMap<>();
        this.pricingService = new PricingServiceImpl(this, 7, 0.2); // Default values
    }

    public ProductServiceImpl(int expirationThreshold, double expirationDiscount) {
        this.products = new HashMap<>();
        this.pricingService = new PricingServiceImpl(this, expirationThreshold, expirationDiscount);
    }

    public ProductServiceImpl(PricingService pricingService) {
        this.products = new HashMap<>();
        this.pricingService = pricingService;
    }

    @Override
    public void addProduct(Product product) {
        products.put(product.getId(), product);
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
        return pricingService.isProductExpired(id);
    }

    @Override
    public boolean isProductNearExpiration(int id) {
        return pricingService.isProductNearExpiration(id);
    }

    @Override
    public double calculateSellingPrice(int id, double markup) throws ProductNotFoundException, NegativePercentageException {
        return pricingService.calculateSellingPrice(id, markup);
    }

    @Override
    public double getTotalDeliveryExpenses() {
        return pricingService.getTotalDeliveryExpenses();
    }
}
