package org.service.mock;

import org.data.Cashier;
import org.data.Product;
import org.data.Receipt;
import org.exception.ReceiptPersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.service.ReceiptPersistenceService;
import org.service.impl.ReceiptServiceImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class ReceiptServiceMockTest {
    private ReceiptPersistenceService persistenceService;
    private ReceiptServiceImpl receiptService;
    private Cashier mockCashier;
    private Product mockProduct;
    private Map<Product, Integer> items;

    @BeforeEach
    void setUp() {
        persistenceService = mock(ReceiptPersistenceService.class);
        receiptService = new ReceiptServiceImpl(persistenceService);
        mockCashier = mock(Cashier.class);
        mockProduct = mock(Product.class);
        items = new HashMap<>();
        items.put(mockProduct, 2);
        reset(persistenceService, mockCashier, mockProduct);
    }

    @Test
    void testCreateReceipt() throws ReceiptPersistenceException {
        doNothing().when(persistenceService).saveReceipt(any(Receipt.class));
        Receipt receipt = receiptService.createReceipt(mockCashier, 1, items, 24.0);
        assertNotNull(receipt);
        assertEquals(1, receipt.getReceiptNumber());
        assertEquals(mockCashier, receipt.getCashier());
        assertEquals(1, receipt.getRegisterNumber());
        assertEquals(24.0, receipt.getTotalAmount(), 0.01);
        verify(persistenceService).saveReceipt(receipt);
    }

    @Test
    void testGetReceipt() throws IOException, ClassNotFoundException, ReceiptPersistenceException {
        Receipt mockReceipt = mock(Receipt.class);
        when(mockReceipt.getReceiptNumber()).thenReturn(1);
        when(persistenceService.deserializeReceiptFromFile("receipt_1.ser")).thenReturn(mockReceipt);
        Receipt receipt = receiptService.getReceipt(1);
        assertNotNull(receipt);
        assertEquals(1, receipt.getReceiptNumber());
        verify(persistenceService).deserializeReceiptFromFile("receipt_1.ser");
    }

    @Test
    void testDeserializeReceiptFromFile() throws IOException, ClassNotFoundException, ReceiptPersistenceException {
        Receipt mockReceipt = mock(Receipt.class);
        when(persistenceService.deserializeReceiptFromFile(anyString())).thenReturn(mockReceipt);
        Receipt receipt = receiptService.deserializeReceiptFromFile("test.ser");
        assertNotNull(receipt);
        verify(persistenceService).deserializeReceiptFromFile("test.ser");
    }

    @Test
    void testReadReceiptTextFromFile() throws IOException {
        String expectedText = "Receipt #1\nTotal: 24.0";
        when(persistenceService.readReceiptTextFromFile(anyString())).thenReturn(expectedText);
        String text = receiptService.readReceiptTextFromFile("test.txt");
        assertEquals(expectedText, text);
        verify(persistenceService).readReceiptTextFromFile("test.txt");
    }

    @Test
    void testDeserializeNonExistentFile() throws IOException, ClassNotFoundException {
        when(persistenceService.deserializeReceiptFromFile(anyString()))
            .thenThrow(new ReceiptPersistenceException("File not found"));
        assertThrows(ReceiptPersistenceException.class, () -> {
            receiptService.deserializeReceiptFromFile("nonexistent.ser");
        });
    }

    @Test
    void testReadNonExistentTextFile() throws IOException {
        when(persistenceService.readReceiptTextFromFile(anyString()))
            .thenThrow(new IOException("File not found"));
        assertThrows(IOException.class, () -> {
            receiptService.readReceiptTextFromFile("nonexistent.txt");
        });
    }
} 