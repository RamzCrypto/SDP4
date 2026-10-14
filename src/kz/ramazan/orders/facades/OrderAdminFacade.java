package kz.ramazan.orders.facades;

import kz.ramazan.orders.services.InventoryService;

import java.util.List;

public class OrderAdminFacade {

    private final InventoryService inventory;

    public OrderAdminFacade(InventoryService inventory) {
        this.inventory = inventory;
    }

    public String stockReport(List<String> skus) {
        StringBuilder sb = new StringBuilder("Stock report:\n");
        for (String sku : skus) {
            sb.append("  ").append(sku)
                    .append(" = ").append(inventory.available(sku)).append('\n');
        }
        return sb.toString();
    }
}