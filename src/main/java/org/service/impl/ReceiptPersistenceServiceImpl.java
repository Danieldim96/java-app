package org.service.impl;

import org.config.StoreConfig;
import org.data.Receipt;
import org.exception.ReceiptPersistenceException;
import org.service.ReceiptPersistenceService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class ReceiptPersistenceServiceImpl implements ReceiptPersistenceService {
    private final StoreConfig config;

    public ReceiptPersistenceServiceImpl(StoreConfig config) {
        this.config = config;
    }

    @Override
    public void saveReceipt(Receipt receipt) throws ReceiptPersistenceException {
        String filePath = getSerializedFilePath(receipt.getReceiptNumber());
        String textFilePath = getTextFilePath(receipt.getReceiptNumber());
        
        ensureDirectoryExists();
        
        // Save serialized receipt
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(receipt);
        } catch (IOException e) {
            throw new ReceiptPersistenceException("Failed to save receipt: " + e.getMessage(), e);
        }
        
        // Save text receipt
        try (PrintWriter writer = new PrintWriter(new FileWriter(textFilePath))) {
            writer.println(receipt);
        } catch (IOException e) {
            throw new ReceiptPersistenceException("Failed to save receipt text: " + e.getMessage(), e);
        }
    }

    @Override
    public Receipt deserializeReceiptFromFile(String filePath) throws IOException, ClassNotFoundException, ReceiptPersistenceException {
        int attempts = 0;
        IOException lastException = null;
        
        while (attempts < config.getMaxRetryAttempts()) {
            try {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
                    return (Receipt) ois.readObject();
                }
            } catch (IOException e) {
                lastException = e;
                attempts++;
                if (attempts < config.getMaxRetryAttempts()) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(config.getRetryDelayMs());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new ReceiptPersistenceException("Interrupted while waiting to retry", ie);
                    }
                }
            }
        }
        
        throw new ReceiptPersistenceException("Failed to deserialize receipt after " + attempts + " attempts", lastException);
    }

    @Override
    public String readReceiptTextFromFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    @Override
    public String getSerializedFilePath(int receiptNumber) {
        return config.getReceiptOutputDir() + "/receipt_" + receiptNumber + ".ser";
    }

    @Override
    public String getTextFilePath(int receiptNumber) {
        return config.getReceiptOutputDir() + "/receipt_" + receiptNumber + ".txt";
    }
    
    private void ensureDirectoryExists() throws ReceiptPersistenceException {
        Path dir = Paths.get(config.getReceiptOutputDir());
        if (!Files.exists(dir)) {
            if (config.isCreateMissingDirectories()) {
                try {
                    Files.createDirectories(dir);
                } catch (IOException e) {
                    if (config.isThrowExceptionOnDirectoryCreationFailure()) {
                        throw new ReceiptPersistenceException("Failed to create directory: " + e.getMessage(), e);
                    }
                }
            } else if (config.isThrowExceptionOnDirectoryCreationFailure()) {
                throw new ReceiptPersistenceException("Directory does not exist: " + config.getReceiptOutputDir());
            }
        }
    }
} 