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
    private static Scanner scanner;

    public static void main(String[] args) {
        try {
            scanner = new Scanner(System.in);
            StoreServiceImpl store = setupStore();
            processCustomers(store);
        } catch (InvalidInputException e) {
            System.err.println("Input Error: " + e.getMessage());
        } catch (InsufficientQuantityException e) {
            System.err.println("Stock Error: " + e.getMessage());
        } catch (ExpiredProductException e) {
            System.err.println("Product Error: " + e.getMessage());
        } catch (ProductNotFoundException e) {
            System.err.println("Product Error: " + e.getMessage());
        } catch (NoAssignedCashierException e) {
            System.err.println("Register Error: " + e.getMessage());
        } catch (RegisterAlreadyAssignedException e) {
            System.err.println("Register Error: " + e.getMessage());
        } catch (NegativeQuantityException e) {
            System.err.println("Quantity Error: " + e.getMessage());
        } catch (NegativePercentageException e) {
            System.err.println("Percentage Error: " + e.getMessage());
        } catch (ReceiptPersistenceException e) {
            System.err.println("Receipt Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    private static StoreServiceImpl setupStore() {
        System.out.println("=== STORE SETUP ===");
        
        String storeName = readString("Enter store name: ");
        String storeAddress = readString("Enter store address: ");
        
        double foodMarkup = readPercentage("Enter food markup percentage (e.g., 20 for 20%): ");
        double nonFoodMarkup = readPercentage("Enter non-food markup percentage (e.g., 30 for 30%): ");
        
        int expirationThreshold = readPositiveInt("Enter expiration threshold in days: ");
        double expirationDiscount = readPercentage("Enter expiration discount percentage (e.g., 15 for 15%): ");

        Store storeData = new Store(storeName, storeAddress, foodMarkup, nonFoodMarkup, 
                expirationThreshold, expirationDiscount);
        StoreServiceImpl store = new StoreServiceImpl(storeData);

        setupProducts(store);
        setupCashiers(store);

        return store;
    }

    private static void setupProducts(StoreServiceImpl store) {
        System.out.println("\n=== PRODUCT SETUP ===");
        int numProducts = readPositiveInt("How many products do you want to add? ");

        for (int i = 0; i < numProducts; i++) {
            System.out.println("\nProduct #" + (i + 1));
            String productName = readString("Enter product name: ");
            double deliveryPrice = readPositiveDouble("Enter delivery price: ");
            
            System.out.print("Enter category (1 for FOOD, 2 for NON_FOOD): ");
            int categoryChoice = readInt();
            if (categoryChoice != 1 && categoryChoice != 2) {
                throw new InvalidInputException("category", String.valueOf(categoryChoice), 
                    "Must be 1 for FOOD or 2 for NON_FOOD");
            }
            ProductCategory category = (categoryChoice == 1) ? ProductCategory.FOOD : ProductCategory.NON_FOOD;

            int expirationDays = readPositiveInt("Enter expiration days from now: ");
            int quantity = readPositiveInt("Enter quantity: ");

            Product product = new Product(productName, deliveryPrice, category,
                    LocalDate.now().plusDays(expirationDays), quantity);
            store.addProduct(product);
        }
    }

    private static void setupCashiers(StoreServiceImpl store) {
        System.out.println("\n=== CASHIER SETUP ===");
        String cashierName = readString("Enter cashier name: ");
        double cashierSalary = readPositiveDouble("Enter cashier monthly salary: ");
        int registerNumber = readPositiveInt("Enter register number to assign: ");

        Cashier cashier = new Cashier(cashierName, cashierSalary);
        store.addCashier(cashier);
        store.assignCashierToRegister(cashier, registerNumber);
    }

    private static void processCustomers(StoreServiceImpl store) {
        System.out.println("=== STORE MANAGEMENT SYSTEM DEMO ===\n");
        displayInitialState(store);

        System.out.println("\n=== CUSTOMER PROCESSING ===");
        int numCustomers = readPositiveInt("How many customers do you want to process? ");

        for (int i = 0; i < numCustomers; i++) {
            processCustomer(store, i + 1);
        }

        displayFinalState(store);
    }

    private static void processCustomer(StoreServiceImpl store, int customerNumber) {
        System.out.println("\n=== CUSTOMER #" + customerNumber + " ===");
        int saleRegisterNumber = readPositiveInt("Enter register number for this sale: ");

        Map<Integer, Integer> purchase = new HashMap<>();
        int numProductTypes = readPositiveInt("How many different products is this customer buying? ");

        System.out.println("Customer wants to buy:");
        for (int j = 0; j < numProductTypes; j++) {
            int productId = readPositiveInt("Enter product ID: ");
            int quantity = readPositiveInt("Enter quantity: ");
            purchase.put(productId, quantity);

            Product product = store.getDeliveredProducts().stream()
                    .filter(p -> p.getId() == productId)
                    .findFirst()
                    .orElseThrow(() -> new ProductNotFoundException(productId));

            System.out.println("- " + quantity + "x " + product.getName() + 
                    (product.isNearExpiration(store.getStore().getExpirationThreshold()) ? 
                    " (near expiration - discount applied!)" : ""));
        }

        try {
            Receipt receipt = store.createSale(saleRegisterNumber, purchase);
            displayReceipt(receipt, store);
            displayUpdatedInventory(store);
        } catch (InsufficientQuantityException | ExpiredProductException | 
                ProductNotFoundException | NoAssignedCashierException e) {
            System.err.println("Sale failed: " + e.getMessage());
        }
    }

    private static void displayInitialState(StoreServiceImpl store) {
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
    }

    private static void displayReceipt(Receipt receipt, StoreServiceImpl store) {
        System.out.println("\n=== RECEIPT #" + receipt.getReceiptNumber() + " GENERATED ===");
        System.out.println("Receipt saved to: output/receipts/receipt_" + receipt.getReceiptNumber() + ".txt");
        System.out.println("Serialized to: output/receipts/receipt_" + receipt.getReceiptNumber() + ".ser");

        System.out.println("\nReceipt Details:");
        System.out.println("Receipt #" + receipt.getReceiptNumber());
        System.out.println("Cashier: " + receipt.getCashier().getName());
        System.out.printf("Total Amount: %.2f BGN\n", receipt.getTotalAmount());

        try {
            Receipt loaded = store.loadReceiptFromFile(receipt.getReceiptNumber());
            System.out.println("\n=== LOADED RECEIPT FROM FILE ===");
            System.out.println(loaded);
        } catch (IOException | ClassNotFoundException | ReceiptPersistenceException e) {
            System.err.println("Failed to load receipt: " + e.getMessage());
        }
    }

    private static void displayUpdatedInventory(StoreServiceImpl store) {
        System.out.println("\n=== UPDATED INVENTORY ===");
        for (Product product : store.getDeliveredProducts()) {
            System.out.printf("- %s: %d units remaining\n",
                    product.getName(), product.getQuantity());
        }
    }

    private static void displayFinalState(StoreServiceImpl store) {
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
    }

    // Input validation helper methods
    private static String readString(String prompt) {
        while (true) {
            System.out.print(prompt);
            System.out.flush();
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("Input cannot be empty. Please try again.");
        }
    }

    private static double readPercentage(String prompt) {
        while (true) {
            double value = readPositiveDouble(prompt);
            if (value <= 100) {
                return value / 100;
            }
            System.out.println("Percentage cannot be greater than 100%. Please try again.");
        }
    }

    private static double readPositiveDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                System.out.flush();
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value > 0) {
                    return value;
                }
                System.out.println("Value must be greater than 0. Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static int readPositiveInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                System.out.flush();
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value > 0) {
                    return value;
                }
                System.out.println("Value must be greater than 0. Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                System.out.flush();
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }
}
