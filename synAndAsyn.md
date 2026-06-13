Sync / Async analysis (auto-generated)

Overview
--------
This file lists which HTTP endpoints and service components behave synchronously (direct HTTP call / immediate return) and which operate asynchronously (message producers/consumers or background tasks).

Payment service
---------------
- Endpoint: POST /orders — synchronous: handled by `PaymentController#createOrder` which calls `PaymentGatewayClient.createOrder` (synchronous RestTemplate HTTP call). See [payment/src/main/java/com/ecom/payment/controller/PaymentController.java](payment/src/main/java/com/ecom/payment/controller/PaymentController.java) and [payment/src/main/java/com/ecom/payment/client/PaymentGatewayClient.java](payment/src/main/java/com/ecom/payment/client/PaymentGatewayClient.java).
- Endpoint: GET /orders/{orderId}/status — synchronous endpoint, but when status is "PAID" it publishes an asynchronous payment event via `PaymentStatusPublisher.publishPaid`. See [payment/src/main/java/com/ecom/payment/controller/PaymentController.java](payment/src/main/java/com/ecom/payment/controller/PaymentController.java) and [payment/src/main/java/com/ecom/payment/messaging/PaymentStatusPublisher.java](payment/src/main/java/com/ecom/payment/messaging/PaymentStatusPublisher.java).
- Async component: `PaymentStatusPublisher` — uses `RabbitTemplate.convertAndSend(...)` to publish messages to RabbitMQ (async producer).

Order service
-------------
- Endpoints: multiple synchronous REST endpoints (e.g. seller endpoints under `/api/sellers/orders`) implemented in controllers such as [order/src/main/java/com/ecommerce/order/controller/SellerOrderController.java](order/src/main/java/com/ecommerce/order/controller/SellerOrderController.java). These return HTTP responses directly and call service methods.
- Mixed (sync -> async): `OrderService#createOrder` is invoked by the order creation endpoint, saves the order then publishes the order message with `rabbitTemplate.convertAndSend(...)` — synchronous API that triggers an asynchronous message. See [order/src/main/java/com/ecommerce/order/services/OrderService.java](order/src/main/java/com/ecommerce/order/services/OrderService.java).
- Async consumer: `PaymentStatusListener` (annotated `@RabbitListener`) listens on the `payment.status.queue` and invokes `OrderService.markAsPaidFromPaymentMessage(...)` to update order state when it receives a PAID event. See [order/src/main/java/com/ecommerce/order/listeners/PaymentStatusListener.java](order/src/main/java/com/ecommerce/order/listeners/PaymentStatusListener.java).

Notification service
--------------------
- Async consumer: `PaymentEventListener` is a `@RabbitListener` on `payment.events.queue` and handles incoming payment events from other services. See [notification/src/main/java/com/ecommerce/notification/listener/PaymentEventListener.java](notification/src/main/java/com/ecommerce/notification/listener/PaymentEventListener.java).
- Background async: `EmailService` methods (e.g. `sendPaymentSuccessEmail`, `sendPaymentFailedEmail`, `sendPaymentPendingEmail`) are annotated with `@Async` — emails are sent in background threads and do not block the caller. See [notification/src/main/java/com/ecommerce/notification/service/EmailService.java](notification/src/main/java/com/ecommerce/notification/service/EmailService.java).

User service
------------
- Endpoints: user-related REST endpoints (e.g. `UserController`) are synchronous HTTP handlers.
- Async notifications: `user` `EmailService` contains `@Async` methods for non-critical notifications (e.g. `sendPasswordResetConfirmation`, `sendAccountDeletionConfirmation`) so they run in background and don't block the main request flow. See [user/src/main/java/com/ecommerce/user/services/EmailService.java](user/src/main/java/com/ecommerce/user/services/EmailService.java).

Product, Gateway, and other services
-----------------------------------
- Product and gateway controllers (e.g. `ProductController`, `FallbackController`) are primarily synchronous REST endpoints (return `ResponseEntity` and call service methods directly). See [product/src/main/java/com/ecommerce/product/controllers/ProductController.java](product/src/main/java/com/ecommerce/product/controllers/ProductController.java) and [gateway/src/main/java/com/ecommerce/gateway/FallbackController.java](gateway/src/main/java/com/ecommerce/gateway/FallbackController.java).

Summary — how to spot sync vs async in this repo
------------------------------------------------
- Synchronous: HTTP controller methods that return `ResponseEntity` and call clients or service methods directly (common across `payment`, `order`, `user`, `product`, `gateway`).
- Asynchronous (messages): classes that use `RabbitTemplate.convertAndSend(...)` or are annotated with `@RabbitListener` — these are RabbitMQ producers/consumers and form async flows between services.
- Asynchronous (background tasks): methods annotated with `@Async` (or using a task executor) — used for non-blocking notifications like sending emails.

If you want, I can also:
- Add exact method-line links for each mentioned method (line numbers), or
- Draw the end-to-end flow for a chosen scenario (e.g. Create order → Payment → Notification) showing which parts are sync vs async.

Generated: automated scan of repository (controllers, Rabbit listeners, and `@Async` methods).
