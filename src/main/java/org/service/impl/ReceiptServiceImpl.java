package org.service.impl;

import org.config.StoreConfig;
import org.data.Receipt;
import org.data.Cashier;
import org.data.Product;
import org.exception.ReceiptPersistenceException;
import org.service.ReceiptService;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReceiptServiceImpl implements ReceiptService {
    private final Map<Integer, Receipt> receipts;
    private final String outputDir;
    private static final Object receiptNumberLock = new Object();
    private final StoreConfig config;

    public ReceiptServiceImpl() {
        this(new StoreConfig());
    }

    public ReceiptServiceImpl(StoreConfig config) {
        this.receipts = new HashMap<>();
        this.config = config;
        this.outputDir = config.getReceiptOutputDir();
        createOutputDirectory();
    }

    @Override
    public Receipt deserializeReceiptFromFile(String filePath) throws IOException, ClassNotFoundException {
        Exception lastException = null;

        for (int attempt = 0; attempt < config.getMaxRetryAttempts(); attempt++) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
                return (Receipt) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                lastException = e;
                // If not the last attempt, wait before retrying
                if (attempt < config.getMaxRetryAttempts() - 1) {
                    try {
                        Thread.sleep(config.getRetryDelayMs());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Thread interrupted while waiting to retry", ie);
                    }
                }
            }
        }

        // If we get here, all attempts failed
        if (lastException instanceof IOException) {
            throw (IOException) lastException;
        } else if (lastException instanceof ClassNotFoundException) {
            throw (ClassNotFoundException) lastException;
        } else {
            throw new IOException("Failed to deserialize receipt after " + 
                    config.getMaxRetryAttempts() + " attempts: " + filePath, lastException);
        }
    }

    @Override
    public String readReceiptTextFromFile(String filePath) throws IOException {
        Exception lastException = null;

        for (int attempt = 0; attempt < config.getMaxRetryAttempts(); attempt++) {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                return sb.toString();
            } catch (IOException e) {
                lastException = e;
                // If not the last attempt, wait before retrying
                if (attempt < config.getMaxRetryAttempts() - 1) {
                    try {
                        Thread.sleep(config.getRetryDelayMs());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Thread interrupted while waiting to retry", ie);
                    }
                }
            }
        }

        // If we get here, all attempts failed
        throw new IOException("Failed to read receipt text file after " + 
                config.getMaxRetryAttempts() + " attempts: " + filePath, lastException);
    }

    @Override
    public void resetReceiptCounter() {
        synchronized (receiptNumberLock) {
            Receipt.resetReceiptCounter();
        }
    }

    private void createOutputDirectory() {
        if (!config.isCreateMissingDirectories()) {
            return;
        }

        File dir = new File(outputDir);
        if (!dir.exists()) {
            boolean created = false;
            Exception lastException = null;

            // Attempt to create directory with retry logic
            for (int attempt = 0; attempt < config.getMaxRetryAttempts(); attempt++) {
                try {
                    created = dir.mkdirs();
                    if (created) {
                        break;
                    }

                    // If not successful but we have more attempts, wait before retrying
                    if (attempt < config.getMaxRetryAttempts() - 1) {
                        Thread.sleep(config.getRetryDelayMs());
                    }
                } catch (Exception e) {
                    lastException = e;
                    // If exception occurred but we have more attempts, wait before retrying
                    if (attempt < config.getMaxRetryAttempts() - 1) {
                        try {
                            Thread.sleep(config.getRetryDelayMs());
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }

            // After all attempts, if directory creation failed and we should throw exception
            if (!created && config.isThrowExceptionOnDirectoryCreationFailure()) {
                if (lastException != null) {
                    throw new ReceiptPersistenceException("Failed to create directory after " + 
                            config.getMaxRetryAttempts() + " attempts: " + outputDir, lastException);
                } else {
                    throw new ReceiptPersistenceException("Failed to create directory after " + 
                            config.getMaxRetryAttempts() + " attempts: " + outputDir);
                }
            }
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
        saveReceiptAsText(receipt);
        saveReceiptAsSerialized(receipt);
    }

    private void saveReceiptAsText(Receipt receipt) {
        String filePath = outputDir + "/receipt_" + receipt.getReceiptNumber() + ".txt";
        Exception lastException = null;

        for (int attempt = 0; attempt < config.getMaxRetryAttempts(); attempt++) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
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
                return; // Success, exit the method
            } catch (IOException e) {
                lastException = e;
                // If not the last attempt, wait before retrying
                if (attempt < config.getMaxRetryAttempts() - 1) {
                    try {
                        Thread.sleep(config.getRetryDelayMs());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        // If we get here, all attempts failed
        throw new ReceiptPersistenceException("Failed to save receipt as text after " + 
                config.getMaxRetryAttempts() + " attempts: " + filePath, lastException);
    }

    private void saveReceiptAsSerialized(Receipt receipt) {
        String filePath = outputDir + "/receipt_" + receipt.getReceiptNumber() + ".ser";
        Exception lastException = null;

        for (int attempt = 0; attempt < config.getMaxRetryAttempts(); attempt++) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
                oos.writeObject(receipt);
                return; // Success, exit the method
            } catch (IOException e) {
                lastException = e;
                // If not the last attempt, wait before retrying
                if (attempt < config.getMaxRetryAttempts() - 1) {
                    try {
                        Thread.sleep(config.getRetryDelayMs());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        // If we get here, all attempts failed
        throw new ReceiptPersistenceException("Failed to serialize receipt after " + 
                config.getMaxRetryAttempts() + " attempts: " + filePath, lastException);
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
