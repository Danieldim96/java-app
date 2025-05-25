package org.service.impl;

import org.model.*;
import org.service.StoreService;
import org.exception.InsufficientQuantityException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class StoreServiceImpl implements StoreService {
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

    public StoreServiceImpl(double foodMarkupPercentage, double nonFoodMarkupPercentage,
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

    @Override
    public void addProduct(Product product) {
        products.put(product.getId(), product);
    }

    @Override
    public void addCashier(Cashier cashier) {
        cashiers.add(cashier);
    }

    @Override
    public void assignCashierToRegister(Cashier cashier, int registerNumber) {
        if (registerAssignments.containsKey(registerNumber)) {
            throw new IllegalStateException("Register " + registerNumber + " is already assigned");
        }
        cashier.setRegisterNumber(registerNumber);
        registerAssignments.put(registerNumber, cashier);
    }

    @Override
    public double calculateSellingPrice(Product product) {
        if (product.isExpired()) {
            throw new IllegalStateException("Cannot sell expired product: " + product.getName());
        }

        double markup = product.getCategory() == ProductCategory.FOOD ? foodMarkupPercentage : nonFoodMarkupPercentage;

        double price = product.getDeliveryPrice() * (1 + markup / 100);

        if (product.isNearExpiration(expirationDaysThreshold)) {
            price *= (1 - expirationDiscountPercentage / 100);
        }

        return price;
    }

    @Override
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
            soldProducts.add(new Product(product.getId(), product.getName(), product.getDeliveryPrice(),
                    product.getCategory(), product.getExpirationDate(), requestedQuantity));
        }

        saveReceiptToFile(receipt);
        saveReceiptSerialized(receipt);
        receipts.add(receipt);

        return receipt;
    }

    @Override
    public List<Receipt> getReceipts() {
        return new ArrayList<>(receipts);
    }

    @Override
    public int getTotalReceipts() {
        return Receipt.getTotalReceipts();
    }

    @Override
    public double getTotalRevenue() {
        return Receipt.getTotalRevenue();
    }

    @Override
    public List<Product> getDeliveredProducts() {
        return new ArrayList<>(products.values());
    }

    @Override
    public List<Product> getSoldProducts() {
        return new ArrayList<>(soldProducts);
    }

    @Override
    public List<Cashier> getCashiers() {
        return new ArrayList<>(cashiers);
    }

    @Override
    public double getSalaryExpenses() {
        return cashiers.stream().mapToDouble(Cashier::getMonthlySalary).sum();
    }

    @Override
    public double getDeliveryExpenses() {
        return products.values().stream().mapToDouble(p -> p.getDeliveryPrice() * p.getQuantity()).sum()
                + soldProducts.stream().mapToDouble(p -> p.getDeliveryPrice() * p.getQuantity()).sum();
    }

    @Override
    public double getIncome() {
        return getTotalRevenue();
    }

    @Override
    public double getProfit() {
        return getIncome() - getSalaryExpenses() - getDeliveryExpenses();
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
}