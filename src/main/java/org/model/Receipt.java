package org.model;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Receipt implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int totalReceipts = 0;
    private static double totalRevenue = 0.0;

    private int receiptNumber;
    private Cashier cashier;
    private LocalDateTime dateTime;
    private List<ReceiptItem> items;
    private double totalAmount;

    public Receipt(Cashier cashier) {
        this.receiptNumber = ++totalReceipts;
        this.cashier = cashier;
        this.dateTime = LocalDateTime.now();
        this.items = new ArrayList<>();
        this.totalAmount = 0.0;
    }

    public int getReceiptNumber() {
        return receiptNumber;
    }

    public Cashier getCashier() {
        return cashier;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void addItem(Product product, int quantity, double price) {
        items.add(new ReceiptItem(product, quantity, price));
        totalAmount += price * quantity;
        totalRevenue += price * quantity;
    }

    public String generateReceiptText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Receipt #").append(receiptNumber).append("\n");
        sb.append("Date: ").append(dateTime).append("\n");
        sb.append("Cashier: ").append(cashier.getName()).append("\n");
        sb.append("Items:\n");
        
        for (ReceiptItem item : items) {
            sb.append(String.format("%s x%d - %.2f BGN\n", 
                item.getProduct().getName(), 
                item.getQuantity(), 
                item.getPrice()));
        }
        
        sb.append("Total: ").append(String.format("%.2f BGN", totalAmount));
        return sb.toString();
    }

    public static int getTotalReceipts() {
        return totalReceipts;
    }

    public static double getTotalRevenue() {
        return totalRevenue;
    }

    public void serializeToFile(String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(this);
        }
    }

    public static Receipt deserializeFromFile(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (Receipt) ois.readObject();
        }
    }

    public static String readReceiptTextFromFile(String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    private static class ReceiptItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private Product product;
        private int quantity;
        private double price;

        public ReceiptItem(Product product, int quantity, double price) {
            this.product = product;
            this.quantity = quantity;
            this.price = price;
        }

        public Product getProduct() {
            return product;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getPrice() {
            return price;
        }
    }
} 