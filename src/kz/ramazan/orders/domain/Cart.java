package kz.ramazan.orders.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Cart {

    private final List<CartItem> items;

    public Cart(List<CartItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("cart must not be empty");
        }
        this.items = List.copyOf(items);
    }

    public List<CartItem> items() { return Collections.unmodifiableList(items); }

    public List<String> skus() {
        List<String> skus = new ArrayList<>();
        for (CartItem item : items) skus.add(item.sku());
        return skus;
    }
}