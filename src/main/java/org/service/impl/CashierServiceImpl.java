package org.service.impl;

import org.data.Cashier;
import org.service.CashierService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CashierServiceImpl implements CashierService {
    private final Map<Integer, Cashier> cashiers;
    private final Map<Integer, Integer> registerAssignments;

    public CashierServiceImpl() {
        this.cashiers = new HashMap<>();
        this.registerAssignments = new HashMap<>();
    }

    @Override
    public void addCashier(Cashier cashier) {
        cashiers.put(cashier.getId(), cashier);
    }

    @Override
    public Cashier getCashier(int id) {
        return cashiers.get(id);
    }

    @Override
    public List<Cashier> getAllCashiers() {
        return new ArrayList<>(cashiers.values());
    }

    @Override
    public void assignCashierToRegister(int cashierId, int registerNumber) {
        Cashier cashier = cashiers.get(cashierId);
        if (cashier == null) {
            throw new IllegalArgumentException("Cashier not found: " + cashierId);
        }
        if (registerAssignments.containsValue(registerNumber)) {
            throw new IllegalStateException("Register " + registerNumber + " is already assigned to another cashier.");
        }
        registerAssignments.put(cashierId, registerNumber);
        cashier.setRegisterNumber(registerNumber);
    }

    @Override
    public double getTotalSalaryExpenses() {
        return cashiers.values().stream()
                .mapToDouble(Cashier::getMonthlySalary)
                .sum();
    }
}