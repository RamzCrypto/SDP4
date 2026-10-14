package kz.ramazan.orders.domain;

import java.util.List;

public final class Order {

    private final String orderId;
    private final String trackingNumber;
    private final List<String> skus;
    private final double totalPaid;

    public Order(String orderId, String trackingNumber,
                 List<String> skus, double totalPaid) {
        this.orderId = orderId;
        this.trackingNumber = trackingNumber;
        this.skus = List.copyOf(skus);
        this.totalPaid = totalPaid;
    }

    public String orderId()        { return orderId; }
    public String trackingNumber() { return trackingNumber; }
    public List<String> skus()     { return skus; }
    public double totalPaid()      { return totalPaid; }

    @Override
    public String toString() {
        return "Order{id='" + orderId + "', tracking='" + trackingNumber
                + "', skus=" + skus + ", total=" + totalPaid + "}";
    }
}