package org.service;

import org.data.Receipt;
import org.data.Cashier;
import org.data.Product;
import org.exception.ReceiptPersistenceException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Interface for receipt management operations
 */
public interface ReceiptService {
    /**
     * Create a new receipt
     * @param cashier The cashier who processed the sale
     * @param registerNumber The register number
     * @param items The items sold (product -> quantity)
     * @param totalAmount The total amount
     * @return The created receipt
     */
    Receipt createReceipt(Cashier cashier, int registerNumber, Map<Product, Integer> items, double totalAmount);

    /**
     * Get a receipt by its number
     * @param receiptNumber The receipt number
     * @return The receipt, or null if not found
     */
    Receipt getReceipt(int receiptNumber);

    /**
     * Get all receipts
     * @return A list of all receipts
     */
    List<Receipt> getAllReceipts();

    /**
     * Save a receipt to persistent storage
     * @param receipt The receipt to save
     */
    void saveReceipt(Receipt receipt);

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
     * Reset the receipt counter
     */
    void resetReceiptCounter();

    /**
     * Get the total revenue from all receipts
     * @return The total revenue
     */
    double getTotalRevenue();

    /**
     * Get the total number of receipts
     * @return The total number of receipts
     */
    int getTotalReceipts();

    /**
     * Get the persistence service used by this receipt service
     * @return The persistence service
     */
    ReceiptPersistenceService getPersistenceService();
}
