package kz.ramazan.orders.errors;

public class PaymentException extends RuntimeException {
    public PaymentException(String message) { super(message); }
}