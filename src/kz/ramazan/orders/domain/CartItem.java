package kz.ramazan.orders.domain;

import java.util.Objects;

public final class CartItem {

    private final String sku;
    private final int quantity;

    public CartItem(String sku, int quantity) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("sku must not be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }
        this.sku = sku;
        this.quantity = quantity;
    }

    public String sku()   { return sku; }
    public int quantity() { return quantity; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CartItem other)) return false;
        return quantity == other.quantity && sku.equals(other.sku);
    }

    @Override
    public int hashCode() { return Objects.hash(sku, quantity); }

    @Override
    public String toString() { return quantity + "x " + sku; }
}