package org;

import org.model.*;
import org.service.Store;
import org.util.InsufficientQuantityException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Store store = new Store(
            20.0,  // 20% markup for food
            30.0,  // 30% markup for non-food
            7,     // 7 days threshold for expiration discount
            15.0   // 15% discount for near-expiration items
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

        try {
            Map<Integer, Integer> purchase = new HashMap<>();
            purchase.put(1, 2);  // 2 units of milk
            purchase.put(2, 1);  // 1 unit of bread
            purchase.put(3, 3);  // 3 units of soap

            Receipt receipt = store.createSale(1, purchase);
            System.out.println("Receipt generated successfully!");
            System.out.println("Total revenue: " + store.getTotalRevenue());
            System.out.println("Total receipts: " + store.getTotalReceipts());

        } catch (InsufficientQuantityException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
} 