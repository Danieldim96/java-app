package org.service;

import org.data.Cashier;
import org.service.impl.CashierServiceImpl;
import org.exception.CashierNotFoundException;
import org.exception.RegisterAlreadyAssignedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CashierServiceTest {
    private CashierService cashierService;

    @BeforeEach
    void setUp() {
        cashierService = new CashierServiceImpl();
    }

    @Nested
    class CashierManagementTests {
        @Test
        void testAddAndGetCashier() {
            Cashier cashier = new Cashier("John Doe", 1500.0);
            cashierService.addCashier(cashier);

            Cashier retrieved = cashierService.getCashier(cashier.getId());
            assertNotNull(retrieved);
            assertEquals(cashier.getId(), retrieved.getId());
            assertEquals(cashier.getName(), retrieved.getName());
            assertEquals(cashier.getSalary(), retrieved.getSalary());
        }

        @Test
        void testGetAllCashiers() {
            Cashier cashier1 = new Cashier("John Doe", 1500.0);
            Cashier cashier2 = new Cashier("Jane Smith", 1600.0);

            cashierService.addCashier(cashier1);
            cashierService.addCashier(cashier2);

            List<Cashier> cashiers = cashierService.getAllCashiers();
            assertEquals(2, cashiers.size());
            assertTrue(cashiers.stream().anyMatch(c -> c.getName().equals("John Doe")));
            assertTrue(cashiers.stream().anyMatch(c -> c.getName().equals("Jane Smith")));
        }
    }

    @Nested
    class RegisterAssignmentTests {
        @Test
        void testAssignCashierToRegister() {
            Cashier cashier = new Cashier("John Doe", 1500.0);
            cashierService.addCashier(cashier);

            cashierService.assignCashierToRegister(cashier.getId(), 1);
            Cashier assignedCashier = cashierService.getCashierAtRegister(1);
            assertNotNull(assignedCashier);
            assertEquals(cashier.getId(), assignedCashier.getId());
        }

        @Test
        void testRegisterAlreadyAssigned() {
            Cashier cashier1 = new Cashier("John Doe", 1500.0);
            Cashier cashier2 = new Cashier("Jane Smith", 1600.0);

            cashierService.addCashier(cashier1);
            cashierService.addCashier(cashier2);

            cashierService.assignCashierToRegister(cashier1.getId(), 1);

            assertThrows(RegisterAlreadyAssignedException.class, () -> {
                cashierService.assignCashierToRegister(cashier2.getId(), 1);
            });
        }

        @Test
        void testGetCashierAtUnassignedRegister() {
            assertNull(cashierService.getCashierAtRegister(1));
        }
    }

    @Nested
    class FinancialTests {
        @Test
        void testGetTotalSalaryExpenses() {
            Cashier cashier1 = new Cashier("John Doe", 1500.0);
            Cashier cashier2 = new Cashier("Jane Smith", 1600.0);

            cashierService.addCashier(cashier1);
            cashierService.addCashier(cashier2);

            double expectedTotal = 1500.0 + 1600.0;
            assertEquals(expectedTotal, cashierService.getTotalSalaryExpenses(), 0.01);
        }
    }

    @Nested
    class ErrorHandlingTests {
        @Test
        void testGetNonExistentCashier() {
            assertThrows(CashierNotFoundException.class, () -> {
                cashierService.getCashier(999);
            });
        }

        @Test
        void testAssignNonExistentCashier() {
            assertThrows(CashierNotFoundException.class, () -> {
                cashierService.assignCashierToRegister(999, 1);
            });
        }
    }
}
