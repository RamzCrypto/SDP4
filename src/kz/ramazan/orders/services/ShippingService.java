package kz.ramazan.orders.services;

import kz.ramazan.orders.domain.Customer;

import java.util.List;

public class ShippingService {

    private int counter = 0;

    public String scheduleShipment(Customer customer,
                                   List<String> skus,
                                   String transactionId) {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalStateException("Cannot ship without a captured payment");
        }
        counter++;
        return "TRK-" + String.format("%05d", counter) + "-" + customer.id();
    }

    public void cancelShipment(String trackingNumber) {
    }
}