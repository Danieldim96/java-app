package org.service;

import org.data.Product;
import org.data.Cashier;
import org.data.Receipt;
import org.data.ProductCategory;
import org.data.Store;
import org.service.impl.StoreServiceImpl;
import org.service.impl.ProductServiceImpl;
import org.service.impl.CashierServiceImpl;
import org.service.impl.ReceiptServiceImpl;
import org.config.StoreConfig;
import org.exception.InsufficientQuantityException;
import org.exception.RegisterAlreadyAssignedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.service.PricingService;
import org.service.impl.PricingServiceImpl;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class StoreServiceTest {
    private StoreService store;
    private Store storeData;
    private StoreConfig config;
    private ProductService productService;
    private CashierService cashierService;
    private ReceiptService receiptService;
    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        Receipt.resetReceiptCounter();
        config = new StoreConfig();
        storeData = new Store("Test Store", "Test Address", 0.20, 0.30, 7, 0.15);
        productService = new ProductServiceImpl(7, 0.15);
        cashierService = new CashierServiceImpl();
        receiptService = new ReceiptServiceImpl(config);
        pricingService = new PricingServiceImpl(productService, 7, 0.15);
        store = new StoreServiceImpl(storeData, config, productService, cashierService, receiptService, pricingService);

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

    @Nested
    class ProductManagementTests {
        @Test
        void testAddProduct() {
            Product newProduct = new Product(3, "New Product", 5.0, ProductCategory.NON_FOOD,
                    LocalDate.now().plusDays(30), 20);
            store.addProduct(newProduct);
            
            assertTrue(store.getDeliveredProducts().stream()
                    .anyMatch(p -> p.getId() == 3));
        }

        @Test
        void testGetDeliveredProducts() {
            assertEquals(2, store.getDeliveredProducts().size());
            assertTrue(store.getDeliveredProducts().stream()
                    .anyMatch(p -> p.getName().equals("Milk")));
            assertTrue(store.getDeliveredProducts().stream()
                    .anyMatch(p -> p.getName().equals("Bread")));
        }
    }

    @Nested
    class CashierManagementTests {
        @Test
        void testAddCashier() {
            Cashier newCashier = new Cashier(2, "Jane Doe", 1600.0);
            store.addCashier(newCashier);
            
            assertTrue(store.getCashiers().stream()
                    .anyMatch(c -> c.getName().equals("Jane Doe")));
        }

        @Test
        void testAssignCashierToRegister() {
            Cashier newCashier = new Cashier(2, "Jane Doe", 1600.0);
            store.addCashier(newCashier);
            store.assignCashierToRegister(newCashier, 2);
            
            assertEquals(newCashier, store.getCashierAtRegister(2));
            assertTrue(store.isRegisterAssigned(2));
        }

        @Test
        void testRegisterAlreadyAssigned() {
            Cashier newCashier = new Cashier(2, "Jane Doe", 1600.0);
            store.addCashier(newCashier);
            
            assertThrows(RegisterAlreadyAssignedException.class, () -> {
                store.assignCashierToRegister(newCashier, 1);
            });
        }
    }

    @Nested
    class SalesManagementTests {
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
    }

    @Nested
    class FinancialManagementTests {
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

            assertTrue(revenue > 0);
            double expectedSalary = store.getCashiers().stream().mapToDouble(Cashier::getSalary).sum();
            assertEquals(expectedSalary, salaryExpenses, 0.01, "Expected salary expenses: " + expectedSalary + ", but got: " + salaryExpenses);
            assertTrue(deliveryExpenses > 0);
            assertEquals(revenue - deliveryExpenses, income, 0.01);
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
    }
} 
