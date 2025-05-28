package org.service;

import org.data.Product;
import org.data.ProductCategory;
import org.exception.NegativePercentageException;
import org.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(7, 0.15);

        Product.resetProductCounter();

        Product milk = new Product("Milk", 2.0, ProductCategory.FOOD,
                LocalDate.now().plusDays(10), 10);
        Product bread = new Product("Bread", 1.5, ProductCategory.FOOD,
                LocalDate.now().plusDays(3), 15);

        productService.addProduct(milk);
        productService.addProduct(bread);
    }

    @Test
    void testAddAndGetProduct() {
        // Since we reset the counter, milk should have ID 1
        Product retrievedMilk = productService.getProduct(1);
        assertNotNull(retrievedMilk);
        assertEquals("Milk", retrievedMilk.getName());
        assertEquals(2.0, retrievedMilk.getDeliveryPrice());
    }

    @Test
    void testUpdateProductQuantity() {
        // Since we reset the counter, milk should have ID 1
        productService.updateProductQuantity(1, 5);
        Product updatedMilk = productService.getProduct(1);
        assertEquals(5, updatedMilk.getQuantity());
    }

    @Test
    void testIsProductNearExpiration() {
        assertFalse(productService.isProductNearExpiration(1));
        assertTrue(productService.isProductNearExpiration(2));
    }

    @Test
    void testCalculateSellingPrice() {
        double milkPrice = productService.calculateSellingPrice(1, 0.20);
        assertEquals(2.4, milkPrice, 0.001);

        double breadPrice = productService.calculateSellingPrice(2, 0.20);
        assertEquals(1.53, breadPrice, 0.001);
    }

    @Test
    void testNegativeExpirationDiscount() {
        Exception exception = assertThrows(NegativePercentageException.class, () -> {
            new ProductServiceImpl(7, -0.15);
        });

        assertTrue(exception.getMessage().contains("Negative percentage value is not allowed"));
    }

    @Test
    void testNegativeMarkup() {
        Exception exception = assertThrows(NegativePercentageException.class, () -> {
            productService.calculateSellingPrice(1, -0.20);
        });

        assertTrue(exception.getMessage().contains("Negative percentage value is not allowed"));
    }
}
