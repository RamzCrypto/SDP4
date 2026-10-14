package kz.ramazan.orders.services;

import kz.ramazan.orders.domain.Customer;
import kz.ramazan.orders.domain.Order;

public class NotificationService {

    public void sendOrderConfirmation(Customer customer, Order order) {
        System.out.println("[Notification] Confirmation sent to "
                + customer.email() + " for order " + order.orderId());
    }

    public void sendOrderCancellation(Customer customer, String orderId) {
        System.out.println("[Notification] Cancellation sent for order " + orderId);
    }
}