package org.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.model.*;
import org.service.impl.StoreServiceImpl;
import org.exception.InsufficientQuantityException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Contract tests for StoreService interface.
 * These tests verify the business logic and contract compliance
 * regardless of the specific implementation.
 */
public class StoreServiceTest {
    private StoreService storeService;
    private Product testProduct;
    private Cashier testCashier;

    @BeforeEach
    void setUp() {
        // Using the concrete implementation for testing the interface contract
        storeService = new StoreServiceImpl(20.0, 30.0, 7, 15.0);
        
        testProduct = new Product(1, "Test Product", 10.0, ProductCategory.FOOD, 
            LocalDate.now().plusDays(10), 5);
        testCashier = new Cashier(1, "Test Cashier", 2000.0);
        
        storeService.addProduct(testProduct);
        storeService.addCashier(testCashier);
        storeService.assignCashierToRegister(testCashier, 1);
    }

    @Test
    void shouldCalculateCorrectSellingPriceForFoodItems() {
        // Given a food product with 20% markup
        // When calculating selling price
        double price = storeService.calculateSellingPrice(testProduct);
        
        // Then price should be delivery price + 20% markup
        assertEquals(12.0, price, 0.01); // 10.0 * 1.2
    }

    @Test
    void shouldCalculateCorrectSellingPriceForNonFoodItems() {
        // Given a non-food product with 30% markup
        Product nonFoodProduct = new Product(2, "Soap", 10.0, ProductCategory.NON_FOOD, 
            LocalDate.now().plusDays(365), 3);
        storeService.addProduct(nonFoodProduct);
        
        // When calculating selling price
        double price = storeService.calculateSellingPrice(nonFoodProduct);
        
        // Then price should be delivery price + 30% markup
        assertEquals(13.0, price, 0.01); // 10.0 * 1.3
    }

    @Test
    void shouldApplyExpirationDiscountWhenNearExpiration() {
        // Given a product near expiration (within 7 days)
        Product nearExpirationProduct = new Product(3, "Near Expiry", 10.0, ProductCategory.FOOD, 
            LocalDate.now().plusDays(5), 2);
        storeService.addProduct(nearExpirationProduct);
        
        // When calculating selling price
        double price = storeService.calculateSellingPrice(nearExpirationProduct);
        
        // Then price should include markup and expiration discount
        // 10.0 * 1.2 * 0.85 = 10.2
        assertEquals(10.2, price, 0.01);
    }

    @Test
    void shouldThrowExceptionForExpiredProducts() {
        // Given an expired product
        Product expiredProduct = new Product(4, "Expired", 10.0, ProductCategory.FOOD, 
            LocalDate.now().minusDays(1), 1);
        storeService.addProduct(expiredProduct);
        
        // When/Then calculating selling price should throw exception
        assertThrows(IllegalStateException.class, () -> {
            storeService.calculateSellingPrice(expiredProduct);
        });
    }

    @Test
    void shouldCreateSaleSuccessfullyWithSufficientQuantity() throws InsufficientQuantityException {
        // Given sufficient product quantity
        Map<Integer, Integer> purchase = new HashMap<>();
        purchase.put(1, 2); // 2 units of test product (available: 5)
        
        // When creating sale
        Receipt receipt = storeService.createSale(1, purchase);
        
        // Then receipt should be created and product quantity updated
        assertNotNull(receipt);
        assertEquals(3, testProduct.getQuantity()); // 5 - 2 = 3
    }

    @Test
    void shouldThrowExceptionForInsufficientQuantity() {
        // Given insufficient product quantity
        Map<Integer, Integer> purchase = new HashMap<>();
        purchase.put(1, 10); // 10 units requested, only 5 available
        
        // When/Then creating sale should throw exception
        InsufficientQuantityException exception = assertThrows(InsufficientQuantityException.class, () -> {
            storeService.createSale(1, purchase);
        });
        
        assertEquals(testProduct, exception.getProduct());
        assertEquals(10, exception.getRequestedQuantity());
    }

    @Test
    void shouldTrackTotalRevenueAndReceipts() throws InsufficientQuantityException {
        // Given initial state
        int initialReceipts = storeService.getTotalReceipts();
        double initialRevenue = storeService.getTotalRevenue();
        
        // When making a sale
        Map<Integer, Integer> purchase = new HashMap<>();
        purchase.put(1, 1);
        storeService.createSale(1, purchase);
        
        // Then totals should be updated
        assertEquals(initialReceipts + 1, storeService.getTotalReceipts());
        assertTrue(storeService.getTotalRevenue() > initialRevenue);
    }

    @Test
    void shouldCalculateFinancialsCorrectly() {
        double salaryExpenses = storeService.getSalaryExpenses();
        double deliveryExpenses = storeService.getDeliveryExpenses();
        double income = storeService.getIncome();
        double profit = storeService.getProfit();
        
        assertTrue(salaryExpenses >= 0);
        assertTrue(deliveryExpenses >= 0);
        assertTrue(income >= 0);
        assertEquals(income - salaryExpenses - deliveryExpenses, profit, 0.01);
    }

    @Test
    void shouldNotAllowAssigningCashierToOccupiedRegister() {
        Cashier newCashier = new Cashier(2, "New Cashier", 1800.0);
        storeService.addCashier(newCashier);
        
        assertThrows(IllegalStateException.class, () -> {
            storeService.assignCashierToRegister(newCashier, 1);
        });
    }
} 