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
import org.service.PricingService;
import org.exception.ExpiredProductException;
import org.exception.InsufficientQuantityException;
import org.exception.NoAssignedCashierException;
import org.exception.ProductNotFoundException;
import org.exception.RegisterAlreadyAssignedException;
import org.exception.NegativePercentageException;
import org.exception.ReceiptPersistenceException;

import java.io.IOException;
import java.util.*;

public class StoreServiceImpl implements StoreService {
    private final ProductService productService;
    private final CashierService cashierService;
    private final ReceiptService receiptService;
    private final PricingService pricingService;
    private final Store store;
    private final StoreConfig config;

    public StoreServiceImpl(Store store, StoreConfig config, 
            ProductService productService, CashierService cashierService, 
            ReceiptService receiptService, PricingService pricingService) {
        this.store = store;
        this.config = config;
        this.productService = productService;
        this.cashierService = cashierService;
        this.receiptService = receiptService;
        this.pricingService = pricingService;
    }

    public StoreServiceImpl(double foodMarkup, double nonFoodMarkup,
            int expirationThreshold, double expirationDiscount) {
        StoreConfig config = new StoreConfig();
        Store store = new Store("Default Store", "Default Address", foodMarkup, nonFoodMarkup,
                expirationThreshold, expirationDiscount);
        ProductService productService = new ProductServiceImpl(expirationThreshold, expirationDiscount);
        CashierService cashierService = new CashierServiceImpl();
        ReceiptService receiptService = new ReceiptServiceImpl(config);
        PricingService pricingService = new PricingServiceImpl(productService, expirationThreshold, expirationDiscount);
        
        this.store = store;
        this.config = config;
        this.productService = productService;
        this.cashierService = cashierService;
        this.receiptService = receiptService;
        this.pricingService = pricingService;
    }

    public StoreServiceImpl(Store store) {
        this(store, new StoreConfig());
    }

    public StoreServiceImpl(Store store, StoreConfig config) {
        ProductService productService = new ProductServiceImpl(store.getExpirationThreshold(), store.getExpirationDiscount());
        CashierService cashierService = new CashierServiceImpl();
        ReceiptService receiptService = new ReceiptServiceImpl(config);
        PricingService pricingService = new PricingServiceImpl(productService, store.getExpirationThreshold(), store.getExpirationDiscount());
        
        this.store = store;
        this.config = config;
        this.productService = productService;
        this.cashierService = cashierService;
        this.receiptService = receiptService;
        this.pricingService = pricingService;
    }

    @Override
    public void addProduct(Product product) {
        productService.addProduct(product);
        if (pricingService instanceof PricingServiceImpl) {
            ((PricingServiceImpl) pricingService).addProductDeliveryExpense(product);
        }
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
    }

    @Override
    public Cashier getCashierAtRegister(int registerNumber) {
        return cashierService.getCashierAtRegister(registerNumber);
    }

    @Override
    public boolean isRegisterAssigned(int registerNumber) {
        return cashierService.getCashierAtRegister(registerNumber) != null;
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
            if (pricingService.isProductExpired(product.getId())) {
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
            double price = pricingService.calculateSellingPrice(product.getId(), markup);

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
        return pricingService.getTotalDeliveryExpenses();
    }

    @Override
    public double getIncome() {
        return getTotalRevenue() - getDeliveryExpenses();
    }

    @Override
    public double getProfit() {
        return getIncome() - getSalaryExpenses();
    }

    @Override
    public int getTotalReceipts() {
        return receiptService.getTotalReceipts();
    }

    @Override
    public Receipt loadReceiptFromFile(int receiptNumber) throws IOException, ClassNotFoundException, ReceiptPersistenceException {
        String filePath = receiptService.getPersistenceService().getSerializedFilePath(receiptNumber);
        return receiptService.deserializeReceiptFromFile(filePath);
    }

    public Store getStore() {
        return store;
    }

    public ReceiptService getReceiptService() {
        return receiptService;
    }

    public double calculateSellingPrice(int productId, double markup) throws ProductNotFoundException, NegativePercentageException {
        return pricingService.calculateSellingPrice(productId, markup);
    }
}
