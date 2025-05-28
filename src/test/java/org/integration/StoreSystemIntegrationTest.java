package org.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.data.*;
import org.service.StoreService;
import org.service.ProductService;
import org.service.CashierService;
import org.service.ReceiptService;
import org.service.impl.StoreServiceImpl;
import org.service.impl.ProductServiceImpl;
import org.service.impl.CashierServiceImpl;
import org.service.impl.ReceiptServiceImpl;
import org.config.StoreConfig;
import org.exception.*;
import org.service.PricingService;
import org.service.impl.PricingServiceImpl;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Integration tests for the entire store management system.
 * These tests verify that all components work together correctly
 * in realistic business scenarios.
 */
public class StoreSystemIntegrationTest {

    private StoreService store;
    private Store storeData;
    private StoreConfig config;
    private ProductService productService;
    private CashierService cashierService;
    private ReceiptService receiptService;

    @BeforeEach
    void setUp() {
        Receipt.resetReceiptCounter();
        Product.resetProductCounter();
        Cashier.resetCashierCounter();
        
        config = new StoreConfig();
        storeData = new Store("Test Store", "Test Address", 0.25, 0.40, 5, 0.20);
        productService = new ProductServiceImpl(5, 0.20);
        cashierService = new CashierServiceImpl();
        receiptService = new ReceiptServiceImpl(config);
        PricingService pricingService = new PricingServiceImpl(productService, 5, 0.20);
        store = new StoreServiceImpl(storeData, config, productService, cashierService, receiptService, pricingService);
    }

    @Test
    void testCompleteSaleWorkflow() throws InsufficientQuantityException {
        // Setup
        Product product = new Product("Test Product", 10.0, ProductCategory.FOOD,
                LocalDate.now().plusDays(10), 5);
        store.addProduct(product);

        Cashier cashier = new Cashier("Test Cashier", 2000.0);
        store.addCashier(cashier);
        store.assignCashierToRegister(cashier, 1);

        // Execute sale
        Map<Integer, Integer> purchase = new HashMap<>();
        purchase.put(product.getId(), 2);
        Receipt receipt = store.createSale(1, purchase);

        // Verify
        assertNotNull(receipt);
        assertEquals(1, receipt.getReceiptNumber());
        assertEquals(cashier, receipt.getCashier());
        assertTrue(receipt.getTotalAmount() > 0);

        // Verify inventory
        Product updatedProduct = store.getDeliveredProducts().stream()
                .filter(p -> p.getId() == product.getId())
                .findFirst()
                .orElse(null);
        assertNotNull(updatedProduct);
        assertEquals(3, updatedProduct.getQuantity());

        // Verify financials
        assertTrue(store.getTotalRevenue() > 0);
        System.out.println("Cashiers: " + store.getCashiers());
        double expectedSalary = store.getCashiers().stream().mapToDouble(Cashier::getSalary).sum();
        double actualSalary = store.getSalaryExpenses();
        System.out.println("Expected salary: " + expectedSalary);
        System.out.println("Actual salary: " + actualSalary);
        assertEquals(expectedSalary, actualSalary, 0.01, "Expected salary expenses: " + expectedSalary + ", but got: " + actualSalary);
        assertTrue(store.getDeliveryExpenses() > 0);
        assertEquals(store.getTotalRevenue() - store.getDeliveryExpenses(), store.getIncome(), 0.01);
        assertEquals(store.getIncome() - store.getSalaryExpenses(), store.getProfit(), 0.01);
    }

    @Test
    void testBusinessRulesEnforcement() {
        // Setup expired and low stock products
        Product expiredProduct = new Product("Expired Yogurt", 2.0, ProductCategory.FOOD,
                LocalDate.now().minusDays(1), 5);
        Product lowStockProduct = new Product("Limited Chips", 3.0, ProductCategory.NON_FOOD,
                LocalDate.now().plusDays(30), 2);

        store.addProduct(expiredProduct);
        store.addProduct(lowStockProduct);

        Cashier cashier = new Cashier("Test Cashier", 2000.0);
        store.addCashier(cashier);
        store.assignCashierToRegister(cashier, 1);

        // Test insufficient quantity
        Map<Integer, Integer> oversizedPurchase = new HashMap<>();
        oversizedPurchase.put(lowStockProduct.getId(), 5);

        InsufficientQuantityException quantityException = assertThrows(InsufficientQuantityException.class, () -> {
            store.createSale(1, oversizedPurchase);
        });

        assertEquals(lowStockProduct, quantityException.getProduct());
        assertEquals(5, quantityException.getRequestedQuantity());

        // Test register assignment
        Cashier anotherCashier = new Cashier("Another Cashier", 1800.0);
        store.addCashier(anotherCashier);

        assertThrows(RegisterAlreadyAssignedException.class, () -> {
            store.assignCashierToRegister(anotherCashier, 1);
        });

        // Test unassigned register
        assertThrows(NoAssignedCashierException.class, () -> {
            Map<Integer, Integer> purchase = new HashMap<>();
            purchase.put(expiredProduct.getId(), 1);
            store.createSale(3, purchase);
        });
    }

    @Test
    void testReceiptPersistence() throws InsufficientQuantityException, IOException, ClassNotFoundException {
        // Setup
        Product product = new Product("Test Product", 10.0, ProductCategory.FOOD,
                LocalDate.now().plusDays(10), 5);
        store.addProduct(product);

        Cashier cashier = new Cashier("Test Cashier", 2000.0);
        store.addCashier(cashier);
        store.assignCashierToRegister(cashier, 1);

        // Create sale
        Map<Integer, Integer> purchase = new HashMap<>();
        purchase.put(product.getId(), 1);
        Receipt receipt = store.createSale(1, purchase);

        // Verify files were created
        String txtFile = "output/receipts/receipt_" + receipt.getReceiptNumber() + ".txt";
        String serFile = "output/receipts/receipt_" + receipt.getReceiptNumber() + ".ser";

        assertTrue(new File(txtFile).exists());
        assertTrue(new File(serFile).exists());

        // Load and verify receipt
        Receipt loadedReceipt = store.loadReceiptFromFile(receipt.getReceiptNumber());
        assertNotNull(loadedReceipt);
        assertEquals(receipt.getReceiptNumber(), loadedReceipt.getReceiptNumber());
        assertEquals(receipt.getTotalAmount(), loadedReceipt.getTotalAmount(), 0.01);

        // Cleanup
        new File(txtFile).delete();
        new File(serFile).delete();
    }
}
