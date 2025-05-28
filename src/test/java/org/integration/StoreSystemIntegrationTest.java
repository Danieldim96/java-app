package org.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.data.*;
import org.service.StoreService;
import org.service.impl.StoreServiceImpl;
import org.exception.InsufficientQuantityException;
import org.exception.RegisterAlreadyAssignedException;
import org.exception.NoAssignedCashierException;
import org.data.Receipt;

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
        Receipt.resetReceiptCounter();
        Product.resetProductCounter();
        Cashier.resetCashierCounter();
        store = new StoreServiceImpl(
                0.25, // 25% markup for food
                0.40, // 40% markup for non-food
                5, // 5 days threshold for expiration discount
                0.20 // 20% discount for near-expiration items
        );
    }

    @Test
    void testCompleteStoreOperationsWorkflow() throws InsufficientQuantityException {
        Product milk = new Product("Fresh Milk", 3.0, ProductCategory.FOOD,
                LocalDate.now().plusDays(7), 20);
        Product bread = new Product("Whole Bread", 2.5, ProductCategory.FOOD,
                LocalDate.now().plusDays(3), 15); // Near expiration
        Product soap = new Product("Hand Soap", 4.0, ProductCategory.NON_FOOD,
                LocalDate.now().plusDays(365), 10);
        Product cheese = new Product("Cheddar Cheese", 5.0, ProductCategory.FOOD,
                LocalDate.now().plusDays(14), 8);

        store.addProduct(milk);
        store.addProduct(bread);
        store.addProduct(soap);
        store.addProduct(cheese);

        Cashier alice = new Cashier("Alice Johnson", 2200.0);
        Cashier bob = new Cashier("Bob Smith", 2000.0);

        store.addCashier(alice);
        store.addCashier(bob);
        store.assignCashierToRegister(alice, 1);
        store.assignCashierToRegister(bob, 2);

        Map<Integer, Integer> purchase1 = new HashMap<>();
        purchase1.put(milk.getId(), 3); // 3 milk
        purchase1.put(soap.getId(), 2); // 2 soap
        purchase1.put(cheese.getId(), 1); // 1 cheese

        Receipt receipt1 = store.createSale(1, purchase1);
        assertNotNull(receipt1);
        assertEquals(alice, receipt1.getCashier());

        double milkPrice = 3.0 * 1.25; // 25% markup = 3.75
        double soapPrice = 4.0 * 1.40; // 40% markup = 5.60
        double cheesePrice = 5.0 * 1.25; // 25% markup = 6.25
        double expectedTotal1 = milkPrice * 3 + soapPrice * 2 + cheesePrice * 1;

        assertEquals(expectedTotal1, receipt1.getTotalAmount(), 0.01);

        Map<Integer, Integer> purchase2 = new HashMap<>();
        purchase2.put(bread.getId(), 5); // 5 bread (near expiration - should get discount)
        purchase2.put(milk.getId(), 2); // 2 milk

        Receipt receipt2 = store.createSale(2, purchase2);
        assertNotNull(receipt2);
        assertEquals(bob, receipt2.getCashier());

        double breadPriceWithDiscount = 2.5 * 1.25 * 0.8; // 25% markup, 20% discount = 2.50
        double expectedTotal2 = breadPriceWithDiscount * 5 + milkPrice * 2;

        assertEquals(expectedTotal2, receipt2.getTotalAmount(), 0.01);

        assertEquals(15, milk.getQuantity()); // 20 - 3 - 2 = 15
        assertEquals(10, bread.getQuantity()); // 15 - 5 = 10
        assertEquals(8, soap.getQuantity()); // 10 - 2 = 8
        assertEquals(7, cheese.getQuantity()); // 8 - 1 = 7

        assertEquals(4200.0, store.getSalaryExpenses(), 0.01); // 2200 + 2000

        double deliveryExpenses = store.getDeliveryExpenses();
        assertTrue(deliveryExpenses > 0);

        double profit = store.getProfit();
        assertEquals(store.getIncome() - store.getSalaryExpenses(),
                profit, 0.01);

        String txtFile1 = "output/receipts/receipt_" + receipt1.getReceiptNumber() + ".txt";
        String serFile1 = "output/receipts/receipt_" + receipt1.getReceiptNumber() + ".ser";
        String txtFile2 = "output/receipts/receipt_" + receipt2.getReceiptNumber() + ".txt";
        String serFile2 = "output/receipts/receipt_" + receipt2.getReceiptNumber() + ".ser";

        assertTrue(new File(txtFile1).exists());
        assertTrue(new File(serFile1).exists());
        assertTrue(new File(txtFile2).exists());
        assertTrue(new File(serFile2).exists());

        new File(txtFile1).delete();
        new File(serFile1).delete();
        new File(txtFile2).delete();
        new File(serFile2).delete();
    }

    @Test
    void testBusinessRulesEnforcement() {
        Product expiredProduct = new Product("Expired Yogurt", 2.0, ProductCategory.FOOD,
                LocalDate.now().minusDays(1), 5);
        Product lowStockProduct = new Product("Limited Chips", 3.0, ProductCategory.NON_FOOD,
                LocalDate.now().plusDays(30), 2);

        store.addProduct(expiredProduct);
        store.addProduct(lowStockProduct);

        Cashier cashier = new Cashier("Test Cashier", 2000.0);
        store.addCashier(cashier);
        store.assignCashierToRegister(cashier, 1);

        Map<Integer, Integer> oversizedPurchase = new HashMap<>();
        oversizedPurchase.put(lowStockProduct.getId(), 5); // Try to buy 5, only 2 available

        InsufficientQuantityException exception = assertThrows(InsufficientQuantityException.class, () -> {
            store.createSale(1, oversizedPurchase);
        });

        assertEquals(lowStockProduct, exception.getProduct());
        assertEquals(5, exception.getRequestedQuantity());

        Cashier anotherCashier = new Cashier(2, "Another Cashier", 1800.0);
        store.addCashier(anotherCashier);

        assertThrows(RegisterAlreadyAssignedException.class, () -> {
            store.assignCashierToRegister(anotherCashier, 1); // Register 1 already taken
        });

        assertThrows(NoAssignedCashierException.class, () -> {
            Map<Integer, Integer> purchase = new HashMap<>();
            purchase.put(11, 1);
            store.createSale(3, purchase); // Register 3 not assigned
        });
    }

    @Test
    void testConcurrentOperationsSimulation() throws InsufficientQuantityException {
        Product popularItem = new Product("Popular Snack", 1.0, ProductCategory.NON_FOOD,
                LocalDate.now().plusDays(60), 100);
        store.addProduct(popularItem);

        Cashier cashier1 = new Cashier("Fast Cashier", 2100.0);
        Cashier cashier2 = new Cashier("Careful Cashier", 2300.0);
        Cashier cashier3 = new Cashier("New Cashier", 1900.0);

        store.addCashier(cashier1);
        store.addCashier(cashier2);
        store.addCashier(cashier3);

        store.assignCashierToRegister(cashier1, 1);
        store.assignCashierToRegister(cashier2, 2);
        store.assignCashierToRegister(cashier3, 3);

        Map<Integer, Integer> sale1 = new HashMap<>();
        sale1.put(popularItem.getId(), 15);

        Map<Integer, Integer> sale2 = new HashMap<>();
        sale2.put(popularItem.getId(), 20);

        Map<Integer, Integer> sale3 = new HashMap<>();
        sale3.put(popularItem.getId(), 10);

        Receipt receipt1 = store.createSale(1, sale1);
        Receipt receipt2 = store.createSale(2, sale2);
        Receipt receipt3 = store.createSale(3, sale3);

        assertNotNull(receipt1);
        assertNotNull(receipt2);
        assertNotNull(receipt3);

        assertEquals(cashier1, receipt1.getCashier());
        assertEquals(cashier2, receipt2.getCashier());
        assertEquals(cashier3, receipt3.getCashier());

        assertEquals(55, popularItem.getQuantity());

        cleanupReceiptFiles(receipt1, receipt2, receipt3);
    }

    @Test
    void testReceiptSerializationAndDeserialization() throws Exception {
        // Given a complete sale with multiple items
        Product milk = new Product("Fresh Milk", 3.0, ProductCategory.FOOD,
                LocalDate.now().plusDays(7), 20);
        Product bread = new Product("Whole Bread", 2.5, ProductCategory.FOOD,
                LocalDate.now().plusDays(3), 15);
        store.addProduct(milk);
        store.addProduct(bread);

        Cashier cashier = new Cashier("Test Cashier", 2000.0);
        store.addCashier(cashier);
        store.assignCashierToRegister(cashier, 1);

        Map<Integer, Integer> purchase = new HashMap<>();
        purchase.put(milk.getId(), 2); // 2 milk
        purchase.put(bread.getId(), 3); // 3 bread (near expiration)

        // When creating a sale and deserializing the receipt
        Receipt originalReceipt = store.createSale(1, purchase);
        String serFile = "output/receipts/receipt_" + originalReceipt.getReceiptNumber() + ".ser";
        String txtFile = "output/receipts/receipt_" + originalReceipt.getReceiptNumber() + ".txt";

        // Then both files should exist
        assertTrue(new File(serFile).exists(), "Serialized file should exist");
        assertTrue(new File(txtFile).exists(), "Text file should exist");

        // When deserializing
        Receipt deserializedReceipt = Receipt.deserializeFromFile(serFile);
        String fileText = Receipt.readReceiptTextFromFile(txtFile);

        // Then all properties should match
        assertEquals(originalReceipt.getReceiptNumber(), deserializedReceipt.getReceiptNumber());
        assertEquals(originalReceipt.getTotalAmount(), deserializedReceipt.getTotalAmount(), 0.01);
        assertEquals(originalReceipt.getCashier(), deserializedReceipt.getCashier());
        assertEquals(originalReceipt.getItems(), deserializedReceipt.getItems());
        assertEquals(originalReceipt.toString(), deserializedReceipt.toString());

        // And text file should contain all necessary information
        assertTrue(fileText.contains("Receipt #" + originalReceipt.getReceiptNumber()));
        assertTrue(fileText.contains(cashier.getName()));
        assertTrue(fileText.contains("Fresh Milk"));
        assertTrue(fileText.contains("Whole Bread"));
        assertTrue(fileText.contains(String.format("%.2f BGN", originalReceipt.getTotalAmount())));

        // Clean up
        new File(serFile).delete();
        new File(txtFile).delete();
    }

    @Test
    void testMultipleReceiptsSerialization() throws Exception {
        // Given multiple sales
        Product product = new Product("Test Product", 10.0, ProductCategory.FOOD,
                LocalDate.now().plusDays(10), 10);
        store.addProduct(product);

        Cashier cashier = new Cashier("Test Cashier", 2000.0);
        store.addCashier(cashier);
        store.assignCashierToRegister(cashier, 1);

        // When creating multiple sales
        Map<Integer, Integer> purchase1 = new HashMap<>();
        purchase1.put(product.getId(), 2);
        Receipt receipt1 = store.createSale(1, purchase1);

        Map<Integer, Integer> purchase2 = new HashMap<>();
        purchase2.put(product.getId(), 3);
        Receipt receipt2 = store.createSale(1, purchase2);

        // Then both receipts should be properly serialized and deserialized
        String serFile1 = "output/receipts/receipt_" + receipt1.getReceiptNumber() + ".ser";
        String serFile2 = "output/receipts/receipt_" + receipt2.getReceiptNumber() + ".ser";

        Receipt deserialized1 = Receipt.deserializeFromFile(serFile1);
        Receipt deserialized2 = Receipt.deserializeFromFile(serFile2);

        assertEquals(receipt1.getReceiptNumber(), deserialized1.getReceiptNumber());
        assertEquals(receipt2.getReceiptNumber(), deserialized2.getReceiptNumber());
        assertNotEquals(deserialized1.getReceiptNumber(), deserialized2.getReceiptNumber());

        // Clean up
        new File(serFile1).delete();
        new File(serFile2).delete();
    }

    private void cleanupReceiptFiles(Receipt... receipts) {
        for (Receipt receipt : receipts) {
            String txtFile = "output/receipts/receipt_" + receipt.getReceiptNumber() + ".txt";
            String serFile = "output/receipts/receipt_" + receipt.getReceiptNumber() + ".ser";
            new File(txtFile).delete();
            new File(serFile).delete();
        }
    }

    @Test
    void testProfitCalculation() {

        Product product = new Product("Test Product", 10.0, ProductCategory.FOOD,
                LocalDate.now().plusDays(10), 5);
        store.addProduct(product);

        Cashier cashier = new Cashier("Test Cashier", 2000.0);
        store.addCashier(cashier);
        store.assignCashierToRegister(cashier, 4);

        Map<Integer, Integer> purchase = new HashMap<>();
        purchase.put(product.getId(), 2);
        store.createSale(4, purchase);

        double income = store.getIncome();
        double salaryExpenses = store.getSalaryExpenses();
        double profit = store.getProfit();

        assertEquals(income - salaryExpenses, profit, 0.01);
    }
}
