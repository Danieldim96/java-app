package org.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.data.*;
import org.service.StoreService;
import org.exception.InsufficientQuantityException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class StoreServiceImplTest {
    private StoreService store;
    private Cashier cashier;

    @BeforeEach
    void setUp() {
        Receipt.resetReceiptCounter();
        store = new StoreServiceImpl(0.20, 0.30, 7, 0.15);

        Product milk = new Product(1, "Milk", 2.0, ProductCategory.FOOD,
                LocalDate.now().plusDays(5), 10); // 5 days triggers discount
        Product bread = new Product(2, "Bread", 1.5, ProductCategory.FOOD,
                LocalDate.now().plusDays(3), 15);
        Product soap = new Product(3, "Soap", 3.0, ProductCategory.NON_FOOD,
                LocalDate.now().plusDays(365), 20);

        store.addProduct(milk);
        store.addProduct(bread);
        store.addProduct(soap);

        cashier = new Cashier(1, "John Doe", 1500.0);
        store.addCashier(cashier);
        store.assignCashierToRegister(cashier, 1);
    }

    @Test
    void testOutputDirectoryCreation() throws IOException {
        Path outputPath = Paths.get("output/receipts");
        if (Files.exists(outputPath)) {
            Files.walk(outputPath)
                    .sorted((a, b) -> b.compareTo(a)) // reverse order for deletion
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException ignored) {
                        }
                    });
        }

        new StoreServiceImpl(0.20, 0.30, 7, 0.15);

        assertTrue(Files.exists(outputPath));
        assertTrue(Files.isDirectory(outputPath));
    }

    @Test
    void testReceiptFileGeneration() throws InsufficientQuantityException {
        Map<Integer, Integer> purchase = new HashMap<>();
        purchase.put(1, 1);
        Receipt receipt = store.createSale(1, purchase);

        String txtFile = "output/receipts/receipt_" + receipt.getReceiptNumber() + ".txt";
        String serFile = "output/receipts/receipt_" + receipt.getReceiptNumber() + ".ser";

        assertTrue(new File(txtFile).exists(), "Text file should be created");
        assertTrue(new File(serFile).exists(), "Serialized file should be created");

        // Clean up
        new File(txtFile).delete();
        new File(serFile).delete();
    }

    @Test
    void testReceiptSerializationAndDeserialization() throws Exception {

        Map<Integer, Integer> purchase = new HashMap<>();
        purchase.put(1, 1);
        Receipt receipt = store.createSale(1, purchase);
        String serFile = "output/receipts/receipt_" + receipt.getReceiptNumber() + ".ser";


        Receipt deserialized = Receipt.deserializeFromFile(serFile);
        assertEquals(receipt.getReceiptNumber(), deserialized.getReceiptNumber());
        assertEquals(receipt.getTotalAmount(), deserialized.getTotalAmount(), 0.01);
        assertEquals(receipt.toString(), deserialized.toString());

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
        assertTrue(fileText.contains(cashier.getName()));

        new File(txtFile).delete();
    }

    @Test
    void testFileOperationErrorHandling() throws InsufficientQuantityException {

        Map<Integer, Integer> purchase = new HashMap<>();
        purchase.put(1, 1);

        assertDoesNotThrow(() -> {
            Receipt receipt = store.createSale(1, purchase);
            assertNotNull(receipt);
        });
    }

    @Test
    void testMultipleStoreInstances() {
        StoreServiceImpl store1 = new StoreServiceImpl(0.20, 0.30, 7, 0.15);
        StoreServiceImpl store2 = new StoreServiceImpl(0.25, 0.35, 5, 0.10);

        Product product1 = new Product(1, "Product1", 10.0, ProductCategory.FOOD,
                LocalDate.now().plusDays(10), 5);
        Product product2 = new Product(2, "Product2", 10.0, ProductCategory.FOOD,
                LocalDate.now().plusDays(10), 5);

        store1.addProduct(product1);
        store2.addProduct(product2);

        double price1 = store1.calculateSellingPrice(product1.getId(), 0.20);
        double price2 = store2.calculateSellingPrice(product2.getId(), 0.25);
        assertNotEquals(price1, price2);
    }
}