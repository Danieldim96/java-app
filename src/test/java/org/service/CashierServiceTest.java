package org.service;

import org.data.Cashier;
import org.service.impl.CashierServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CashierServiceTest {
    private CashierService cashierService;

    @BeforeEach
    void setUp() {
        cashierService = new CashierServiceImpl();

        Cashier.resetCashierCounter();

        Cashier john = new Cashier("John Doe", 1500.0);
        Cashier jane = new Cashier("Jane Smith", 1600.0);

        cashierService.addCashier(john);
        cashierService.addCashier(jane);
    }

    @Test
    void testAddAndGetCashier() {
        // Since we reset the counter, john should have ID 1
        Cashier retrievedJohn = cashierService.getCashier(1);
        assertNotNull(retrievedJohn);
        assertEquals("John Doe", retrievedJohn.getName());
        assertEquals(1500.0, retrievedJohn.getMonthlySalary());
    }

    @Test
    void testAssignCashierToRegister() {
        // Since we reset the counter, john should have ID 1
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
