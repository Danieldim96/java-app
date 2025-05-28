package org.service.impl;

import org.data.Product;
import org.exception.NegativePercentageException;
import org.exception.ProductNotFoundException;
import org.service.PricingService;
import org.service.ProductService;

import java.time.LocalDate;
import java.util.Map;

public class PricingServiceImpl implements PricingService {
    private final ProductService productService;
    private final int expirationThreshold;
    private final double expirationDiscount;
    private double totalDeliveryExpenses = 0.0;

    public PricingServiceImpl(ProductService productService, int expirationThreshold, double expirationDiscount) {
        if (expirationDiscount < 0) {
            throw new NegativePercentageException(expirationDiscount);
        }
        this.productService = productService;
        this.expirationThreshold = expirationThreshold;
        this.expirationDiscount = expirationDiscount;
    }

    @Override
    public double calculateSellingPrice(int productId, double markup) throws ProductNotFoundException, NegativePercentageException {
        if (markup < 0) {
            throw new NegativePercentageException(markup);
        }

        Product product = productService.getProduct(productId);
        if (product == null) {
            throw new ProductNotFoundException(productId);
        }

        double basePrice = product.getDeliveryPrice();
        double priceWithMarkup = basePrice * (1 + markup);

        if (isProductNearExpiration(productId)) {
            priceWithMarkup *= (1.0 - expirationDiscount);
        }

        return Math.round(priceWithMarkup * 100.0) / 100.0;
    }

    @Override
    public double getTotalDeliveryExpenses() {
        return totalDeliveryExpenses;
    }

    @Override
    public boolean isProductNearExpiration(int productId) {
        Product product = productService.getProduct(productId);
        if (product == null) {
            return false;
        }
        return product.getExpirationDate().minusDays(expirationThreshold)
                .isBefore(LocalDate.now());
    }

    @Override
    public boolean isProductExpired(int productId) {
        Product product = productService.getProduct(productId);
        if (product == null) {
            return false;
        }
        return LocalDate.now().isAfter(product.getExpirationDate());
    }

    /**
     * Update the total delivery expenses when a new product is added
     * @param product The product to add to the delivery expenses
     */
    public void addProductDeliveryExpense(Product product) {
        totalDeliveryExpenses += product.getDeliveryPrice() * product.getQuantity();
    }
} 