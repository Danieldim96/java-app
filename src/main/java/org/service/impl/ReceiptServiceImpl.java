package org.service.impl;

import org.data.Receipt;
import org.data.Cashier;
import org.data.Product;
import org.service.ReceiptService;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReceiptServiceImpl implements ReceiptService {
    private final Map<Integer, Receipt> receipts;
    private final String outputDir;

    public ReceiptServiceImpl() {
        this.receipts = new HashMap<>();
        this.outputDir = "output/receipts";
        createOutputDirectory();
    }

    private void createOutputDirectory() {
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    public Receipt createReceipt(Cashier cashier, int registerNumber, Map<Product, Integer> items, double totalAmount) {
        Receipt receipt = new Receipt(cashier, items, totalAmount);
        receipts.put(receipt.getReceiptNumber(), receipt);
        saveReceipt(receipt);
        return receipt;
    }

    @Override
    public Receipt getReceipt(int receiptNumber) {
        return receipts.get(receiptNumber);
    }

    @Override
    public List<Receipt> getAllReceipts() {
        return new ArrayList<>(receipts.values());
    }

    @Override
    public void saveReceipt(Receipt receipt) {
        // Save as text file
        try (PrintWriter writer = new PrintWriter(new FileWriter(
                outputDir + "/receipt_" + receipt.getReceiptNumber() + ".txt"))) {
            writer.println("Receipt #" + receipt.getReceiptNumber());
            writer.println("Date: " + receipt.getDate());
            writer.println("Cashier: " + receipt.getCashier().getName());
            writer.println("Items:");
            for (Map.Entry<Product, Integer> entry : receipt.getItems().entrySet()) {
                writer.printf("%s x%d - %.2f BGN\n",
                        entry.getKey().getName(),
                        entry.getValue(),
                        entry.getKey().getDeliveryPrice() * entry.getValue());
            }
            writer.printf("Total: %.2f BGN\n", receipt.getTotalAmount());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save receipt", e);
        }

        // Save as serialized file
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(outputDir + "/receipt_" + receipt.getReceiptNumber() + ".ser"))) {
            oos.writeObject(receipt);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize receipt", e);
        }
    }

    @Override
    public double getTotalRevenue() {
        return receipts.values().stream()
                .mapToDouble(Receipt::getTotalAmount)
                .sum();
    }

    @Override
    public int getTotalReceipts() {
        return receipts.size();
    }
}