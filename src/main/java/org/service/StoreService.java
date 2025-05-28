package org.service;

import org.data.Product;
import org.data.Cashier;
import org.data.Receipt;
import org.exception.InsufficientQuantityException;

import java.util.List;
import java.util.Map;

public interface StoreService {

    // Product management
    void addProduct(Product product);

    List<Product> getDeliveredProducts();

    // Cashier management
    void addCashier(Cashier cashier);

    List<Cashier> getCashiers();

    void assignCashierToRegister(Cashier cashier, int registerNumber);

    // Sales operations
    Receipt createSale(int registerNumber, Map<Integer, Integer> purchase)
            throws InsufficientQuantityException;

    // Financial reporting
    double getTotalRevenue();

    double getSalaryExpenses();

    double getDeliveryExpenses();

    double getIncome();

    double getProfit();

    int getTotalReceipts();
}