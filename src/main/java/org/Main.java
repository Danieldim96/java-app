package org;

import org.data.*;
import org.service.impl.StoreServiceImpl;
import org.exception.*;
import java.io.IOException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            System.out.println("=== STORE SETUP ===");
            System.out.print("Enter store name: ");
            String storeName = scanner.nextLine();

            System.out.print("Enter store address: ");
            String storeAddress = scanner.nextLine();

            System.out.print("Enter food markup percentage (e.g., 20 for 20%): ");
            double foodMarkup = Double.parseDouble(scanner.nextLine()) / 100;

            System.out.print("Enter non-food markup percentage (e.g., 30 for 30%): ");
            double nonFoodMarkup = Double.parseDouble(scanner.nextLine()) / 100;

            System.out.print("Enter expiration threshold in days: ");
            int expirationThreshold = Integer.parseInt(scanner.nextLine());

            System.out.print("Enter expiration discount percentage (e.g., 15 for 15%): ");
            double expirationDiscount = Double.parseDouble(scanner.nextLine()) / 100;

            Store storeData = new Store(storeName, storeAddress, foodMarkup, nonFoodMarkup, 
                    expirationThreshold, expirationDiscount);
            StoreServiceImpl store = new StoreServiceImpl(storeData);

            System.out.println("\n=== PRODUCT SETUP ===");
            System.out.print("How many products do you want to add? ");
            int numProducts = Integer.parseInt(scanner.nextLine());

            for (int i = 0; i < numProducts; i++) {
                System.out.println("\nProduct #" + (i + 1));
                System.out.print("Enter product name: ");
                String productName = scanner.nextLine();

                System.out.print("Enter delivery price: ");
                double deliveryPrice = Double.parseDouble(scanner.nextLine());

                System.out.print("Enter category (1 for FOOD, 2 for NON_FOOD): ");
                int categoryChoice = Integer.parseInt(scanner.nextLine());
                ProductCategory category = (categoryChoice == 1) ? ProductCategory.FOOD : ProductCategory.NON_FOOD;

                System.out.print("Enter expiration days from now: ");
                int expirationDays = Integer.parseInt(scanner.nextLine());

                System.out.print("Enter quantity: ");
                int quantity = Integer.parseInt(scanner.nextLine());

                Product product = new Product(productName, deliveryPrice, category,
                        LocalDate.now().plusDays(expirationDays), quantity);
                store.addProduct(product);
            }

            System.out.println("\n=== CASHIER SETUP ===");
            System.out.print("Enter cashier name: ");
            String cashierName = scanner.nextLine();

            System.out.print("Enter cashier monthly salary: ");
            double cashierSalary = Double.parseDouble(scanner.nextLine());

            System.out.print("Enter register number to assign: ");
            int registerNumber = Integer.parseInt(scanner.nextLine());

            Cashier cashier = new Cashier(cashierName, cashierSalary);
            store.addCashier(cashier);
            store.assignCashierToRegister(cashier, registerNumber);

            System.out.println("=== STORE MANAGEMENT SYSTEM DEMO ===\n");

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
                System.out.println("\n=== CUSTOMER PROCESSING ===");
                System.out.print("How many customers do you want to process? ");
                int numCustomers = Integer.parseInt(scanner.nextLine());

                for (int i = 0; i < numCustomers; i++) {
                    System.out.println("\n=== CUSTOMER #" + (i + 1) + " ===");
                    System.out.print("Enter register number for this sale: ");
                    int saleRegisterNumber = Integer.parseInt(scanner.nextLine());

                    Map<Integer, Integer> purchase = new HashMap<>();
                    System.out.print("How many different products is this customer buying? ");
                    int numProductTypes = Integer.parseInt(scanner.nextLine());

                    System.out.println("Customer wants to buy:");
                    for (int j = 0; j < numProductTypes; j++) {
                        System.out.print("Enter product ID: ");
                        int productId = Integer.parseInt(scanner.nextLine());

                        System.out.print("Enter quantity: ");
                        int quantity = Integer.parseInt(scanner.nextLine());

                        purchase.put(productId, quantity);

                        Product product = store.getDeliveredProducts().stream()
                                .filter(p -> p.getId() == productId)
                                .findFirst()
                                .orElse(null);

                        if (product != null) {
                            System.out.println("- " + quantity + "x " + product.getName() + 
                                    (product.isNearExpiration(expirationThreshold) ? " (near expiration - discount applied!)" : ""));
                        }
                    }

                    Receipt receipt = store.createSale(saleRegisterNumber, purchase);

                    System.out.println("\n=== RECEIPT #" + (i + 1) + " GENERATED ===");
                    System.out.println("Receipt saved to: output/receipts/receipt_" + receipt.getReceiptNumber() + ".txt");
                    System.out.println("Serialized to: output/receipts/receipt_" + receipt.getReceiptNumber() + ".ser");

                    System.out.println("\nReceipt Details:");
                    System.out.println("Receipt #" + receipt.getReceiptNumber());
                    System.out.println("Cashier: " + receipt.getCashier().getName());
                    System.out.printf("Total Amount: %.2f BGN\n", receipt.getTotalAmount());

                    if (i == 0) {
                        try {
                            Receipt loaded = store.loadReceiptFromFile(receipt.getReceiptNumber());
                            System.out.println("\n=== LOADED RECEIPT FROM FILE ===");
                            System.out.println(loaded);
                        } catch (IOException e) {
                            System.out.println("Failed to load receipt from file (I/O error): " + e.getMessage());
                        } catch (ClassNotFoundException e) {
                            System.out.println("Failed to load receipt from file (class not found): " + e.getMessage());
                        } catch (ReceiptPersistenceException e) {
                            System.out.println("Failed to load receipt from file (persistence error): " + e.getMessage());
                        }
                    }

                    System.out.println("\n=== UPDATED INVENTORY ===");
                    for (Product product : store.getDeliveredProducts()) {
                        System.out.printf("- %s: %d units remaining\n",
                                product.getName(), product.getQuantity());
                    }
                }

                System.out.println("\n=== FINAL STORE STATUS ===");
                System.out.printf("Total Revenue: %.2f BGN\n", store.getTotalRevenue());
                System.out.printf("Salary Expenses: %.2f BGN\n", store.getSalaryExpenses());
                System.out.printf("Delivery Expenses: %.2f BGN\n", store.getDeliveryExpenses());
                System.out.printf("Total Income: %.2f BGN\n", store.getIncome());
                System.out.printf("Net Profit: %.2f BGN\n", store.getProfit());
                System.out.printf("Total Receipts Issued: %d\n", store.getTotalReceipts());

                System.out.println("\nFinal Inventory:");
                for (Product product : store.getDeliveredProducts()) {
                    System.out.printf("- %s: %d units remaining\n",
                            product.getName(), product.getQuantity());
                }

                // Test insufficient quantity scenario
                System.out.println("\n=== TESTING INSUFFICIENT QUANTITY ===");
                System.out.print("Enter a product ID to test insufficient quantity: ");
                int testProductId = Integer.parseInt(scanner.nextLine());

                System.out.print("Enter a large quantity (more than available): ");
                int largeQuantity = Integer.parseInt(scanner.nextLine());

                Map<Integer, Integer> testPurchase = new HashMap<>();
                testPurchase.put(testProductId, largeQuantity);

                try {
                    store.createSale(registerNumber, testPurchase);
                } catch (InsufficientQuantityException e) {
                    System.out.println("Error: " + e.getMessage());
                }

                System.out.println("\n=== TESTING PROFIT CALCULATION ===");
                double income = store.getIncome();
                double salaryExpenses = store.getSalaryExpenses();
                double profit = store.getProfit();
                System.out.printf("Income: %.2f BGN\n", income);
                System.out.printf("Salary Expenses: %.2f BGN\n", salaryExpenses);
                System.out.printf("Profit: %.2f BGN\n", profit);
                System.out.printf("Expected Profit: %.2f BGN\n", income - salaryExpenses);

            } catch (InsufficientQuantityException e) {
                System.err.println("Error (insufficient quantity): " + e.getMessage());
            } catch (NoAssignedCashierException e) {
                System.err.println("Error (no cashier assigned): " + e.getMessage());
            } catch (RegisterAlreadyAssignedException e) {
                System.err.println("Error (register already assigned): " + e.getMessage());
            } catch (ExpiredProductException e) {
                System.err.println("Error (expired product): " + e.getMessage());
            } catch (ProductNotFoundException e) {
                System.err.println("Error (product not found): " + e.getMessage());
            }
        } catch (NegativePercentageException e) {
            System.err.println("Error (negative percentage): " + e.getMessage());
        } catch (NegativeQuantityException e) {
            System.err.println("Error (negative quantity): " + e.getMessage());
        } catch (NegativeRegisterNumberException e) {
            System.err.println("Error (negative register number): " + e.getMessage());
        } catch (CashierNotFoundException e) {
            System.err.println("Error (cashier not found): " + e.getMessage());
        } catch (ReceiptPersistenceException e) {
            System.err.println("Error (receipt persistence): " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Error (runtime): " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
