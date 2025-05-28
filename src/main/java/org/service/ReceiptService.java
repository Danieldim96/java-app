package org.service;

import org.data.Receipt;
import org.data.Cashier;
import org.data.Product;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ReceiptService {
    Receipt createReceipt(Cashier cashier, int registerNumber, Map<Product, Integer> items, double totalAmount);

    Receipt getReceipt(int receiptNumber);

    List<Receipt> getAllReceipts();

    void saveReceipt(Receipt receipt);

    Receipt deserializeReceiptFromFile(String filePath) throws IOException, ClassNotFoundException;

    String readReceiptTextFromFile(String filePath) throws IOException;

    void resetReceiptCounter();

    double getTotalRevenue();

    int getTotalReceipts();
}
