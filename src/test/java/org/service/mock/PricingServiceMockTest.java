package org.service.mock;

import org.data.Product;
import org.data.ProductCategory;
import org.exception.NegativePercentageException;
import org.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.service.ProductService;
import org.service.impl.PricingServiceImpl;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.mockito.Mockito;

public class PricingServiceMockTest {
    private ProductService productService;
    private PricingServiceImpl pricingService;
    private Product mockProduct;
    private static final int EXPIRATION_THRESHOLD = 7;
    private static final double EXPIRATION_DISCOUNT = 0.15;

    @BeforeEach
    void setUp() {
        productService = org.mockito.Mockito.mock(ProductService.class);
        mockProduct = org.mockito.Mockito.mock(Product.class);
        pricingService = new PricingServiceImpl(productService, EXPIRATION_THRESHOLD, EXPIRATION_DISCOUNT);
        org.mockito.Mockito.reset(productService, mockProduct);
    }

    @Test
    void testCalculateSellingPriceWithMock() throws ProductNotFoundException, NegativePercentageException {
        org.mockito.Mockito.when(productService.getProduct(1)).thenReturn(mockProduct);
        org.mockito.Mockito.when(mockProduct.getDeliveryPrice()).thenReturn(10.0);
        org.mockito.Mockito.when(mockProduct.getCategory()).thenReturn(ProductCategory.FOOD);
        org.mockito.Mockito.when(mockProduct.getExpirationDate()).thenReturn(LocalDate.now().plusDays(10));
        double price = pricingService.calculateSellingPrice(1, 0.2);
        assertEquals(12.0, price, 0.01);
        Mockito.verify(productService, Mockito.atLeastOnce()).getProduct(1);
    }

    @Test
    void testCalculateSellingPriceWithExpirationDiscount() throws ProductNotFoundException, NegativePercentageException {
        org.mockito.Mockito.when(productService.getProduct(1)).thenReturn(mockProduct);
        org.mockito.Mockito.when(mockProduct.getDeliveryPrice()).thenReturn(10.0);
        org.mockito.Mockito.when(mockProduct.getCategory()).thenReturn(ProductCategory.FOOD);
        org.mockito.Mockito.when(mockProduct.getExpirationDate()).thenReturn(LocalDate.now().plusDays(EXPIRATION_THRESHOLD - 1));
        double price = pricingService.calculateSellingPrice(1, 0.2);
        double expectedPrice = 10.0 * (1 + 0.2) * (1 - EXPIRATION_DISCOUNT);
        assertEquals(expectedPrice, price, 0.01);
    }

    @Test
    void testCalculateSellingPriceWithNegativeMarkup() {
        org.mockito.Mockito.when(productService.getProduct(1)).thenReturn(mockProduct);
        org.mockito.Mockito.when(mockProduct.getDeliveryPrice()).thenReturn(10.0);
        assertThrows(NegativePercentageException.class, () -> pricingService.calculateSellingPrice(1, -0.2));
    }

    @Test
    void testCalculateSellingPriceWithNonExistentProduct() {
        org.mockito.Mockito.when(productService.getProduct(999)).thenReturn(null);
        assertThrows(ProductNotFoundException.class, () -> pricingService.calculateSellingPrice(999, 0.2));
    }

    @Test
    void testIsProductExpired() {
        org.mockito.Mockito.when(productService.getProduct(1)).thenReturn(mockProduct);
        org.mockito.Mockito.when(mockProduct.getExpirationDate()).thenReturn(LocalDate.now().minusDays(1));
        boolean isExpired = pricingService.isProductExpired(1);
        assertTrue(isExpired);
    }

    @Test
    void testIsProductNearExpiration() {
        org.mockito.Mockito.when(productService.getProduct(1)).thenReturn(mockProduct);
        org.mockito.Mockito.when(mockProduct.getExpirationDate()).thenReturn(LocalDate.now().plusDays(EXPIRATION_THRESHOLD - 1));
        boolean isNearExpiration = pricingService.isProductNearExpiration(1);
        assertTrue(isNearExpiration);
    }
} 