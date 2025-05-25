package org.service;

import org.model.*;
import org.exception.InsufficientQuantityException;

import java.util.List;
import java.util.Map;

public interface StoreService {
    
    void addProduct(Product product);
    List<Product> getDeliveredProducts();
    List<Product> getSoldProducts();
    
    void addCashier(Cashier cashier);
    void assignCashierToRegister(Cashier cashier, int registerNumber);
    List<Cashier> getCashiers();
    
    double calculateSellingPrice(Product product);
    
    Receipt createSale(int registerNumber, Map<Integer, Integer> productQuantities) 
            throws InsufficientQuantityException;
    
    List<Receipt> getReceipts();
    int getTotalReceipts();
    double getTotalRevenue();
    
    double getSalaryExpenses();
    double getDeliveryExpenses();
    double getIncome();
    double getProfit();
} 