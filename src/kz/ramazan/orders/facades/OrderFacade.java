package kz.ramazan.orders.facades;

import kz.ramazan.orders.domain.Cart;
import kz.ramazan.orders.domain.CartItem;
import kz.ramazan.orders.domain.Customer;
import kz.ramazan.orders.domain.Order;
import kz.ramazan.orders.errors.OrderPlacementException;
import kz.ramazan.orders.services.InventoryService;
import kz.ramazan.orders.services.NotificationService;
import kz.ramazan.orders.services.PaymentProcessor;
import kz.ramazan.orders.services.ShippingService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OrderFacade {

    private final InventoryService inventory;
    private final PaymentProcessor payments;
    private final ShippingService shipping;
    private final NotificationService notifications;

    private final Map<String, Order> activeOrders = new ConcurrentHashMap<>();

    public OrderFacade() {
        this(new InventoryService(), new PaymentProcessor(),
                new ShippingService(), new NotificationService());
    }

    public OrderFacade(InventoryService inventory,
                       PaymentProcessor payments,
                       ShippingService shipping,
                       NotificationService notifications) {
        this.inventory = inventory;
        this.payments = payments;
        this.shipping = shipping;
        this.notifications = notifications;
    }

    public Order placeOrder(Cart cart, Customer customer) {
        if (cart == null || customer == null) {
            throw new OrderPlacementException("cart and customer are required");
        }

        List<CartItem> reserved = new ArrayList<>();
        String transactionId = null;
        String trackingNumber = null;

        try {
            double total = 0.0;
            for (CartItem item : cart.items()) {
                total += inventory.reserve(item.sku(), item.quantity());
                reserved.add(item);
            }

            transactionId = payments.charge(customer, total);

            trackingNumber = shipping.scheduleShipment(
                    customer, cart.skus(), transactionId);

            Order order = new Order(
                    "ORD-" + UUID.randomUUID().toString().substring(0, 8),
                    trackingNumber, cart.skus(), total);
            notifications.sendOrderConfirmation(customer, order);
            activeOrders.put(order.orderId(), order);
            return order;

        } catch (RuntimeException ex) {
            rollback(reserved, transactionId, trackingNumber);
            throw new OrderPlacementException(
                    "Order could not be placed: " + ex.getMessage(), ex);
        }
    }

    public void cancelOrder(String orderId) {
        Order order = activeOrders.remove(orderId);
        if (order == null) return;

        if (order.trackingNumber() != null) {
            shipping.cancelShipment(order.trackingNumber());
        }
        for (String sku : order.skus()) {
            inventory.release(sku, 1);
        }
        notifications.sendOrderCancellation(null, orderId);
    }

    private void rollback(List<CartItem> reserved,
                          String transactionId,
                          String trackingNumber) {
        if (trackingNumber != null) shipping.cancelShipment(trackingNumber);
        if (transactionId != null)  payments.refund(transactionId);
        for (CartItem item : reserved) {
            inventory.release(item.sku(), item.quantity());
        }
    }
}