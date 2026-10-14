package kz.ramazan.orders.app;

import kz.ramazan.orders.domain.Cart;
import kz.ramazan.orders.domain.CartItem;
import kz.ramazan.orders.domain.Customer;
import kz.ramazan.orders.domain.Order;
import kz.ramazan.orders.errors.OrderPlacementException;
import kz.ramazan.orders.facades.OrderFacade;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        OrderFacade facade = new OrderFacade();

        Customer ramazan = new Customer(
                "CUST-001", "ramazan@example.com", "Almaty, Kazakhstan");

        Cart smallCart = new Cart(List.of(
                new CartItem("BOOK-001", 1),
                new CartItem("MUG-777", 2)
        ));
        Order order = facade.placeOrder(smallCart, ramazan);
        System.out.println("Placed: " + order);

        Cart expensiveCart = new Cart(List.of(
                new CartItem("GADGET-X", 2)
        ));
        try {
            facade.placeOrder(expensiveCart, ramazan);
        } catch (OrderPlacementException ex) {
            System.out.println("Expected failure: " + ex.getMessage());
        }

        facade.cancelOrder(order.orderId());
    }
}