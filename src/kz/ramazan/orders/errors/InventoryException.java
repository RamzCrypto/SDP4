package kz.ramazan.orders.errors;

public class InventoryException extends RuntimeException {
    public InventoryException(String message) { super(message); }
}