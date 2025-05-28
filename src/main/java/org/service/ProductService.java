package org.service;

import org.data.Product;
import java.util.List;

public interface ProductService {
    void addProduct(Product product);

    Product getProduct(int id);

    List<Product> getAllProducts();

    void updateProductQuantity(int productId, int quantity);

    boolean isProductNearExpiration(int productId);

    boolean isProductExpired(int productId);

    double calculateSellingPrice(int productId, double markup);
}
