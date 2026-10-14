package kz.ramazan.orders.services;

import kz.ramazan.orders.errors.InventoryException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryService {

    private final Map<String, Integer> stockBySku = new ConcurrentHashMap<>();

    public InventoryService() {
        stockBySku.put("BOOK-001", 5);
        stockBySku.put("GADGET-X", 2);
        stockBySku.put("MUG-777", 100);
    }

    public void addStock(String sku, int quantity) {
        stockBySku.merge(sku, quantity, Integer::sum);
    }

    public double reserve(String sku, int qty) {
        if (qty <= 0) throw new InventoryException("qty must be > 0");

        Integer available = stockBySku.get(sku);
        if (available == null) {
            throw new InventoryException("Unknown SKU: " + sku);
        }
        if (available < qty) {
            throw new InventoryException(
                    "Insufficient stock for " + sku + " (requested " + qty
                            + ", available " + available + ")");
        }
        stockBySku.put(sku, available - qty);
        return priceOf(sku) * qty;
    }

    public void release(String sku, int qty) {
        stockBySku.merge(sku, qty, Integer::sum);
    }

    public int available(String sku) {
        return stockBySku.getOrDefault(sku, 0);
    }

    private double priceOf(String sku) {
        return switch (sku) {
            case "BOOK-001" -> 4500.0;
            case "GADGET-X" -> 25990.0;
            case "MUG-777"  -> 1800.0;
            default         -> throw new InventoryException("No price for SKU " + sku);
        };
    }
}