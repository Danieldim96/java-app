package org.service.impl;

import org.config.StoreConfig;
import org.data.Receipt;
import org.data.Cashier;
import org.data.Product;
import org.exception.ReceiptPersistenceException;
import org.service.ReceiptService;
import org.service.ReceiptPersistenceService;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ReceiptServiceImpl implements ReceiptService {
    private final ReceiptPersistenceService persistenceService;
    private final List<Receipt> receipts;
    private final AtomicInteger receiptCounter;

    public ReceiptServiceImpl(StoreConfig config) {
        this.persistenceService = new ReceiptPersistenceServiceImpl(config);
        this.receipts = new ArrayList<>();
        this.receiptCounter = new AtomicInteger(1);
    }

    public ReceiptServiceImpl(ReceiptPersistenceService persistenceService) {
        this.persistenceService = persistenceService;
        this.receipts = new ArrayList<>();
        this.receiptCounter = new AtomicInteger(1);
    }

    @Override
    public Receipt createReceipt(Cashier cashier, int registerNumber, Map<Product, Integer> items, double totalAmount) {
        int receiptNumber = receiptCounter.getAndIncrement();
        Receipt receipt = new Receipt(receiptNumber, cashier, registerNumber, items, totalAmount);
        receipts.add(receipt);
        
        try {
            persistenceService.saveReceipt(receipt);
        } catch (ReceiptPersistenceException e) {
            // Log the error but don't fail the receipt creation
            System.err.println("Failed to save receipt: " + e.getMessage());
        }
        
        return receipt;
    }

    @Override
    public Receipt getReceipt(int receiptNumber) {
        return receipts.stream()
                .filter(r -> r.getReceiptNumber() == receiptNumber)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Receipt> getAllReceipts() {
        return new ArrayList<>(receipts);
    }

    @Override
    public void saveReceipt(Receipt receipt) {
        try {
            persistenceService.saveReceipt(receipt);
        } catch (ReceiptPersistenceException e) {
            throw new RuntimeException("Failed to save receipt: " + e.getMessage(), e);
        }
    }

    @Override
    public Receipt deserializeReceiptFromFile(String filePath) throws IOException, ClassNotFoundException, ReceiptPersistenceException {
        return persistenceService.deserializeReceiptFromFile(filePath);
    }

    @Override
    public String readReceiptTextFromFile(String filePath) throws IOException {
        return persistenceService.readReceiptTextFromFile(filePath);
    }

    @Override
    public void resetReceiptCounter() {
        receiptCounter.set(1);
    }

    @Override
    public double getTotalRevenue() {
        return receipts.stream()
                .mapToDouble(Receipt::getTotalAmount)
                .sum();
    }

    @Override
    public int getTotalReceipts() {
        return receipts.size();
    }

    @Override
    public ReceiptPersistenceService getPersistenceService() {
        return persistenceService;
    }
}
