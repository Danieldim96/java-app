package org.service;

import org.data.Product;
import org.data.ProductCategory;
import org.service.impl.ProductServiceImpl;
import org.service.impl.StoreServiceImpl;
import org.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProductServiceTest {
    private ProductService productService;
    private static final int EXPIRATION_THRESHOLD = 7;
    private static final double EXPIRATION_DISCOUNT = 0.15;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(EXPIRATION_THRESHOLD, EXPIRATION_DISCOUNT);
    }

    @Nested
    class ProductManagementTests {
        @Test
        void testAddAndGetProduct() {
            Product product = new Product("Test Product", 10.0, ProductCategory.FOOD,
                    LocalDate.now().plusDays(10), 5);
            productService.addProduct(product);

            Product retrieved = productService.getProduct(product.getId());
            assertNotNull(retrieved);
            assertEquals(product.getId(), retrieved.getId());
            assertEquals(product.getName(), retrieved.getName());
        }

        @Test
        void testGetAllProducts() {
            Product product1 = new Product("Product 1", 10.0, ProductCategory.FOOD,
                    LocalDate.now().plusDays(10), 5);
            Product product2 = new Product("Product 2", 20.0, ProductCategory.NON_FOOD,
                    LocalDate.now().plusDays(20), 10);

            productService.addProduct(product1);
            productService.addProduct(product2);

            List<Product> products = productService.getAllProducts();
            assertEquals(2, products.size());
            assertTrue(products.stream().anyMatch(p -> p.getName().equals("Product 1")));
            assertTrue(products.stream().anyMatch(p -> p.getName().equals("Product 2")));
        }

        @Test
        void testUpdateProductQuantity() {
            Product product = new Product("Test Product", 10.0, ProductCategory.FOOD,
                    LocalDate.now().plusDays(10), 5);
            productService.addProduct(product);

            productService.updateProductQuantity(product.getId(), 10);
            Product updated = productService.getProduct(product.getId());
            assertEquals(10, updated.getQuantity());
        }
    }

    @Nested
    class ExpirationTests {
        @Test
        void testProductExpiration() {
            Product expiredProduct = new Product("Expired", 10.0, ProductCategory.FOOD,
                    LocalDate.now().minusDays(1), 5);
            Product validProduct = new Product("Valid", 10.0, ProductCategory.FOOD,
                    LocalDate.now().plusDays(10), 5);

            productService.addProduct(expiredProduct);
            productService.addProduct(validProduct);

            assertTrue(productService.isProductExpired(expiredProduct.getId()));
            assertFalse(productService.isProductExpired(validProduct.getId()));
        }

        @Test
        void testNearExpiration() {
            Product nearExpirationProduct = new Product("Near Expiration", 10.0, ProductCategory.FOOD,
                    LocalDate.now().plusDays(EXPIRATION_THRESHOLD - 1), 5);
            Product farExpirationProduct = new Product("Far Expiration", 10.0, ProductCategory.FOOD,
                    LocalDate.now().plusDays(EXPIRATION_THRESHOLD + 1), 5);

            productService.addProduct(nearExpirationProduct);
            productService.addProduct(farExpirationProduct);

            assertTrue(productService.isProductNearExpiration(nearExpirationProduct.getId()));
            assertFalse(productService.isProductNearExpiration(farExpirationProduct.getId()));
        }
    }

    @Nested
    class PricingTests {
        @Test
        void testCalculateSellingPrice() {
            Product product = new Product("Test Product", 10.0, ProductCategory.FOOD,
                    LocalDate.now().plusDays(10), 5);
            productService.addProduct(product);

            double markup = 0.20; // 20% markup
            double expectedPrice = 10.0 * (1 + markup);
            double actualPrice = productService.calculateSellingPrice(product.getId(), markup);

            assertEquals(expectedPrice, actualPrice, 0.01);
        }

        @Test
        void testCalculateSellingPriceWithExpirationDiscount() {
            Product product = new Product("Test Product", 10.0, ProductCategory.FOOD,
                    LocalDate.now().plusDays(EXPIRATION_THRESHOLD - 1), 5);
            productService.addProduct(product);

            double markup = 0.20; // 20% markup
            double expectedPrice = 10.0 * (1 + markup) * (1 - EXPIRATION_DISCOUNT);
            double actualPrice = productService.calculateSellingPrice(product.getId(), markup);

            assertEquals(expectedPrice, actualPrice, 0.01);
        }

        @Test
        void testGetTotalDeliveryExpenses() {
            StoreServiceImpl store = new StoreServiceImpl(0.20, 0.30, EXPIRATION_THRESHOLD, EXPIRATION_DISCOUNT);
            Product product1 = new Product("Product 1", 10.0, ProductCategory.FOOD,
                    LocalDate.now().plusDays(10), 5);
            Product product2 = new Product("Product 2", 20.0, ProductCategory.NON_FOOD,
                    LocalDate.now().plusDays(20), 10);

            store.addProduct(product1);
            store.addProduct(product2);

            double expectedExpenses = (10.0 * 5) + (20.0 * 10);
            assertEquals(expectedExpenses, store.getDeliveryExpenses(), 0.01);
        }
    }

    @Nested
    class ErrorHandlingTests {
        @Test
        void testGetNonExistentProduct() {
            assertNull(productService.getProduct(999));
        }

        @Test
        void testUpdateNonExistentProduct() {
            productService.updateProductQuantity(999, 10); // Should not throw exception
        }

        @Test
        void testCalculatePriceForNonExistentProduct() {
            assertThrows(ProductNotFoundException.class, () -> {
                productService.calculateSellingPrice(999, 0.20);
            });
        }
    }
}
