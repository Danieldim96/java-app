package org.service;

import org.data.Product;
import org.data.Cashier;
import org.data.Receipt;
import org.exception.InsufficientQuantityException;

import java.util.List;
import java.util.Map;

public interface StoreService {

    void addProduct(Product product);

    List<Product> getDeliveredProducts();

    void addCashier(Cashier cashier);

    List<Cashier> getCashiers();

    void assignCashierToRegister(Cashier cashier, int registerNumber);

    Cashier getCashierAtRegister(int registerNumber);

    boolean isRegisterAssigned(int registerNumber);

    Receipt createSale(int registerNumber, Map<Integer, Integer> purchase)
            throws InsufficientQuantityException;

    double getTotalRevenue();

    double getSalaryExpenses();

    double getDeliveryExpenses();

    double getIncome();

    double getProfit();

    int getTotalReceipts();
}
