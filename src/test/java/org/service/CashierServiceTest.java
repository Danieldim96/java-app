package org.service;

import org.data.Cashier;
import org.service.impl.CashierServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CashierServiceTest {
    private CashierService cashierService;
    private Cashier john;
    private Cashier jane;

    @BeforeEach
    void setUp() {
        cashierService = new CashierServiceImpl();

        john = new Cashier(1, "John Doe", 1500.0);
        jane = new Cashier(2, "Jane Smith", 1600.0);

        cashierService.addCashier(john);
        cashierService.addCashier(jane);
    }

    @Test
    void testAddAndGetCashier() {
        Cashier retrievedJohn = cashierService.getCashier(1);
        assertNotNull(retrievedJohn);
        assertEquals("John Doe", retrievedJohn.getName());
        assertEquals(1500.0, retrievedJohn.getMonthlySalary());
    }

    @Test
    void testAssignCashierToRegister() {
        cashierService.assignCashierToRegister(1, 1);
        Cashier john = cashierService.getCashier(1);
        assertEquals(1, john.getRegisterNumber());
    }

    @Test
    void testGetTotalSalaryExpenses() {
        double totalSalary = cashierService.getTotalSalaryExpenses();
        assertEquals(3100.0, totalSalary); // 1500 + 1600
    }

    @Test
    void testGetAllCashiers() {
        assertEquals(2, cashierService.getAllCashiers().size());
    }
}