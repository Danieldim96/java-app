package org.service;

import org.data.Product;
import org.data.Cashier;
import org.data.Receipt;
import org.data.Store;
import org.exception.InsufficientQuantityException;
import org.exception.ReceiptPersistenceException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Main service interface for store operations.
 * This interface extends several more focused interfaces to better follow the Interface Segregation Principle.
 */
public interface StoreService extends ProductManagementService, CashierManagementService, SalesManagementService, FinancialManagementService {
    /**
     * Get the store entity
     * @return The store
     */
    Store getStore();
    
    /**
     * Load a receipt from file
     * @param receiptNumber The receipt number
     * @return The loaded receipt
     * @throws IOException if there's an I/O error
     * @throws ClassNotFoundException if the receipt class can't be found
     * @throws ReceiptPersistenceException if there's an error with receipt persistence
     */
    Receipt loadReceiptFromFile(int receiptNumber) throws IOException, ClassNotFoundException, ReceiptPersistenceException;
}

/**
 * Interface for product management operations
 */
interface ProductManagementService {
    /**
     * Add a product to the store
     * @param product The product to add
     */
    void addProduct(Product product);
    
    /**
     * Get all delivered products
     * @return List of all products
     */
    List<Product> getDeliveredProducts();
}

/**
 * Interface for cashier management operations
 */
interface CashierManagementService {
    /**
     * Add a cashier to the store
     * @param cashier The cashier to add
     */
    void addCashier(Cashier cashier);
    
    /**
     * Get all cashiers
     * @return List of all cashiers
     */
    List<Cashier> getCashiers();
    
    /**
     * Assign a cashier to a register
     * @param cashier The cashier to assign
     * @param registerNumber The register number
     */
    void assignCashierToRegister(Cashier cashier, int registerNumber);
    
    /**
     * Get the cashier at a specific register
     * @param registerNumber The register number
     * @return The cashier at the register, or null if none
     */
    Cashier getCashierAtRegister(int registerNumber);
    
    /**
     * Check if a register is assigned
     * @param registerNumber The register number
     * @return true if the register is assigned, false otherwise
     */
    boolean isRegisterAssigned(int registerNumber);
}

/**
 * Interface for sales management operations
 */
interface SalesManagementService {
    /**
     * Create a sale
     * @param registerNumber The register number
     * @param purchase The purchase details (product ID -> quantity)
     * @return The created receipt
     * @throws InsufficientQuantityException if there's insufficient quantity
     */
    Receipt createSale(int registerNumber, Map<Integer, Integer> purchase) throws InsufficientQuantityException;
    
    /**
     * Get the total number of receipts
     * @return The total number of receipts
     */
    int getTotalReceipts();
}

/**
 * Interface for financial management operations
 */
interface FinancialManagementService {
    /**
     * Get the total revenue
     * @return The total revenue
     */
    double getTotalRevenue();
    
    /**
     * Get the salary expenses
     * @return The salary expenses
     */
    double getSalaryExpenses();
    
    /**
     * Get the delivery expenses
     * @return The delivery expenses
     */
    double getDeliveryExpenses();
    
    /**
     * Get the total income
     * @return The total income
     */
    double getIncome();
    
    /**
     * Get the profit
     * @return The profit
     */
    double getProfit();
}
