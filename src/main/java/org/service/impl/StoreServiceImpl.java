package org.service.impl;

import org.config.StoreConfig;
import org.data.Product;
import org.data.Cashier;
import org.data.Receipt;
import org.data.ProductCategory;
import org.data.Store;
import org.service.StoreService;
import org.service.ProductService;
import org.service.CashierService;
import org.service.ReceiptService;
import org.exception.ExpiredProductException;
import org.exception.InsufficientQuantityException;
import org.exception.NoAssignedCashierException;
import org.exception.ProductNotFoundException;
import org.exception.RegisterAlreadyAssignedException;

import java.io.IOException;
import java.util.*;

public class StoreServiceImpl implements StoreService {
    private final ProductService productService;
    private final CashierService cashierService;
    private final ReceiptService receiptService;
    private final Store store;
    private final StoreConfig config;

    public StoreServiceImpl(double foodMarkup, double nonFoodMarkup,
            int expirationThreshold, double expirationDiscount) {
        this(new Store("Default Store", "Default Address", foodMarkup, nonFoodMarkup,
                expirationThreshold, expirationDiscount), new StoreConfig());
    }

    public StoreServiceImpl(Store store) {
        this(store, new StoreConfig());
    }

    public StoreServiceImpl(double foodMarkup, double nonFoodMarkup,
            int expirationThreshold, double expirationDiscount, StoreConfig config) {
        this(new Store("Default Store", "Default Address", foodMarkup, nonFoodMarkup,
                expirationThreshold, expirationDiscount), config);
    }

    public StoreServiceImpl(Store store, StoreConfig config) {
        this.store = store;
        this.config = config;
        this.productService = new ProductServiceImpl(store.getExpirationThreshold(), store.getExpirationDiscount());
        this.cashierService = new CashierServiceImpl();
        this.receiptService = new ReceiptServiceImpl(config);
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
        if (isRegisterAssigned(registerNumber)) {
            throw new RegisterAlreadyAssignedException(registerNumber);
        }
        cashierService.assignCashierToRegister(cashier.getId(), registerNumber);
        store.assignCashierToRegister(registerNumber, cashier);
    }

    @Override
    public Cashier getCashierAtRegister(int registerNumber) {
        return store.getCashierAtRegister(registerNumber);
    }

    @Override
    public boolean isRegisterAssigned(int registerNumber) {
        return store.isRegisterAssigned(registerNumber);
    }

    @Override
    public Receipt createSale(int registerNumber, Map<Integer, Integer> purchase)
            throws InsufficientQuantityException {
        Cashier cashier = getCashierAtRegister(registerNumber);
        if (cashier == null) {
            throw new NoAssignedCashierException(registerNumber);
        }

        for (Map.Entry<Integer, Integer> entry : purchase.entrySet()) {
            Product product = productService.getProduct(entry.getKey());
            if (product == null) {
                throw new ProductNotFoundException(entry.getKey());
            }
            if (productService.isProductExpired(product.getId())) {
                throw new ExpiredProductException(product);
            }
            if (product.getQuantity() < entry.getValue()) {
                throw new InsufficientQuantityException(product, entry.getValue());
            }
        }

        double totalAmount = 0;
        Map<Product, Integer> soldItems = new HashMap<>();

        for (Map.Entry<Integer, Integer> entry : purchase.entrySet()) {
            Product product = productService.getProduct(entry.getKey());
            int quantity = entry.getValue();

            double markup = product.getCategory() == ProductCategory.FOOD ? store.getFoodMarkup() : store.getNonFoodMarkup();
            double price = productService.calculateSellingPrice(product.getId(), markup);

            totalAmount += price * quantity;
            soldItems.put(product, quantity);

            productService.updateProductQuantity(
                    product.getId(),
                    product.getQuantity() - quantity);
        }

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
            throw new ProductNotFoundException(productId);
        }

        double basePrice = product.getDeliveryPrice();
        double sellingPrice = basePrice * (1 + markup);

        if (productService.isProductNearExpiration(product.getId())) {
            sellingPrice *= (1 - store.getExpirationDiscount());
        }

        return sellingPrice;
    }

    public Receipt loadReceiptFromFile(int receiptNumber) throws IOException, ClassNotFoundException {
        String filePath = config.getReceiptOutputDir() + "/receipt_" + receiptNumber + ".ser";
        return receiptService.deserializeReceiptFromFile(filePath);
    }
}
