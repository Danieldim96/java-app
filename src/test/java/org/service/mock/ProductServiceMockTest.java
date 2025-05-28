package org.service.mock;

import org.data.Product;
import org.exception.NegativeQuantityException;
import org.exception.ProductNotFoundException;
import org.exception.NegativePercentageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.service.impl.ProductServiceImpl;
import org.service.PricingService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.mockito.Mockito;

public class ProductServiceMockTest {
    private PricingService pricingService;
    private ProductServiceImpl productService;
    private Product mockProduct;

    @BeforeEach
    void setUp() {
        pricingService = org.mockito.Mockito.mock(PricingService.class);
        productService = new ProductServiceImpl(pricingService);
        mockProduct = org.mockito.Mockito.mock(Product.class);
        org.mockito.Mockito.reset(pricingService, mockProduct);
    }

    @Test
    void testAddAndGetProduct() {
        org.mockito.Mockito.when(mockProduct.getId()).thenReturn(1);
        productService.addProduct(mockProduct);
        Product retrieved = productService.getProduct(1);
        assertNotNull(retrieved);
        assertEquals(1, retrieved.getId());
    }

    @Test
    void testGetAllProducts() {
        org.mockito.Mockito.when(mockProduct.getId()).thenReturn(1);
        productService.addProduct(mockProduct);
        List<Product> products = productService.getAllProducts();
        assertNotNull(products);
        assertEquals(1, products.size());
    }

    @Test
    void testUpdateProductQuantity() {
        org.mockito.Mockito.when(mockProduct.getId()).thenReturn(1);
        productService.addProduct(mockProduct);
        productService.updateProductQuantity(1, 10);
        org.mockito.Mockito.verify(mockProduct).setQuantity(10);
    }

    @Test
    void testUpdateProductQuantityWithNegativeValue() {
        org.mockito.Mockito.when(mockProduct.getId()).thenReturn(1);
        org.mockito.Mockito.doThrow(new NegativeQuantityException(-5)).when(mockProduct).setQuantity(-5);
        productService.addProduct(mockProduct);
        assertThrows(NegativeQuantityException.class, () -> productService.updateProductQuantity(1, -5));
    }

    @Test
    void testGetNonExistentProduct() {
        assertNull(productService.getProduct(999));
    }

    @Test
    void testIsProductExpired() {
        org.mockito.Mockito.when(mockProduct.getId()).thenReturn(1);
        org.mockito.Mockito.when(pricingService.isProductExpired(1)).thenReturn(true);
        productService.addProduct(mockProduct);
        assertTrue(productService.isProductExpired(1));
    }

    @Test
    void testIsProductNearExpiration() {
        org.mockito.Mockito.when(mockProduct.getId()).thenReturn(1);
        org.mockito.Mockito.when(pricingService.isProductNearExpiration(1)).thenReturn(true);
        productService.addProduct(mockProduct);
        assertTrue(productService.isProductNearExpiration(1));
    }

    @Test
    void testCalculateSellingPriceThrowsForNonExistentProduct() {
        Mockito.when(pricingService.calculateSellingPrice(999, 0.2)).thenThrow(new ProductNotFoundException(999));
        assertThrows(ProductNotFoundException.class, () -> productService.calculateSellingPrice(999, 0.2));
    }

    @Test
    void testCalculateSellingPrice() throws ProductNotFoundException, NegativePercentageException {
        org.mockito.Mockito.when(mockProduct.getId()).thenReturn(1);
        org.mockito.Mockito.when(pricingService.calculateSellingPrice(1, 0.2)).thenReturn(12.0);
        productService.addProduct(mockProduct);
        double price = productService.calculateSellingPrice(1, 0.2);
        assertEquals(12.0, price, 0.01);
    }

    @Test
    void testCalculateSellingPriceWithExpirationDiscount() throws ProductNotFoundException, NegativePercentageException {
        org.mockito.Mockito.when(mockProduct.getId()).thenReturn(1);
        double expectedPrice = 10.0 * (1 + 0.2) * (1 - 0.15);
        org.mockito.Mockito.when(pricingService.calculateSellingPrice(1, 0.2)).thenReturn(expectedPrice);
        productService.addProduct(mockProduct);
        double price = productService.calculateSellingPrice(1, 0.2);
        assertEquals(expectedPrice, price, 0.01);
    }
} 