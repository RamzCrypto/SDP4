package kz.ramazan.orders.services;

import kz.ramazan.orders.domain.Customer;
import kz.ramazan.orders.errors.PaymentException;

public class PaymentProcessor {

    private static final double PAYMENT_LIMIT = 30_000.0;

    public String charge(Customer customer, double amount) {
        if (customer == null) throw new PaymentException("customer is null");
        if (amount <= 0)      throw new PaymentException("amount must be > 0");

        if (amount > PAYMENT_LIMIT) {
            throw new PaymentException(
                    "Payment declined for " + customer.id()
                            + ": amount " + amount + " exceeds limit " + PAYMENT_LIMIT);
        }
        return "TXN-" + System.currentTimeMillis() + "-" + customer.id();
    }

    public void refund(String transactionId) {
        if (transactionId == null || transactionId.isBlank()) {
            throw new PaymentException("transactionId required for refund");
        }
    }
}