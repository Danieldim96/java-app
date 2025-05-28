package org.service;

import org.data.Product;
import org.exception.NegativePercentageException;
import org.exception.ProductNotFoundException;

public interface PricingService {
    /**
     * Calculate the selling price for a product with the given markup
     * @param productId The ID of the product
     * @param markup The markup percentage (e.g., 0.2 for 20%)
     * @return The calculated selling price
     * @throws ProductNotFoundException if the product doesn't exist
     * @throws NegativePercentageException if the markup is negative
     */
    double calculateSellingPrice(int productId, double markup) throws ProductNotFoundException, NegativePercentageException;
    
    /**
     * Get the total delivery expenses for all products
     * @return The total delivery expenses
     */
    double getTotalDeliveryExpenses();
    
    /**
     * Check if a product is near expiration
     * @param productId The ID of the product
     * @return true if the product is near expiration, false otherwise
     */
    boolean isProductNearExpiration(int productId);
    
    /**
     * Check if a product is expired
     * @param productId The ID of the product
     * @return true if the product is expired, false otherwise
     */
    boolean isProductExpired(int productId);
} 