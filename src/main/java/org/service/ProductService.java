package org.service;

import org.data.Product;
import java.util.List;

public interface ProductService {
    /**
     * Add a new product to the inventory
     * @param product The product to add
     */
    void addProduct(Product product);

    /**
     * Get a product by its ID
     * @param id The product ID
     * @return The product, or null if not found
     */
    Product getProduct(int id);

    /**
     * Get all products in the inventory
     * @return A list of all products
     */
    List<Product> getAllProducts();

    /**
     * Update the quantity of a product
     * @param id The product ID
     * @param newQuantity The new quantity
     */
    void updateProductQuantity(int id, int newQuantity);

    boolean isProductExpired(int id);

    boolean isProductNearExpiration(int id);

    double calculateSellingPrice(int id, double markup);

    double getTotalDeliveryExpenses();
}
