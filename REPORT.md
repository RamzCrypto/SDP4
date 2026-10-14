# Assignment #4 — Facade Pattern Report

**Course:** Software Design Patterns 
**Student:** Bazaraly Ramazan
**Group:** SE-2526
**Repository:** https://github.com/RamzCrypto/SDP4

---

## 1. Introduction — chosen subsystem and why it needs a Facade

For this assignment I chose **Option B — Online Order Processing**. Placing an
online order is a textbook example of a *complex subsystem*: one business
action requires four independent services to cooperate in a strict order —
reserve stock, charge the customer, schedule a shipment, and notify them.
Each service owns its own API, its own failure mode, and its own invariants
(for example, a shipment cannot be scheduled before a payment has been
captured).

Without a Facade, every client that wants to place an order must know at
least five things:

1. That `InventoryService.reserve` must be called before `PaymentProcessor.charge`.
2. That `ShippingService.scheduleShipment` requires a captured transaction id.
3. That if `PaymentProcessor.charge` throws, the stock reserved in step 1
   must be released.
4. That if `ShippingService.scheduleShipment` throws, the captured payment
   must be refunded *and* the stock released.
5. That `NotificationService` is best-effort and must not break the order.

That is four subsystem classes and five ordering/compensation rules leaking
into every caller. This is exactly the problem the Facade pattern solves:
a single class, `OrderFacade`, exposes one method —
`placeOrder(cart, customer)` — and internally owns the orchestration and
rollback logic. Clients stay simple and correct by construction; the
subsystem stays reusable for advanced clients who bypass the Facade.

**Public API of the Facade (the entire surface a client sees):**

```java
Order order = facade.placeOrder(cart, customer);
facade.cancelOrder(order.orderId());
```

---

## 2. UML class diagram
![UML class diagram](uml.png)

The diagram below shows the Facade (`OrderFacade`), the optional second
Facade (`OrderAdminFacade`), the four subsystem classes, the domain value
objects, and the client (`Main`). Note that **no arrow points from a
subsystem class back to the Facade** — this is the defining property of the
pattern.



**How to render:** paste the block above into https://www.plantuml.com/plantuml
and export as PNG, then embed the image in this section.

**Reading the diagram:**
- `Main` depends on `OrderFacade` and on the value objects (`Cart`,
  `Customer`, `Order`) — nothing else.
- `OrderFacade` depends on the four subsystem classes (one-directional
  arrows).
- No arrow points from a subsystem back to `OrderFacade`.
- `OrderAdminFacade` is a second, orthogonal entry point that exposes a
  different slice of subsystem functionality without polluting the main
  Facade.

---

## 3. Clean Code section

### Principle 1 — Meaningful names that make the simplified API self-explanatory

The Facade's public surface uses domain vocabulary, not implementation
vocabulary.

**Before (leaky, technical):**
```java
public String process(List<Item> i, String custId, String addr) { ... }
```

**After (self-documenting):**
```java
public Order placeOrder(Cart cart, Customer customer) { ... }
public void  cancelOrder(String orderId) { ... }
```

*Justification:* a reader immediately understands *what* the method does and
*who* the inputs are, without reading the body. `placeOrder` / `cancelOrder`
mirror the business vocabulary and remove the mental translation between
code and requirements.

---

### Principle 2 — Single Responsibility: the Facade coordinates, it does not reimplement

`OrderFacade` never calculates prices, never talks to a database, never
formats an email. It only decides *the order of operations* and
*compensation*.

```java
public Order placeOrder(Cart cart, Customer customer) {
    List<CartItem> reserved = new ArrayList<>();
    String txnId = null, tracking = null;
    try {
        double total = 0.0;
        for (CartItem item : cart.items()) {
            total += inventory.reserve(item.sku(), item.quantity());
            reserved.add(item);
        }
        txnId    = payments.charge(customer, total);
        tracking = shipping.scheduleShipment(customer, cart.skus(), txnId);
        Order o  = new Order(newId(), tracking, cart.skus(), total);
        notifications.sendOrderConfirmation(customer, o);
        activeOrders.put(o.orderId(), o);
        return o;
    } catch (RuntimeException ex) {
        rollback(reserved, txnId, tracking);
        throw new OrderPlacementException(
            "Order could not be placed: " + ex.getMessage(), ex);
    }
}
```

*Justification:* if pricing logic crept in here, changing a discount rule
would force a change to the Facade. Keeping the Facade thin means the only
reason it changes is a change in the *orchestration order* — one
responsibility.

---

### Principle 3 — No leaking of subsystem details through the public interface

The client never receives an `InventoryService`, `PaymentProcessor`,
`ShippingService` or `NotificationService` instance.

**Before (leaky):**
```java
public InventoryService getInventory() { return inventory; }   // BAD
```

**After:**
```java
// No getters for subsystem collaborators — only domain results are returned.
public Order placeOrder(Cart cart, Customer customer) { ... }
```

Failure signalling is also hidden behind a facade-level exception:

```java
public class OrderPlacementException extends RuntimeException { ... }
```

Instead of letting `PaymentException` or `InventoryException` escape, the
Facade wraps them. A future change of payment provider no longer ripples
into client `catch` blocks.

*Justification:* the client is coupled to *one* abstraction (`OrderFacade`)
and *one* exception type. Subsystems can be replaced, upgraded, or mocked
without touching the client.

---

### Principle 4 — Small, focused subsystem classes with clear responsibilities

Each subsystem class does exactly one thing and does not know about the
others.

```java
public class InventoryService {                // only knows about stock
    public double reserve(String sku, int qty) { ... }
    public void   release(String sku, int qty) { ... }
    public int    available(String sku)        { ... }
}

public class PaymentProcessor {                // only knows about money
    public String charge(Customer c, double amount) { ... }
    public void   refund(String txnId)              { ... }
}
```

*Justification:* cohesion is high, coupling between subsystems is zero.
`InventoryService` can be unit-tested without a payment gateway;
`PaymentProcessor` can be tested without a warehouse. The Facade composes
them.

---

### Principle 5 — Proper error handling for subsystem coordination failures

The Facade distinguishes the *happy path* from the *compensation path* and
guarantees atomicity from the client's point of view.

```java
private void rollback(List<CartItem> reserved,
                      String transactionId,
                      String trackingNumber) {
    if (trackingNumber != null) shipping.cancelShipment(trackingNumber);
    if (transactionId != null)  payments.refund(transactionId);
    for (CartItem item : reserved) {
        inventory.release(item.sku(), item.quantity());
    }
}
```

*Justification:* without this, a declined payment would leave stock
reserved forever — a classic “dangling resource” bug. Putting compensation
into one private method (a) prevents duplication, (b) makes failure
semantics auditable in one place, and (c) mirrors the orchestration order
in reverse, which is easy to verify.


The second attempt (`GADGET-X × 2 = 51 980 KZT`, above the 30 000 limit)
was rejected by `PaymentProcessor`, and `rollback` returned the reserved
stock to `InventoryService` — `GADGET-X` is still 2 units in stock
afterwards.

---

## 4. Conclusion — what the Facade hides, and what remains directly accessible

**What `OrderFacade` hides from clients:**
- The existence of `InventoryService`, `PaymentProcessor`,
  `ShippingService` and `NotificationService`.
- The strict ordering constraint `reserve -> charge -> ship -> notify`.
- The compensation logic (cancel shipment, refund payment, release stock).
- Subsystem-specific exception types — clients only see
  `OrderPlacementException`.

**What remains directly accessible (and why that is correct):**
- The four subsystem classes are `public` and have **no back-reference**
  to the Facade. An advanced client — for example, a warehouse dashboard —
  can inject an `InventoryService` directly and call `reserve` / `release`
  without going through the order flow. This is *intentional*: the Facade
  is a **convenience**, not a **gatekeeper**.
- `OrderAdminFacade` demonstrates a second, orthogonal slice of subsystem
  functionality exposed through its own Facade, so the main `OrderFacade`
  stays small and single-purpose.

**Pattern property satisfied:** subsystem classes remain unaware of the
Facade's existence, and the Facade is the only class that knows the
orchestration order. Adding a new client (mobile app, kiosk, admin tool)
requires no change to any subsystem class — only a new Facade consumer,
which is precisely the extensibility the pattern is meant to deliver.

---

## 5. Link to the GitHub repository

**https://github.com/RamzCrypto/SDP4**

The repository contains:
- Complete Java 17 source under `src/kz/ramazan/orders/`
- `README.md` with build & run instructions
- `REPORT.md` (this document)
- Incremental commit history showing the development flow

---

## Appendix A — How to build and run

```bash
git clone https://github.com/RamzCrypto/SDP4.git
cd SDP4
javac -d out $(find src -name "*.java")
java -cp out kz.ramazan.orders.app.Main
```

Requires **JDK 17 or newer**.