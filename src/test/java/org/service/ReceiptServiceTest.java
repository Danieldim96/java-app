package org.service;

import org.data.Receipt;
import org.data.Cashier;
import org.data.Product;
import org.data.ProductCategory;
import org.service.impl.ReceiptServiceImpl;
import org.config.StoreConfig;
import org.exception.ReceiptPersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ReceiptServiceTest {
    private ReceiptService receiptService;
    private StoreConfig config;
    private Cashier cashier;
    private Product product;
    private Map<Product, Integer> items;

    @TempDir
    File tempDir;

    @BeforeEach
    void setUp() {
        Receipt.resetReceiptCounter();
        config = new StoreConfig();
        receiptService = new ReceiptServiceImpl(config);
        
        cashier = new Cashier("John Doe", 1500.0);
        product = new Product("Test Product", 10.0, ProductCategory.FOOD,
                LocalDate.now().plusDays(10), 5);
        
        items = new HashMap<>();
        items.put(product, 2);
    }

    @Nested
    class ReceiptCreationTests {
        @Test
        void testCreateReceipt() {
            double totalAmount = 24.0; // 2 * 10.0 * 1.2 (20% markup)
            Receipt receipt = receiptService.createReceipt(cashier, 1, items, totalAmount);

            assertNotNull(receipt);
            assertEquals(1, receipt.getReceiptNumber());
            assertEquals(cashier, receipt.getCashier());
            assertEquals(1, receipt.getRegisterNumber());
            assertEquals(totalAmount, receipt.getTotalAmount(), 0.01);
            
            // Check that items are stored correctly
            Map<Product, Integer> receiptItems = receipt.getItems();
            assertEquals(1, receiptItems.size());
            assertTrue(receiptItems.containsKey(product));
            assertEquals(2, receiptItems.get(product));
        }

        @Test
        void testGetReceipt() {
            double totalAmount = 24.0;
            Receipt createdReceipt = receiptService.createReceipt(cashier, 1, items, totalAmount);
            
            Receipt retrievedReceipt = receiptService.getReceipt(createdReceipt.getReceiptNumber());
            assertNotNull(retrievedReceipt);
            assertEquals(createdReceipt.getReceiptNumber(), retrievedReceipt.getReceiptNumber());
            assertEquals(createdReceipt.getTotalAmount(), retrievedReceipt.getTotalAmount(), 0.01);
        }

        @Test
        void testGetAllReceipts() {
            double totalAmount1 = 24.0;
            double totalAmount2 = 36.0;
            
            receiptService.createReceipt(cashier, 1, items, totalAmount1);
            receiptService.createReceipt(cashier, 2, items, totalAmount2);

            List<Receipt> receipts = receiptService.getAllReceipts();
            assertEquals(2, receipts.size());
            assertTrue(receipts.stream().anyMatch(r -> r.getTotalAmount() == totalAmount1));
            assertTrue(receipts.stream().anyMatch(r -> r.getTotalAmount() == totalAmount2));
        }
    }

    @Nested
    class ReceiptPersistenceTests {
        @Test
        void testSaveAndLoadReceipt() throws IOException, ClassNotFoundException, ReceiptPersistenceException {
            double totalAmount = 24.0;
            Receipt receipt = receiptService.createReceipt(cashier, 1, items, totalAmount);
            
            // The receipt is automatically saved by createReceipt, so we can load it
            String filePath = config.getReceiptOutputDir() + "/receipt_" + receipt.getReceiptNumber() + ".ser";
            
            Receipt loadedReceipt = receiptService.deserializeReceiptFromFile(filePath);
            assertNotNull(loadedReceipt);
            assertEquals(receipt.getReceiptNumber(), loadedReceipt.getReceiptNumber());
            assertEquals(receipt.getTotalAmount(), loadedReceipt.getTotalAmount(), 0.01);
        }

        @Test
        void testReadReceiptText() throws IOException {
            double totalAmount = 24.0;
            Receipt receipt = receiptService.createReceipt(cashier, 1, items, totalAmount);
            
            // The receipt is automatically saved by createReceipt, so we can read it
            String filePath = config.getReceiptOutputDir() + "/receipt_" + receipt.getReceiptNumber() + ".txt";
            
            String receiptText = receiptService.readReceiptTextFromFile(filePath);
            assertNotNull(receiptText);
            assertTrue(receiptText.contains("Receipt #" + receipt.getReceiptNumber()));
            assertTrue(receiptText.contains("Total: " + totalAmount));
        }
    }

    @Nested
    class FinancialTests {
        @Test
        void testGetTotalRevenue() {
            double totalAmount1 = 24.0;
            double totalAmount2 = 36.0;
            
            receiptService.createReceipt(cashier, 1, items, totalAmount1);
            receiptService.createReceipt(cashier, 2, items, totalAmount2);

            double expectedTotal = totalAmount1 + totalAmount2;
            assertEquals(expectedTotal, receiptService.getTotalRevenue(), 0.01);
        }

        @Test
        void testGetTotalReceipts() {
            receiptService.createReceipt(cashier, 1, items, 24.0);
            receiptService.createReceipt(cashier, 2, items, 36.0);

            assertEquals(2, receiptService.getTotalReceipts());
        }

        @Test
        void testResetReceiptCounter() {
            receiptService.createReceipt(cashier, 1, items, 24.0);
            receiptService.resetReceiptCounter();
            
            // After reset, the next receipt should start from 1 again
            Receipt newReceipt = receiptService.createReceipt(cashier, 2, items, 36.0);
            assertEquals(1, newReceipt.getReceiptNumber());
        }
    }

    @Nested
    class ErrorHandlingTests {
        @Test
        void testGetNonExistentReceipt() {
            assertNull(receiptService.getReceipt(999));
        }

        @Test
        void testDeserializeNonExistentFile() {
            assertThrows(org.exception.ReceiptPersistenceException.class, () -> {
                receiptService.deserializeReceiptFromFile("nonexistent.ser");
            });
        }

        @Test
        void testReadNonExistentTextFile() {
            assertThrows(IOException.class, () -> {
                receiptService.readReceiptTextFromFile("nonexistent.txt");
            });
        }
    }
} 