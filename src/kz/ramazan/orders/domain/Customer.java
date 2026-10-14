package kz.ramazan.orders.domain;

public final class Customer {

    private final String id;
    private final String email;
    private final String shippingAddress;

    public Customer(String id, String email, String shippingAddress) {
        if (id == null || id.isBlank())          throw new IllegalArgumentException("id required");
        if (email == null || email.isBlank())    throw new IllegalArgumentException("email required");
        if (shippingAddress == null || shippingAddress.isBlank())
            throw new IllegalArgumentException("shippingAddress required");
        this.id = id;
        this.email = email;
        this.shippingAddress = shippingAddress;
    }

    public String id()              { return id; }
    public String email()           { return email; }
    public String shippingAddress() { return shippingAddress; }
}