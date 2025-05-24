package org.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.model.*;
import org.util.InsufficientQuantityException;

import java.io.File;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class StoreTest {
    private Store store;
    private Product milk;
    private Product bread;
    private Product soap;
    private Cashier cashier;

    @BeforeEach
    void setUp() {
        store = new Store(20.0, 30.0, 7, 15.0);
        
        milk = new Product(1, "Milk", 2.0, ProductCategory.FOOD, 
            LocalDate.now().plusDays(5), 10); // 5 days triggers discount
        bread = new Product(2, "Bread", 1.5, ProductCategory.FOOD, 
            LocalDate.now().plusDays(3), 15);
        soap = new Product(3, "Soap", 3.0, ProductCategory.NON_FOOD, 
            LocalDate.now().plusDays(365), 20);

        store.addProduct(milk);
        store.addProduct(bread);
        store.addProduct(soap);

        cashier = new Cashier(1, "John Doe", 1500.0);
        store.addCashier(cashier);
        store.assignCashierToRegister(cashier, 1);
    }

    @Test
    void testCalculateSellingPrice() {
        // Test food item price calculation (with discount)
        double milkPrice = store.calculateSellingPrice(milk);
        assertEquals(2.04, milkPrice, 0.01); // 2.0 * (1 + 20%) * (1 - 15%)

        // Test non-food item price calculation
        double soapPrice = store.calculateSellingPrice(soap);
        assertEquals(3.9, soapPrice, 0.01); // 3.0 * (1 + 30%)
    }

    @Test
    void testNearExpirationDiscount() {
        Product nearExpiredMilk = new Product(4, "Near Expired Milk", 2.0, 
            ProductCategory.FOOD, LocalDate.now().plusDays(3), 5);
        store.addProduct(nearExpiredMilk);

        double price = store.calculateSellingPrice(nearExpiredMilk);
        // Original price: 2.0 * (1 + 20%) = 2.4
        // With 15% discount: 2.4 * (1 - 15%) = 2.04
        assertEquals(2.04, price, 0.01);
    }

    @Test
    void testExpiredProduct() {
        Product expiredMilk = new Product(5, "Expired Milk", 2.0, 
            ProductCategory.FOOD, LocalDate.now().minusDays(1), 5);
        store.addProduct(expiredMilk);

        assertThrows(IllegalStateException.class, () -> {
            store.calculateSellingPrice(expiredMilk);
        });
    }

    @Test
    void testCreateSale() throws InsufficientQuantityException {
        Map<Integer, Integer> purchase = new HashMap<>();
        purchase.put(1, 2); // 2 units of milk
        purchase.put(2, 1); // 1 unit of bread

        Receipt receipt = store.createSale(1, purchase);
        assertNotNull(receipt);
        assertEquals(8, milk.getQuantity()); // 10 - 2
        assertEquals(14, bread.getQuantity()); // 15 - 1
    }

    @Test
    void testInsufficientQuantity() {
        Map<Integer, Integer> purchase = new HashMap<>();
        purchase.put(1, 15); // Try to buy more milk than available

        assertThrows(InsufficientQuantityException.class, () -> {
            store.createSale(1, purchase);
        });
    }

    @Test
    void testAssignCashierToRegister() {
        Cashier newCashier = new Cashier(2, "Jane Doe", 1600.0);
        store.addCashier(newCashier);
        store.assignCashierToRegister(newCashier, 2);

        assertThrows(IllegalStateException.class, () -> {
            store.assignCashierToRegister(cashier, 2);
        });
    }

    @Test
    void testTotalRevenueAndReceipts() throws InsufficientQuantityException {
        Map<Integer, Integer> purchase1 = new HashMap<>();
        purchase1.put(1, 2);
        store.createSale(1, purchase1);
        Map<Integer, Integer> purchase2 = new HashMap<>();
        purchase2.put(2, 1);
        store.createSale(1, purchase2);
        assertEquals(4, store.getTotalReceipts());
        assertTrue(store.getTotalRevenue() > 0);
    }

    @Test
    void testReceiptSerializationAndDeserialization() throws Exception {
        Map<Integer, Integer> purchase = new HashMap<>();
        purchase.put(1, 1);
        Receipt receipt = store.createSale(1, purchase);
        String serFile = "output/receipts/receipt_" + receipt.getReceiptNumber() + ".ser";
        Receipt deserialized = Receipt.deserializeFromFile(serFile);
        assertEquals(receipt.getReceiptNumber(), deserialized.getReceiptNumber());
        assertEquals(receipt.generateReceiptText(), deserialized.generateReceiptText());
        // Clean up
        new File(serFile).delete();
    }

    @Test
    void testReadReceiptTextFromFile() throws Exception {
        Map<Integer, Integer> purchase = new HashMap<>();
        purchase.put(1, 1);
        Receipt receipt = store.createSale(1, purchase);
        String txtFile = "output/receipts/receipt_" + receipt.getReceiptNumber() + ".txt";
        String fileText = Receipt.readReceiptTextFromFile(txtFile);
        assertTrue(fileText.contains("Receipt #" + receipt.getReceiptNumber()));
        // Clean up
        new File(txtFile).delete();
    }

    @Test
    void testStoreCalculations() throws Exception {
        // Add another cashier
        Cashier c2 = new Cashier(2, "Jane Doe", 2000.0);
        store.addCashier(c2);
        // Make some sales
        Map<Integer, Integer> purchase = new HashMap<>();
        purchase.put(1, 2);
        store.createSale(1, purchase);
        // Salary expenses
        double salary = store.getSalaryExpenses();
        assertEquals(3500.0, salary, 0.01);
        // Delivery expenses (all delivered and sold)
        double delivery = store.getDeliveryExpenses();
        assertTrue(delivery > 0);
        // Income
        double income = store.getIncome();
        assertTrue(income > 0);
        // Profit
        double profit = store.getProfit();
        assertEquals(income - salary - delivery, profit, 0.01);
    }
} 