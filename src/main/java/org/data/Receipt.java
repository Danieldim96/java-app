package org.data;

import java.io.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Receipt implements Serializable {
    private static final long serialVersionUID = 1L;
    private static volatile int nextReceiptNumber = 1;
    private static final Object receiptNumberLock = new Object();

    private final int receiptNumber;
    private final LocalDateTime date;
    private final Cashier cashier;
    private final Map<Product, Integer> items;
    private final double totalAmount;

    public Receipt(Cashier cashier, Map<Product, Integer> items, double totalAmount) {
        synchronized (receiptNumberLock) {
            this.receiptNumber = nextReceiptNumber++;
        }
        this.date = LocalDateTime.now();
        this.cashier = cashier;
        this.items = new HashMap<>(items);
        this.totalAmount = totalAmount;
    }

    public int getReceiptNumber() {
        return receiptNumber;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public Cashier getCashier() {
        return cashier;
    }

    public Map<Product, Integer> getItems() {
        return new HashMap<>(items);
    }

    public double getTotalAmount() {
        return totalAmount;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Receipt receipt = (Receipt) o;
        return receiptNumber == receipt.receiptNumber;
    }

    @Override
    public int hashCode() {
        return receiptNumber;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Receipt #").append(receiptNumber).append("\n");
        sb.append("Date: ").append(date).append("\n");
        sb.append("Cashier: ").append(cashier.getName()).append("\n");
        sb.append("Items:\n");
        for (Map.Entry<Product, Integer> entry : items.entrySet()) {
            sb.append(String.format("- %s x%d\n",
                    entry.getKey().getName(), entry.getValue()));
        }
        sb.append(String.format("Total: %.2f BGN", totalAmount));
        return sb.toString();
    }

    public static void resetReceiptCounter() {
        synchronized (receiptNumberLock) {
            nextReceiptNumber = 1;
        }
    }
}
