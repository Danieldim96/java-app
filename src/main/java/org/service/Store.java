package org.service;

import org.model.*;
import org.util.InsufficientQuantityException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

public class Store {
    private static final String OUTPUT_DIR = "output/receipts";
    
    private Map<Integer, Product> products;
    private List<Product> soldProducts;
    private List<Cashier> cashiers;
    private Map<Integer, Cashier> registerAssignments;
    private List<Receipt> receipts;
    private double foodMarkupPercentage;
    private double nonFoodMarkupPercentage;
    private int expirationDaysThreshold;
    private double expirationDiscountPercentage;

    public Store(double foodMarkupPercentage, double nonFoodMarkupPercentage,
                int expirationDaysThreshold, double expirationDiscountPercentage) {
        this.products = new HashMap<>();
        this.soldProducts = new ArrayList<>();
        this.cashiers = new ArrayList<>();
        this.registerAssignments = new HashMap<>();
        this.receipts = new ArrayList<>();
        this.foodMarkupPercentage = foodMarkupPercentage;
        this.nonFoodMarkupPercentage = nonFoodMarkupPercentage;
        this.expirationDaysThreshold = expirationDaysThreshold;
        this.expirationDiscountPercentage = expirationDiscountPercentage;
        
        createOutputDirectory();
    }

    public void addProduct(Product product) {
        products.put(product.getId(), product);
    }

    public void addCashier(Cashier cashier) {
        cashiers.add(cashier);
    }

    public void assignCashierToRegister(Cashier cashier, int registerNumber) {
        if (registerAssignments.containsKey(registerNumber)) {
            throw new IllegalStateException("Register " + registerNumber + " is already assigned");
        }
        cashier.setRegisterNumber(registerNumber);
        registerAssignments.put(registerNumber, cashier);
    }

    public double calculateSellingPrice(Product product) {
        if (product.isExpired()) {
            throw new IllegalStateException("Cannot sell expired product: " + product.getName());
        }

        double markup = product.getCategory() == ProductCategory.FOOD ? 
            foodMarkupPercentage : nonFoodMarkupPercentage;
        
        double price = product.getDeliveryPrice() * (1 + markup / 100);

        if (product.isNearExpiration(expirationDaysThreshold)) {
            price *= (1 - expirationDiscountPercentage / 100);
        }

        return price;
    }

    public Receipt createSale(int registerNumber, Map<Integer, Integer> productQuantities) 
            throws InsufficientQuantityException {
        Cashier cashier = registerAssignments.get(registerNumber);
        if (cashier == null) {
            throw new IllegalStateException("No cashier assigned to register " + registerNumber);
        }

        Receipt receipt = new Receipt(cashier);

        for (Map.Entry<Integer, Integer> entry : productQuantities.entrySet()) {
            Product product = products.get(entry.getKey());
            if (product == null) {
                throw new IllegalArgumentException("Product not found: " + entry.getKey());
            }

            int requestedQuantity = entry.getValue();
            if (product.getQuantity() < requestedQuantity) {
                throw new InsufficientQuantityException(product, requestedQuantity);
            }

            double sellingPrice = calculateSellingPrice(product);
            receipt.addItem(product, requestedQuantity, sellingPrice);
            product.setQuantity(product.getQuantity() - requestedQuantity);
            soldProducts.add(new Product(product.getId(), product.getName(), product.getDeliveryPrice(), product.getCategory(), product.getExpirationDate(), requestedQuantity));
        }

        saveReceiptToFile(receipt);
        saveReceiptSerialized(receipt);
        receipts.add(receipt);

        return receipt;
    }

    private void createOutputDirectory() {
        try {
            Path outputPath = Paths.get(OUTPUT_DIR);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
            }
        } catch (IOException e) {
            System.err.println("Error creating output directory: " + e.getMessage());
        }
    }

    private void saveReceiptToFile(Receipt receipt) {
        String fileName = OUTPUT_DIR + File.separator + "receipt_" + receipt.getReceiptNumber() + ".txt";
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(receipt.generateReceiptText());
        } catch (IOException e) {
            System.err.println("Error saving receipt to file: " + e.getMessage());
        }
    }

    private void saveReceiptSerialized(Receipt receipt) {
        String fileName = OUTPUT_DIR + File.separator + "receipt_" + receipt.getReceiptNumber() + ".ser";
        try {
            receipt.serializeToFile(fileName);
        } catch (IOException e) {
            System.err.println("Error serializing receipt: " + e.getMessage());
        }
    }

    public double getTotalRevenue() {
        return Receipt.getTotalRevenue();
    }

    public int getTotalReceipts() {
        return Receipt.getTotalReceipts();
    }

    public List<Receipt> getReceipts() {
        return receipts;
    }

    public List<Product> getDeliveredProducts() {
        return new ArrayList<>(products.values());
    }

    public List<Product> getSoldProducts() {
        return soldProducts;
    }

    public List<Cashier> getCashiers() {
        return cashiers;
    }

    public double getSalaryExpenses() {
        return cashiers.stream().mapToDouble(Cashier::getMonthlySalary).sum();
    }

    public double getDeliveryExpenses() {
        return products.values().stream().mapToDouble(p -> p.getDeliveryPrice() * p.getQuantity()).sum()
            + soldProducts.stream().mapToDouble(p -> p.getDeliveryPrice() * p.getQuantity()).sum();
    }

    public double getIncome() {
        return getTotalRevenue();
    }

    public double getProfit() {
        return getIncome() - getSalaryExpenses() - getDeliveryExpenses();
    }
} 