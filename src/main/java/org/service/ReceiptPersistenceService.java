package org.service;

import org.data.Receipt;
import org.exception.ReceiptPersistenceException;

import java.io.IOException;

/**
 * Interface for receipt persistence operations
 */
public interface ReceiptPersistenceService {
    /**
     * Save a receipt to persistent storage
     * @param receipt The receipt to save
     * @throws ReceiptPersistenceException if there's an error with receipt persistence
     */
    void saveReceipt(Receipt receipt) throws ReceiptPersistenceException;
    
    /**
     * Deserialize a receipt from a file
     * @param filePath The file path
     * @return The deserialized receipt
     * @throws IOException if there's an I/O error
     * @throws ClassNotFoundException if the receipt class can't be found
     * @throws ReceiptPersistenceException if there's an error with receipt persistence
     */
    Receipt deserializeReceiptFromFile(String filePath) throws IOException, ClassNotFoundException, ReceiptPersistenceException;
    
    /**
     * Read a receipt's text content from a file
     * @param filePath The file path
     * @return The receipt text
     * @throws IOException if there's an I/O error
     */
    String readReceiptTextFromFile(String filePath) throws IOException;

    /**
     * Get the file path for a receipt's serialized file
     * @param receiptNumber The receipt number
     * @return The file path
     */
    String getSerializedFilePath(int receiptNumber);

    /**
     * Get the file path for a receipt's text file
     * @param receiptNumber The receipt number
     * @return The file path
     */
    String getTextFilePath(int receiptNumber);
} 