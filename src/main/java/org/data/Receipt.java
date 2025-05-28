package org.data;

import java.io.Serializable;
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
    private final int registerNumber;
    private final Map<Product, Integer> items;
    private final double totalAmount;

    public Receipt(Cashier cashier, int registerNumber, Map<Product, Integer> items, double totalAmount) {
        synchronized (receiptNumberLock) {
            this.receiptNumber = nextReceiptNumber++;
        }
        this.date = LocalDateTime.now();
        this.cashier = cashier;
        this.registerNumber = registerNumber;
        this.items = new HashMap<>(items);
        this.totalAmount = totalAmount;
    }

    public Receipt(int receiptNumber, Cashier cashier, int registerNumber, Map<Product, Integer> items, double totalAmount) {
        this.receiptNumber = receiptNumber;
        this.date = LocalDateTime.now();
        this.cashier = cashier;
        this.registerNumber = registerNumber;
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

    public int getRegisterNumber() {
        return registerNumber;
    }

    public Map<Product, Integer> getItems() {
        return new HashMap<>(items);
    }

    public double getTotalAmount() {
        return totalAmount;
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
            sb.append(String.format("%s x%d - %.2f BGN\n",
                    entry.getKey().getName(),
                    entry.getValue(),
                    entry.getKey().getDeliveryPrice() * entry.getValue()));
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
