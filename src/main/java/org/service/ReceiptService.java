package org.service;

import org.data.Receipt;
import org.data.Cashier;
import org.data.Product;
import java.util.List;
import java.util.Map;

public interface ReceiptService {
    Receipt createReceipt(Cashier cashier, int registerNumber, Map<Product, Integer> items, double totalAmount);

    Receipt getReceipt(int receiptNumber);

    List<Receipt> getAllReceipts();

    void saveReceipt(Receipt receipt);

    double getTotalRevenue();

    int getTotalReceipts();
}