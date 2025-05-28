package org;

import org.data.*;
import org.service.StoreService;
import org.service.impl.StoreServiceImpl;
import org.exception.InsufficientQuantityException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        StoreService store = new StoreServiceImpl(
                0.20, // 20% markup for food
                0.30, // 30% markup for non-food
                7, // 7 days threshold for expiration discount
                0.15 // 15% discount for near-expiration items
        );

        // Add some products
        Product milk = new Product(1, "Milk", 2.0, ProductCategory.FOOD,
                LocalDate.now().plusDays(5), 10);
        Product bread = new Product(2, "Bread", 1.5, ProductCategory.FOOD,
                LocalDate.now().plusDays(3), 15);
        Product soap = new Product(3, "Soap", 3.0, ProductCategory.NON_FOOD,
                LocalDate.now().plusDays(365), 20);

        store.addProduct(milk);
        store.addProduct(bread);
        store.addProduct(soap);

        Cashier cashier = new Cashier(1, "John Doe", 1500.0);
        store.addCashier(cashier);
        store.assignCashierToRegister(cashier, 1);

        System.out.println("=== STORE MANAGEMENT SYSTEM DEMO ===\n");

        // Display initial inventory
        System.out.println("Initial Inventory:");
        for (Product product : store.getDeliveredProducts()) {
            System.out.printf("- %s: %d units @ %.2f BGN (expires: %s)\n",
                    product.getName(), product.getQuantity(),
                    product.getDeliveryPrice(), product.getExpirationDate());
        }

        System.out.println("\nCashiers:");
        for (Cashier c : store.getCashiers()) {
            System.out.printf("- %s (Register %d, Salary: %.2f BGN)\n",
                    c.getName(), c.getRegisterNumber(), c.getMonthlySalary());
        }

        try {
            Map<Integer, Integer> purchase = new HashMap<>();
            purchase.put(1, 2); // 2 units of milk
            purchase.put(2, 1); // 1 unit of bread
            purchase.put(3, 3); // 3 units of soap

            System.out.println("\n=== PROCESSING SALE ===");
            System.out.println("Customer wants to buy:");
            System.out.println("- 2x Milk");
            System.out.println("- 1x Bread (near expiration - discount applied!)");
            System.out.println("- 3x Soap");

            Receipt receipt = store.createSale(1, purchase);

            System.out.println("\n=== RECEIPT GENERATED ===");
            System.out.println("Receipt saved to: output/receipts/receipt_" + receipt.getReceiptNumber() + ".txt");
            System.out.println("Serialized to: output/receipts/receipt_" + receipt.getReceiptNumber() + ".ser");

            System.out.println("\nReceipt Details:");
            System.out.println("Receipt #" + receipt.getReceiptNumber());
            System.out.println("Cashier: " + receipt.getCashier().getName());
            System.out.printf("Total Amount: %.2f BGN\n", receipt.getTotalAmount());

            // === DEMO: Load and print the receipt from file ===
            if (store instanceof org.service.impl.StoreServiceImpl) {
                try {
                    Receipt loaded = ((org.service.impl.StoreServiceImpl) store).loadReceiptFromFile(receipt.getReceiptNumber());
                    System.out.println("\n=== LOADED RECEIPT FROM FILE ===");
                    System.out.println(loaded);
                } catch (Exception e) {
                    System.out.println("Failed to load receipt from file: " + e.getMessage());
                }
            }

            System.out.println("\n=== UPDATED INVENTORY ===");
            for (Product product : store.getDeliveredProducts()) {
                System.out.printf("- %s: %d units remaining\n",
                        product.getName(), product.getQuantity());
            }

            System.out.println("\n=== STORE FINANCIALS ===");
            System.out.printf("Total Revenue: %.2f BGN\n", store.getTotalRevenue());
            System.out.printf("Salary Expenses: %.2f BGN\n", store.getSalaryExpenses());
            System.out.printf("Delivery Expenses: %.2f BGN\n", store.getDeliveryExpenses());
            System.out.printf("Total Income: %.2f BGN\n", store.getIncome());
            System.out.printf("Net Profit: %.2f BGN\n", store.getProfit());
            System.out.printf("Total Receipts Issued: %d\n", store.getTotalReceipts());

            // === SECOND SALE ===
            System.out.println("\n=== SECOND CUSTOMER ===");
            Map<Integer, Integer> purchase2 = new HashMap<>();
            purchase2.put(1, 1); // 1 more milk
            purchase2.put(2, 3); // 3 bread (near expiration)

            System.out.println("Second customer wants to buy:");
            System.out.println("- 1x Milk");
            System.out.println("- 3x Bread (near expiration - discount applied!)");

            Receipt receipt2 = store.createSale(1, purchase2);

            System.out.println("\n=== SECOND RECEIPT GENERATED ===");
            System.out.println("Receipt saved to: output/receipts/receipt_" + receipt2.getReceiptNumber() + ".txt");
            System.out.printf("Total Amount: %.2f BGN\n", receipt2.getTotalAmount());

            // === THIRD SALE ===
            System.out.println("\n=== THIRD CUSTOMER ===");
            Map<Integer, Integer> purchase3 = new HashMap<>();
            purchase3.put(1, 1); // 1 more milk
            purchase3.put(3, 2); // 2 more soap

            System.out.println("Third customer wants to buy:");
            System.out.println("- 1x Milk");
            System.out.println("- 2x Soap");

            Receipt receipt3 = store.createSale(1, purchase3);

            System.out.println("\n=== THIRD RECEIPT GENERATED ===");
            System.out.println("Receipt saved to: output/receipts/receipt_" + receipt3.getReceiptNumber() + ".txt");
            System.out.printf("Total Amount: %.2f BGN\n", receipt3.getTotalAmount());

            // === FOURTH SALE ===
            System.out.println("\n=== FOURTH CUSTOMER ===");
            Map<Integer, Integer> purchase4 = new HashMap<>();
            purchase4.put(2, 2); // 2 more bread (near expiration)
            purchase4.put(3, 1); // 1 more soap

            System.out.println("Fourth customer wants to buy:");
            System.out.println("- 2x Bread (near expiration - discount applied!)");
            System.out.println("- 1x Soap");

            Receipt receipt4 = store.createSale(1, purchase4);

            System.out.println("\n=== FOURTH RECEIPT GENERATED ===");
            System.out.println("Receipt saved to: output/receipts/receipt_" + receipt4.getReceiptNumber() + ".txt");
            System.out.printf("Total Amount: %.2f BGN\n", receipt4.getTotalAmount());

            // === FINAL STORE STATUS ===
            System.out.println("\n=== FINAL STORE STATUS ===");
            System.out.printf("Total Revenue: %.2f BGN\n", store.getTotalRevenue());
            System.out.printf("Net Profit: %.2f BGN\n", store.getProfit());
            System.out.printf("Total Receipts Issued: %d\n", store.getTotalReceipts());

            System.out.println("\nFinal Inventory:");
            for (Product product : store.getDeliveredProducts()) {
                System.out.printf("- %s: %d units remaining\n",
                        product.getName(), product.getQuantity());
            }

            // === TESTING INSUFFICIENT QUANTITY ===
            System.out.println("\n=== TESTING INSUFFICIENT QUANTITY ===");
            Map<Integer, Integer> purchase5 = new HashMap<>();
            purchase5.put(1, 20); // Try to buy 20 milk when only 15 available
            try {
                store.createSale(1, purchase5);
            } catch (InsufficientQuantityException e) {
                System.out.println("Error: " + e.getMessage());
            }

            // === TESTING PROFIT CALCULATION ===
            System.out.println("\n=== TESTING PROFIT CALCULATION ===");
            double income = store.getIncome();
            double salaryExpenses = store.getSalaryExpenses();
            double profit = store.getProfit();
            System.out.printf("Income: %.2f BGN\n", income);
            System.out.printf("Salary Expenses: %.2f BGN\n", salaryExpenses);
            System.out.printf("Profit: %.2f BGN\n", profit);
            System.out.printf("Expected Profit: %.2f BGN\n", income - salaryExpenses);

        } catch (InsufficientQuantityException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}