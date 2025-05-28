package org.service;

import org.data.Cashier;
import java.util.List;

public interface CashierService {
    void addCashier(Cashier cashier);

    Cashier getCashier(int id);

    List<Cashier> getAllCashiers();

    void assignCashierToRegister(int cashierId, int registerNumber);

    double getTotalSalaryExpenses();

    Cashier getCashierAtRegister(int registerNumber);
}