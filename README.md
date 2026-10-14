# SDP4 — Facade Pattern (Online Order Processing)

**Course:** Software Design Patterns
**Student:** Bazaraly Ramazan
**Assignment:** #4 — Facade Pattern

## What it does

`OrderFacade` hides four collaborating subsystems behind two methods:

```java
Order order = facade.placeOrder(cart, customer);
facade.cancelOrder(order.orderId());
```

Internally it coordinates `InventoryService`, `PaymentProcessor`,
`ShippingService`, and `NotificationService` in a fixed order, and
rolls back already-completed steps if a later step fails.

## Run it

```bash
javac -d out $(find src -name "*.java")
java -cp out kz.ramazan.orders.app.Main
```

Requires **JDK 17+**.

## Design highlights

- Subsystem classes have no reference to `OrderFacade`.
- The Facade's public API exposes only `placeOrder` / `cancelOrder`.
- `OrderAdminFacade` is an optional second Facade for read-only reporting.

## Report

See `REPORT.md`.