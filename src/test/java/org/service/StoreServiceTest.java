package org.service;

import org.data.Product;
import org.data.Cashier;
import org.data.Receipt;
import org.data.ProductCategory;
import org.service.impl.StoreServiceImpl;
import org.exception.InsufficientQuantityException;
import org.exception.RegisterAlreadyAssignedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class StoreServiceTest {
    private StoreService store;

    @BeforeEach
    void setUp() {
        Receipt.resetReceiptCounter();
        store = new StoreServiceImpl(0.20, 0.30, 7, 0.15);

        Product milk = new Product(1, "Milk", 2.0, ProductCategory.FOOD,
                LocalDate.now().plusDays(10), 10);
        Product bread = new Product(2, "Bread", 1.5, ProductCategory.FOOD,
                LocalDate.now().plusDays(3), 15);

        store.addProduct(milk);
        store.addProduct(bread);

        Cashier john = new Cashier(1, "John Doe", 1500.0);
        store.addCashier(john);
        store.assignCashierToRegister(john, 1);
    }

    @Test
    void testCreateSale() throws InsufficientQuantityException {
        Map<Integer, Integer> purchase = new HashMap<>();
        purchase.put(1, 2); // 2 milk
        purchase.put(2, 1); // 1 bread

        Receipt receipt = store.createSale(1, purchase);
        assertNotNull(receipt);
        assertEquals(1, receipt.getReceiptNumber());

        // Verify inventory updated
        Product updatedMilk = store.getDeliveredProducts().stream()
                .filter(p -> p.getId() == 1)
                .findFirst()
                .orElse(null);
        assertNotNull(updatedMilk);
        assertEquals(8, updatedMilk.getQuantity()); // 10 - 2
    }

    @Test
    void testInsufficientQuantity() {
        Map<Integer, Integer> purchase = new HashMap<>();
        purchase.put(1, 15); // Try to buy 15 milk when only 10 available

        assertThrows(InsufficientQuantityException.class, () -> {
            store.createSale(1, purchase);
        });
    }

    @Test
    void testFinancialCalculations() throws InsufficientQuantityException {
        Map<Integer, Integer> purchase = new HashMap<>();
        purchase.put(1, 2); // 2 milk
        purchase.put(2, 1); // 1 bread
        store.createSale(1, purchase);

        double revenue = store.getTotalRevenue();
        double salaryExpenses = store.getSalaryExpenses();
        double deliveryExpenses = store.getDeliveryExpenses();
        double income = store.getIncome();
        double profit = store.getProfit();

        System.out.println("Total Revenue: " + revenue);
        System.out.println("Salary Expenses: " + salaryExpenses);
        System.out.println("Delivery Expenses: " + deliveryExpenses);
        System.out.println("Income: " + income);
        System.out.println("Profit: " + profit);

        assertTrue(revenue > 0);
        assertEquals(1500.0, salaryExpenses);
        assertTrue(deliveryExpenses > 0);
        assertEquals(revenue, income);
        assertEquals(income - salaryExpenses, profit, 0.01);
    }

    @Test
    void testTrackTotalRevenueAndReceipts() throws InsufficientQuantityException {

        int initialReceipts = store.getTotalReceipts();
        double initialRevenue = store.getTotalRevenue();

        Map<Integer, Integer> purchase = new HashMap<>();
        purchase.put(1, 1);
        store.createSale(1, purchase);

        assertEquals(initialReceipts + 1, store.getTotalReceipts());
        assertTrue(store.getTotalRevenue() > initialRevenue);
    }

    @Test
    void shouldCalculateFinancialsCorrectly() {
        double salaryExpenses = store.getSalaryExpenses();
        double deliveryExpenses = store.getDeliveryExpenses();
        double income = store.getIncome();
        double profit = store.getProfit();

        System.out.println("salaryExpenses=" + salaryExpenses + ", deliveryExpenses=" + deliveryExpenses + ", income=" + income + ", profit=" + profit);

        assertTrue(salaryExpenses >= 0);
        assertTrue(deliveryExpenses >= 0);
        assertTrue(income >= 0);
        assertEquals(income - salaryExpenses, profit, 0.01);
    }

    @Test
    void shouldNotAllowAssigningCashierToOccupiedRegister() {
        Cashier newCashier = new Cashier(2, "New Cashier", 1800.0);
        store.addCashier(newCashier);

        assertThrows(RegisterAlreadyAssignedException.class, () -> {
            store.assignCashierToRegister(newCashier, 1);
        });
    }
} 
