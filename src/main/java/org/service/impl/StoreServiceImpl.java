package org.service.impl;

import org.data.Product;
import org.data.Cashier;
import org.data.Receipt;
import org.data.ProductCategory;
import org.service.StoreService;
import org.service.ProductService;
import org.service.CashierService;
import org.service.ReceiptService;
import org.exception.InsufficientQuantityException;

import java.util.*;

public class StoreServiceImpl implements StoreService {
    private final ProductService productService;
    private final CashierService cashierService;
    private final ReceiptService receiptService;
    private final double foodMarkup;
    private final double nonFoodMarkup;
    private final int expirationThreshold;
    private final double expirationDiscount;
    private final Map<Integer, Cashier> registerAssignments;

    public StoreServiceImpl(double foodMarkup, double nonFoodMarkup,
            int expirationThreshold, double expirationDiscount) {
        this.productService = new ProductServiceImpl(expirationThreshold, expirationDiscount);
        this.cashierService = new CashierServiceImpl();
        this.receiptService = new ReceiptServiceImpl();
        this.foodMarkup = foodMarkup;
        this.nonFoodMarkup = nonFoodMarkup;
        this.expirationThreshold = expirationThreshold;
        this.expirationDiscount = expirationDiscount;
        this.registerAssignments = new HashMap<>();
    }

    @Override
    public void addProduct(Product product) {
        productService.addProduct(product);
    }

    @Override
    public List<Product> getDeliveredProducts() {
        return productService.getAllProducts();
    }

    @Override
    public void addCashier(Cashier cashier) {
        cashierService.addCashier(cashier);
    }

    @Override
    public List<Cashier> getCashiers() {
        return cashierService.getAllCashiers();
    }

    @Override
    public void assignCashierToRegister(Cashier cashier, int registerNumber) {
        if (registerAssignments.containsKey(registerNumber)) {
            throw new IllegalStateException("Register " + registerNumber + " is already assigned to a cashier");
        }
        cashierService.assignCashierToRegister(cashier.getId(), registerNumber);
        registerAssignments.put(registerNumber, cashier);
    }

    @Override
    public Receipt createSale(int registerNumber, Map<Integer, Integer> purchase)
            throws InsufficientQuantityException {
        // Validate register assignment
        Cashier cashier = registerAssignments.get(registerNumber);
        if (cashier == null) {
            throw new IllegalStateException("No cashier assigned to register " + registerNumber);
        }

        // Validate quantities
        for (Map.Entry<Integer, Integer> entry : purchase.entrySet()) {
            Product product = productService.getProduct(entry.getKey());
            if (product == null) {
                throw new IllegalArgumentException("Product not found: " + entry.getKey());
            }
            if (product.isExpired()) {
                throw new IllegalStateException("Cannot sell expired product: " + product.getName());
            }
            if (product.getQuantity() < entry.getValue()) {
                throw new InsufficientQuantityException(product, entry.getValue());
            }
        }

        // Calculate total and update inventory
        double totalAmount = 0;
        Map<Product, Integer> soldItems = new HashMap<>();

        for (Map.Entry<Integer, Integer> entry : purchase.entrySet()) {
            Product product = productService.getProduct(entry.getKey());
            int quantity = entry.getValue();

            // Calculate price with appropriate markup
            double markup = product.getCategory() == ProductCategory.FOOD ? foodMarkup : nonFoodMarkup;
            double price = productService.calculateSellingPrice(product.getId(), markup);

            totalAmount += price * quantity;
            soldItems.put(product, quantity);

            // Update inventory
            productService.updateProductQuantity(
                    product.getId(),
                    product.getQuantity() - quantity);
        }

        // Create and save receipt
        return receiptService.createReceipt(cashier, registerNumber, soldItems, totalAmount);
    }

    @Override
    public double getTotalRevenue() {
        return receiptService.getTotalRevenue();
    }

    @Override
    public double getSalaryExpenses() {
        return cashierService.getTotalSalaryExpenses();
    }

    @Override
    public double getDeliveryExpenses() {
        if (productService instanceof ProductServiceImpl) {
            return ((ProductServiceImpl) productService).getTotalDeliveryExpenses();
        }
        return 0.0;
    }

    @Override
    public double getIncome() {
        return getTotalRevenue();
    }

    @Override
    public double getProfit() {
        return getIncome() - getSalaryExpenses();
    }

    @Override
    public int getTotalReceipts() {
        return receiptService.getTotalReceipts();
    }

    public double calculateSellingPrice(int productId, double markup) {
        Product product = productService.getProduct(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }

        double basePrice = product.getDeliveryPrice();
        double sellingPrice = basePrice * (1 + markup);

        // Apply expiration discount if needed
        if (product.isNearExpiration(expirationThreshold)) {
            sellingPrice *= (1 - expirationDiscount);
        }

        return sellingPrice;
    }

    // Load a receipt from file by its number
    public Receipt loadReceiptFromFile(int receiptNumber) throws Exception {
        String filePath = "output/receipts/receipt_" + receiptNumber + ".ser";
        return Receipt.deserializeFromFile(filePath);
    }
}
