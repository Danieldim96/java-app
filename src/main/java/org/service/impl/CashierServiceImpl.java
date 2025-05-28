package org.service.impl;

import org.data.Cashier;
import org.exception.CashierNotFoundException;
import org.exception.RegisterAlreadyAssignedException;
import org.service.CashierService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CashierServiceImpl implements CashierService {
    private final Map<Integer, Cashier> cashiers;
    private final Map<Integer, Cashier> registerAssignments;

    public CashierServiceImpl() {
        this.cashiers = new ConcurrentHashMap<>();
        this.registerAssignments = new ConcurrentHashMap<>();
    }

    @Override
    public void addCashier(Cashier cashier) {
        cashiers.put(cashier.getId(), cashier);
    }

    @Override
    public Cashier getCashier(int id) {
        Cashier cashier = cashiers.get(id);
        if (cashier == null) {
            throw new CashierNotFoundException(id);
        }
        return cashier;
    }

    @Override
    public List<Cashier> getAllCashiers() {
        return new ArrayList<>(cashiers.values());
    }

    @Override
    public void assignCashierToRegister(int cashierId, int registerNumber) {
        Cashier cashier = getCashier(cashierId);
        
        if (registerAssignments.containsKey(registerNumber)) {
            throw new RegisterAlreadyAssignedException(registerNumber);
        }

        cashier.setRegisterNumber(registerNumber);
        registerAssignments.put(registerNumber, cashier);
    }

    @Override
    public double getTotalSalaryExpenses() {
        return cashiers.values().stream()
                .mapToDouble(Cashier::getSalary)
                .sum();
    }

    @Override
    public Cashier getCashierAtRegister(int registerNumber) {
        return registerAssignments.get(registerNumber);
    }
}
