package org.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.model.*;
import org.service.StoreService;
import org.service.impl.StoreServiceImpl;
import org.exception.InsufficientQuantityException;

import java.io.File;
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
    
    @BeforeEach
    void setUp() {
        store = new StoreServiceImpl(
            25.0,  // 25% markup for food
            40.0,  // 40% markup for non-food
            5,     // 5 days threshold for expiration discount
            20.0   // 20% discount for near-expiration items
        );
    }

    @Test
    void testCompleteStoreOperationsWorkflow() throws InsufficientQuantityException {
        // === SETUP PHASE ===
        // Add products to inventory
        Product milk = new Product(1, "Fresh Milk", 3.0, ProductCategory.FOOD, 
            LocalDate.now().plusDays(7), 20);
        Product bread = new Product(2, "Whole Bread", 2.5, ProductCategory.FOOD, 
            LocalDate.now().plusDays(3), 15); // Near expiration
        Product soap = new Product(3, "Hand Soap", 4.0, ProductCategory.NON_FOOD, 
            LocalDate.now().plusDays(365), 10);
        Product cheese = new Product(4, "Cheddar Cheese", 5.0, ProductCategory.FOOD, 
            LocalDate.now().plusDays(14), 8);
        
        store.addProduct(milk);
        store.addProduct(bread);
        store.addProduct(soap);
        store.addProduct(cheese);
        
        // Add cashiers and assign to registers
        Cashier alice = new Cashier(1, "Alice Johnson", 2200.0);
        Cashier bob = new Cashier(2, "Bob Smith", 2000.0);
        
        store.addCashier(alice);
        store.addCashier(bob);
        store.assignCashierToRegister(alice, 1);
        store.assignCashierToRegister(bob, 2);
        
        // === BUSINESS OPERATIONS PHASE ===
        
        // First customer - normal purchase
        Map<Integer, Integer> purchase1 = new HashMap<>();
        purchase1.put(1, 3); // 3 milk
        purchase1.put(3, 2); // 2 soap
        purchase1.put(4, 1); // 1 cheese
        
        Receipt receipt1 = store.createSale(1, purchase1);
        assertNotNull(receipt1);
        assertEquals(alice, receipt1.getCashier());
        
        // Verify pricing calculations
        double milkPrice = 3.0 * 1.25; // 25% markup = 3.75
        double soapPrice = 4.0 * 1.40; // 40% markup = 5.60
        double cheesePrice = 5.0 * 1.25; // 25% markup = 6.25
        double expectedTotal1 = milkPrice * 3 + soapPrice * 2 + cheesePrice * 1;
        
        assertEquals(expectedTotal1, receipt1.getTotalAmount(), 0.01);
        
        // Second customer - purchase includes near-expiration item
        Map<Integer, Integer> purchase2 = new HashMap<>();
        purchase2.put(2, 5); // 5 bread (near expiration - should get discount)
        purchase2.put(1, 2); // 2 milk
        
        Receipt receipt2 = store.createSale(2, purchase2);
        assertNotNull(receipt2);
        assertEquals(bob, receipt2.getCashier());
        
        // Verify near-expiration discount applied
        double breadPriceWithDiscount = 2.5 * 1.25 * 0.8; // 25% markup, 20% discount = 2.50
        double expectedTotal2 = breadPriceWithDiscount * 5 + milkPrice * 2;
        
        assertEquals(expectedTotal2, receipt2.getTotalAmount(), 0.01);
        
        // === INVENTORY VERIFICATION ===
        assertEquals(15, milk.getQuantity()); // 20 - 3 - 2 = 15
        assertEquals(10, bread.getQuantity()); // 15 - 5 = 10
        assertEquals(8, soap.getQuantity()); // 10 - 2 = 8
        assertEquals(7, cheese.getQuantity()); // 8 - 1 = 7
        
        // === FINANCIAL VERIFICATION ===
        assertEquals(2, store.getReceipts().size()); // Check store-specific receipts
        double expectedRevenue = expectedTotal1 + expectedTotal2;
        
        // Calculate revenue from store's receipts instead of global static counter
        double actualRevenue = store.getReceipts().stream()
            .mapToDouble(Receipt::getTotalAmount)
            .sum();
        assertEquals(expectedRevenue, actualRevenue, 0.01);
        
        // Salary expenses
        assertEquals(4200.0, store.getSalaryExpenses(), 0.01); // 2200 + 2000
        
        // Delivery expenses (all products delivered + sold)
        double deliveryExpenses = store.getDeliveryExpenses();
        assertTrue(deliveryExpenses > 0);
        
        // Profit calculation
        double profit = store.getProfit();
        assertEquals(store.getIncome() - store.getSalaryExpenses() - store.getDeliveryExpenses(), 
            profit, 0.01);
        
        // === FILE GENERATION VERIFICATION ===
        // Verify receipt files were created
        String txtFile1 = "output/receipts/receipt_" + receipt1.getReceiptNumber() + ".txt";
        String serFile1 = "output/receipts/receipt_" + receipt1.getReceiptNumber() + ".ser";
        String txtFile2 = "output/receipts/receipt_" + receipt2.getReceiptNumber() + ".txt";
        String serFile2 = "output/receipts/receipt_" + receipt2.getReceiptNumber() + ".ser";
        
        assertTrue(new File(txtFile1).exists());
        assertTrue(new File(serFile1).exists());
        assertTrue(new File(txtFile2).exists());
        assertTrue(new File(serFile2).exists());
        
        // Cleanup
        new File(txtFile1).delete();
        new File(serFile1).delete();
        new File(txtFile2).delete();
        new File(serFile2).delete();
    }

    @Test
    void testBusinessRulesEnforcement() {
        // Add test data
        Product expiredProduct = new Product(10, "Expired Yogurt", 2.0, ProductCategory.FOOD, 
            LocalDate.now().minusDays(1), 5);
        Product lowStockProduct = new Product(11, "Limited Chips", 3.0, ProductCategory.NON_FOOD, 
            LocalDate.now().plusDays(30), 2);
        
        store.addProduct(expiredProduct);
        store.addProduct(lowStockProduct);
        
        Cashier cashier = new Cashier(1, "Test Cashier", 2000.0);
        store.addCashier(cashier);
        store.assignCashierToRegister(cashier, 1);
        
        // Test 1: Cannot sell expired products
        assertThrows(IllegalStateException.class, () -> {
            store.calculateSellingPrice(expiredProduct);
        });
        
        // Test 2: Cannot sell more than available quantity
        Map<Integer, Integer> oversizedPurchase = new HashMap<>();
        oversizedPurchase.put(11, 5); // Try to buy 5, only 2 available
        
        InsufficientQuantityException exception = assertThrows(InsufficientQuantityException.class, () -> {
            store.createSale(1, oversizedPurchase);
        });
        
        assertEquals(lowStockProduct, exception.getProduct());
        assertEquals(5, exception.getRequestedQuantity());
        
        // Test 3: Cannot assign cashier to occupied register
        Cashier anotherCashier = new Cashier(2, "Another Cashier", 1800.0);
        store.addCashier(anotherCashier);
        
        assertThrows(IllegalStateException.class, () -> {
            store.assignCashierToRegister(anotherCashier, 1); // Register 1 already taken
        });
        
        // Test 4: Cannot create sale at unassigned register
        assertThrows(IllegalStateException.class, () -> {
            Map<Integer, Integer> purchase = new HashMap<>();
            purchase.put(11, 1);
            store.createSale(3, purchase); // Register 3 not assigned
        });
    }

    @Test
    void testConcurrentOperationsSimulation() throws InsufficientQuantityException {
        // Simulate multiple cashiers working simultaneously
        Product popularItem = new Product(20, "Popular Snack", 1.0, ProductCategory.NON_FOOD, 
            LocalDate.now().plusDays(60), 100);
        store.addProduct(popularItem);
        
        Cashier cashier1 = new Cashier(1, "Fast Cashier", 2100.0);
        Cashier cashier2 = new Cashier(2, "Careful Cashier", 2300.0);
        Cashier cashier3 = new Cashier(3, "New Cashier", 1900.0);
        
        store.addCashier(cashier1);
        store.addCashier(cashier2);
        store.addCashier(cashier3);
        
        store.assignCashierToRegister(cashier1, 1);
        store.assignCashierToRegister(cashier2, 2);
        store.assignCashierToRegister(cashier3, 3);
        
        // Simulate sales from different registers
        Map<Integer, Integer> sale1 = new HashMap<>();
        sale1.put(20, 15);
        
        Map<Integer, Integer> sale2 = new HashMap<>();
        sale2.put(20, 20);
        
        Map<Integer, Integer> sale3 = new HashMap<>();
        sale3.put(20, 10);
        
        Receipt receipt1 = store.createSale(1, sale1);
        Receipt receipt2 = store.createSale(2, sale2);
        Receipt receipt3 = store.createSale(3, sale3);
        
        // Verify all sales processed correctly
        assertNotNull(receipt1);
        assertNotNull(receipt2);
        assertNotNull(receipt3);
        
        assertEquals(cashier1, receipt1.getCashier());
        assertEquals(cashier2, receipt2.getCashier());
        assertEquals(cashier3, receipt3.getCashier());
        
        // Verify inventory updated correctly
        assertEquals(55, popularItem.getQuantity()); // 100 - 15 - 20 - 10 = 55
        
        // Verify financial totals
        assertEquals(3, store.getReceipts().size()); // Check store-specific receipts, not global static counter
        
        // Calculate revenue from store's receipts
        double actualRevenue = store.getReceipts().stream()
            .mapToDouble(Receipt::getTotalAmount)
            .sum();
        assertTrue(actualRevenue > 0);
        assertEquals(6300.0, store.getSalaryExpenses(), 0.01); // 2100 + 2300 + 1900
        
        // Cleanup receipt files
        cleanupReceiptFiles(receipt1, receipt2, receipt3);
    }
    
    private void cleanupReceiptFiles(Receipt... receipts) {
        for (Receipt receipt : receipts) {
            String txtFile = "output/receipts/receipt_" + receipt.getReceiptNumber() + ".txt";
            String serFile = "output/receipts/receipt_" + receipt.getReceiptNumber() + ".ser";
            new File(txtFile).delete();
            new File(serFile).delete();
        }
    }
} 