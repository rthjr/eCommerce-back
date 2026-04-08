# 🐰 RabbitMQ Message Flow Architecture - E-Commerce Application

## 📋 Table of Contents
1. [Overview](#overview)
2. [RabbitMQ Infrastructure](#rabbitmq-infrastructure)
3. [Message Producers](#message-producers)
4. [Message Consumers](#message-consumers)
5. [Complete Message Flow Diagrams](#complete-message-flow-diagrams)
6. [Event Models & DTOs](#event-models--dtos)
7. [Configuration Details](#configuration-details)

---

## 🎯 Overview

This e-commerce microservices application uses **RabbitMQ** as an event-driven message broker to enable asynchronous communication between services. The architecture follows the **Topic Exchange** pattern with durable queues for reliable message delivery.

### Key Components:
- **2 Topic Exchanges**: `order.exchange`, `payment.exchange`
- **3 Durable Queues**: Order queue, Order status queue, Payment events queue
- **2 Message Producers**: Order Service (Java), Payment Gateway (Python)
- **1 Message Consumer**: Notification Service (Java)
- **Connection**: CloudAMQP (Hosted RabbitMQ)

---

## 🏗️ RabbitMQ Infrastructure

### Exchanges

| Exchange Name | Type | Durable | Purpose |
|--------------|------|---------|---------|
| `order.exchange` | Topic | ✅ | Routes order creation and status change events |
| `payment.exchange` | Topic | ✅ | Routes payment processing events (success, failure, pending, refund) |

### Queues

| Queue Name | Durable | Bound To Exchange | Routing Key Pattern | Consumer |
|-----------|---------|-------------------|-------------------|----------|
| `order.queue` | ✅ | `order.exchange` | `order.tracking` | Notification Service |
| `order.status.queue` | ✅ | `order.exchange` | `order.status.*` | Notification Service |
| `payment.events.queue` | ✅ | `payment.exchange` | `payment.*` | Notification Service |

### Connection Configuration

```yaml
spring:
  rabbitmq:
    addresses: amqps://exnddjpl:t2JWuxv4dfjNPSCw-L9V9519lSM2DMSG@cougar.rmq.cloudamqp.com/exnddjpl
    connection-timeout: 30000ms
```

---

## 📤 Message Producers

### 1. Order Service (Java)

**File**: `order/src/main/java/com/ecommerce/order/services/OrderService.java`

**Technology**: Spring AMQP (`RabbitTemplate`)

**Event Published**: Order Created

| Property | Value |
|---------|-------|
| **Exchange** | `order-exchange` |
| **Routing Key** | `order.tracker` |
| **Message Type** | `Order` entity (Full object) |
| **Trigger** | When customer places a new order |
| **Code Location** | Line 110-111 |

**Payload Includes**:
- `orderId`, `userId`, `status` (CONFIRMED)
- `itemsPrice`, `shippingPrice`, `taxPrice`, `totalAmount`
- List of `OrderItem` entities
- Shipping address, Payment method

**Code Snippet**:
```java
rabbitTemplate.convertAndSend(exchangeName, routingKey, order);
```

---

### 2. Payment Gateway Service (Python)

**File**: `payment-gateway/rabbitmq_publisher.py`

**Technology**: Pika (Python RabbitMQ client)

**Events Published**:

#### Payment Events

| Event Type | Exchange | Routing Key | Trigger |
|-----------|----------|-------------|---------|
| `PAYMENT_SUCCESS` | `payment.exchange` | `payment.payment_success` | Payment processed successfully |
| `PAYMENT_FAILED` | `payment.exchange` | `payment.payment_failed` | Payment declined/failed |
| `PAYMENT_PENDING` | `payment.exchange` | `payment.payment_pending` | Payment awaiting confirmation |
| `REFUND_PROCESSED` | `payment.exchange` | `payment.refund_processed` | Refund completed |

**Payload Structure**:
```json
{
  "orderId": "string",
  "userId": "string",
  "userRole": "ROLE_CUSTOMER",
  "eventType": "PAYMENT_SUCCESS",
  "amount": 99.99,
  "currency": "USD",
  "paymentMethod": "KHQR",
  "transactionId": "txn_abc123",
  "timestamp": "2026-04-07T13:43:26Z",
  "message": "Payment successful",
  "failureReason": null
}
```

#### Order Status Events

| Event | Exchange | Routing Key | Trigger |
|-------|----------|-------------|---------|
| Order Status Changed | `order.exchange` | `order.status.{newStatus}` | Status update (SHIPPED, DELIVERED, CANCELLED) |

**Examples**:
- `order.status.shipped` → Order marked as shipped
- `order.status.delivered` → Order delivered to customer
- `order.status.cancelled` → Order cancelled

**Payload Structure**:
```json
{
  "orderId": 12345,
  "userId": "user_abc",
  "userRole": "ROLE_CUSTOMER",
  "previousStatus": "CONFIRMED",
  "newStatus": "SHIPPED",
  "changedAt": "2026-04-07T13:43:26Z",
  "message": "Your order has been shipped"
}
```

---

## 📥 Message Consumers

### Notification Service (Java)

**Technology**: Spring Cloud Stream + RabbitMQ Listener

#### Consumer 1: PaymentEventListener

**File**: `notification/src/main/java/com/ecommerce/notification/listener/PaymentEventListener.java`

```java
@RabbitListener(queues = "payment.events.queue")
public void handlePaymentEvent(PaymentEvent event)
```

**Actions by Event Type**:

| Event Type | Action |
|-----------|--------|
| `PAYMENT_SUCCESS` | Fetch user details → Send payment success email |
| `PAYMENT_FAILED` | Fetch user details → Send payment failed email |
| `PAYMENT_PENDING` | Fetch user details → Send payment pending email |
| `REFUND_PROCESSED` | Log refund notification |

---

#### Consumer 2: OrderEventConsumer (Spring Cloud Stream)

**File**: `notification/src/main/java/com/ecommerce/notification/OrderEventConsumer.java`

**Function 1: orderCreated()**
- **Binding**: `orderCreated-in-0` → `order.queue`
- **Input**: `OrderCreatedEvent`
- **Action**: Log order creation details

**Function 2: orderStatusChanged()**
- **Binding**: `orderStatusChanged-in-0` → `order.status.queue`
- **Input**: `OrderStatusChangedEvent`
- **Actions**:
  - `DELIVERED` → Send delivery notification email
  - `CONFIRMED` → Log confirmation
  - `SHIPPED` → Log shipment
  - `CANCELLED` → Log cancellation

**Function 3: paymentEvent()**
- **Binding**: `paymentEvent-in-0` → `payment.events.queue`
- **Input**: `PaymentEvent`
- **Action**: Log payment events (duplicate consumption with PaymentEventListener)

---

## 🔄 Complete Message Flow Diagrams

### 1️⃣ Order Creation Flow

```mermaid
flowchart TD
    Start([Customer Places Order]) --> A[Order Service: Validate Cart]
    A --> B{Stock Available?}
    B -->|No| Error[Return Error: Out of Stock]
    B -->|Yes| C[Reduce Product Stock]
    C --> D[Save Order to Database]
    D --> E[Order Status: CONFIRMED]
    E --> F[RabbitTemplate.convertAndSend]
    F --> G{order.exchange}
    G -->|Routing Key:<br/>order.tracker| H[(order.queue)]
    H --> I[Notification Service:<br/>OrderEventConsumer.orderCreated]
    I --> J[Log: Order created for user X]
    J --> K[Clear Customer Cart]
    K --> End([Order Creation Complete])
    
    style Start fill:#e1f5e1
    style End fill:#e1f5e1
    style G fill:#fff3cd
    style H fill:#d1ecf1
    style I fill:#f8d7da
```

---

### 2️⃣ Payment Success Flow

```mermaid
flowchart TD
    Start([Payment Gateway Processes Payment]) --> A{Payment Result}
    A -->|Success| B[Payment Gateway:<br/>publish_payment_event]
    B --> C[Create PaymentEvent Object]
    C --> D[eventType = PAYMENT_SUCCESS<br/>transactionId = txn_abc123]
    D --> E{payment.exchange}
    E -->|Routing Key:<br/>payment.payment_success| F[(payment.events.queue)]
    
    F --> G[Notification Service:<br/>PaymentEventListener]
    F --> H[Notification Service:<br/>OrderEventConsumer.paymentEvent]
    
    G --> I[Switch on EventType:<br/>PAYMENT_SUCCESS]
    I --> J[NotificationService.<br/>sendPaymentSuccessNotification]
    J --> K[Fetch User Details<br/>from User Service API]
    K --> L[EmailService.<br/>sendPaymentSuccessEmail]
    L --> M[Send Email to Customer]
    
    H --> N[Log: Payment successful<br/>Order 12345 - Amount 99.99 USD]
    
    M --> End([Payment Notification Sent])
    N --> End
    
    style Start fill:#e1f5e1
    style End fill:#e1f5e1
    style E fill:#fff3cd
    style F fill:#d1ecf1
    style G fill:#f8d7da
    style H fill:#f8d7da
```

---

### 3️⃣ Order Status Change Flow

```mermaid
flowchart TD
    Start([Order Status Update Triggered]) --> A[Payment Gateway:<br/>publish_order_status_event]
    A --> B[Create OrderStatusChangedEvent]
    B --> C{Select New Status}
    
    C -->|SHIPPED| D[previousStatus = CONFIRMED<br/>newStatus = SHIPPED<br/>message = Order has been shipped]
    C -->|DELIVERED| E[previousStatus = SHIPPED<br/>newStatus = DELIVERED<br/>message = Order delivered]
    C -->|CANCELLED| F[previousStatus = CONFIRMED<br/>newStatus = CANCELLED<br/>message = Order cancelled]
    
    D --> G{order.exchange}
    E --> G
    F --> G
    
    G -->|Routing Key:<br/>order.status.shipped| H[(order.status.queue)]
    G -->|Routing Key:<br/>order.status.delivered| H
    G -->|Routing Key:<br/>order.status.cancelled| H
    
    H --> I[Notification Service:<br/>OrderEventConsumer.orderStatusChanged]
    I --> J{Switch on newStatus}
    
    J -->|SHIPPED| K[Log: Order shipped<br/>OrderId 12345]
    J -->|DELIVERED| L[Fetch User Details<br/>Send Delivery Notification Email]
    J -->|CONFIRMED| M[Log: Order confirmed]
    J -->|CANCELLED| N[Log: Order cancelled]
    
    K --> End([Status Update Processed])
    L --> End
    M --> End
    N --> End
    
    style Start fill:#e1f5e1
    style End fill:#e1f5e1
    style G fill:#fff3cd
    style H fill:#d1ecf1
    style I fill:#f8d7da
```

---

### 4️⃣ Payment Failure Flow

```mermaid
flowchart TD
    Start([Payment Processing Fails]) --> A[Payment Gateway:<br/>publish_payment_event]
    A --> B[Create PaymentEvent Object]
    B --> C[eventType = PAYMENT_FAILED<br/>failureReason = Card Declined]
    C --> D{payment.exchange}
    D -->|Routing Key:<br/>payment.payment_failed| E[(payment.events.queue)]
    
    E --> F[Notification Service:<br/>PaymentEventListener]
    F --> G[Switch on EventType:<br/>PAYMENT_FAILED]
    G --> H[NotificationService.<br/>sendPaymentFailedNotification]
    H --> I[Fetch User Details<br/>from User Service API]
    I --> J[EmailService.<br/>sendPaymentFailedEmail]
    J --> K[Email Content:<br/>- Order ID<br/>- Amount<br/>- Failure Reason<br/>- Retry Instructions]
    K --> L[Send Email to Customer]
    L --> End([Failure Notification Sent])
    
    style Start fill:#ffe6e6
    style End fill:#ffe6e6
    style D fill:#fff3cd
    style E fill:#d1ecf1
    style F fill:#f8d7da
```

---

### 5️⃣ Complete System Architecture

```mermaid
flowchart TB
    subgraph Producers
        OS[Order Service<br/>Java + Spring Boot]
        PG[Payment Gateway<br/>Python + Pika]
    end
    
    subgraph RabbitMQ_Broker[RabbitMQ Broker - CloudAMQP]
        OE{order.exchange<br/>Topic}
        PE{payment.exchange<br/>Topic}
        
        OQ[(order.queue)]
        OSQ[(order.status.queue)]
        PEQ[(payment.events.queue)]
        
        OE -->|order.tracking| OQ
        OE -->|order.status.*| OSQ
        PE -->|payment.*| PEQ
    end
    
    subgraph Consumer
        NS[Notification Service<br/>Java + Spring Cloud Stream]
    end
    
    subgraph External_Services[External Services]
        US[User Service<br/>REST API]
        ES[Email Service<br/>SMTP]
    end
    
    OS -->|New Order Created<br/>Full Order Object| OE
    PG -->|Payment Events<br/>4 Types| PE
    PG -->|Status Changes<br/>SHIPPED/DELIVERED/CANCELLED| OE
    
    OQ --> NS
    OSQ --> NS
    PEQ --> NS
    
    NS -->|Fetch User Details<br/>GET /users/id| US
    NS -->|Send Email<br/>Payment/Order Updates| ES
    
    style OS fill:#d4edda
    style PG fill:#d4edda
    style NS fill:#f8d7da
    style RabbitMQ_Broker fill:#fff3cd
    style OE fill:#ffeaa7
    style PE fill:#ffeaa7
    style OQ fill:#74b9ff
    style OSQ fill:#74b9ff
    style PEQ fill:#74b9ff
```

---

### 6️⃣ Message Flow Timeline

```mermaid
sequenceDiagram
    participant C as Customer
    participant OS as Order Service
    participant OE as order.exchange
    participant PE as payment.exchange
    participant OQ as order.queue
    participant PQ as payment.events.queue
    participant NS as Notification Service
    participant PG as Payment Gateway
    participant ES as Email Service
    
    Note over C,ES: Phase 1: Order Creation
    C->>OS: POST /api/orders/create
    OS->>OS: Validate cart & stock
    OS->>OS: Create order (DB)
    OS->>OE: Send Order object<br/>routing: order.tracker
    OE->>OQ: Route message
    OQ->>NS: Consume: OrderCreatedEvent
    NS->>NS: Log order creation
    OS-->>C: 201 Created (Order ID)
    
    Note over C,ES: Phase 2: Payment Processing
    C->>PG: Initiate Payment
    PG->>PG: Process payment
    alt Payment Success
        PG->>PE: PAYMENT_SUCCESS event<br/>routing: payment.payment_success
        PE->>PQ: Route message
        PQ->>NS: Consume: PaymentEvent
        NS->>NS: Fetch user details
        NS->>ES: Send success email
        ES-->>C: Email: Payment Successful
    else Payment Failed
        PG->>PE: PAYMENT_FAILED event<br/>routing: payment.payment_failed
        PE->>PQ: Route message
        PQ->>NS: Consume: PaymentEvent
        NS->>NS: Fetch user details
        NS->>ES: Send failure email
        ES-->>C: Email: Payment Failed
    end
    
    Note over C,ES: Phase 3: Order Fulfillment
    PG->>OE: Order Status: SHIPPED<br/>routing: order.status.shipped
    OE->>NS: Consume: OrderStatusChangedEvent
    NS->>NS: Log: Order shipped
    
    PG->>OE: Order Status: DELIVERED<br/>routing: order.status.delivered
    OE->>NS: Consume: OrderStatusChangedEvent
    NS->>NS: Fetch user details
    NS->>ES: Send delivery notification
    ES-->>C: Email: Order Delivered
```

---

## 📦 Event Models & DTOs

### PaymentEvent

**File**: `notification/payload/PaymentEvent.java`

```java
public class PaymentEvent {
    private String orderId;
    private String userId;
    private String userRole;
    private PaymentEventType eventType;
    private BigDecimal amount;
    private String currency = "USD";
    private String paymentMethod;
    private String transactionId;
    private LocalDateTime timestamp;
    private String message;
    private String failureReason;
}
```

### PaymentEventType Enum

```java
public enum PaymentEventType {
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    PAYMENT_PENDING,
    REFUND_PROCESSED
}
```

### OrderCreatedEvent

**File**: `notification/payload/OrderCreatedEvent.java`

```java
public class OrderCreatedEvent {
    private Long orderId;
    private String userId;
    private OrderStatus status;
    private List<OrderItemDTO> items;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}
```

### OrderStatusChangedEvent

**File**: `notification/payload/OrderStatusChangedEvent.java`

```java
public class OrderStatusChangedEvent {
    private Long orderId;
    private String userId;
    private String userRole;
    private OrderStatus previousStatus;
    private OrderStatus newStatus;
    private LocalDateTime changedAt;
    private String message;
}
```

### OrderStatus Enum

```java
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
```

### OrderItemDTO

```java
public class OrderItemDTO {
    private Long id;
    private String productId;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subTotal;
}
```

---

## ⚙️ Configuration Details

### Order Service Configuration

**File**: `configserver/src/main/resources/config/order-service.yml`

```yaml
spring:
  rabbitmq:
    addresses: amqps://exnddjpl:t2JWuxv4dfjNPSCw-L9V9519lSM2DMSG@cougar.rmq.cloudamqp.com/exnddjpl
    connection-timeout: 30000ms

rabbitmq:
  exchange:
    name: order-exchange
  queue:
    name: order.queue
  routing:
    key: order.tracker
```

**Configuration Class**: `order/config/RabbitMQConfiguration.java`

```java
@Bean
public Queue queue() {
    return new Queue(queueName, true); // Durable queue
}

@Bean
public TopicExchange exchange() {
    return new TopicExchange(exchangeName);
}

@Bean
public Binding binding(Queue queue, TopicExchange exchange) {
    return BindingBuilder.bind(queue).to(exchange).with(routingKey);
}

@Bean
public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
}
```

---

### Notification Service Configuration

**File**: `notification/src/main/resources/application.yml`

```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}

  cloud:
    stream:
      function:
        definition: orderCreated;orderStatusChanged;paymentEvent
      bindings:
        orderCreated-in-0:
          destination: order.queue
          group: notification-group
        orderStatusChanged-in-0:
          destination: order.status.queue
          group: notification-group
        paymentEvent-in-0:
          destination: payment.events.queue
          group: notification-group

rabbitmq:
  queue:
    name: order.queue
  exchange:
    name: order.exchange
  routing:
    key: order.tracking
```

**Configuration Class**: `notification/config/RabbitMQConfig.java`

```java
// Order Exchange & Queue
@Bean
public TopicExchange orderExchange() {
    return new TopicExchange("order.exchange", true, false);
}

@Bean
public Queue orderQueue() {
    return new Queue("order.queue", true);
}

@Bean
public Queue orderStatusQueue() {
    return new Queue("order.status.queue", true);
}

@Bean
public Binding orderBinding() {
    return BindingBuilder.bind(orderQueue())
        .to(orderExchange())
        .with("order.tracking");
}

@Bean
public Binding orderStatusBinding() {
    return BindingBuilder.bind(orderStatusQueue())
        .to(orderExchange())
        .with("order.status.*");
}

// Payment Exchange & Queue
@Bean
public TopicExchange paymentExchange() {
    return new TopicExchange("payment.exchange", true, false);
}

@Bean
public Queue paymentEventsQueue() {
    return new Queue("payment.events.queue", true);
}

@Bean
public Binding paymentBinding() {
    return BindingBuilder.bind(paymentEventsQueue())
        .to(paymentExchange())
        .with("payment.*");
}

@Bean
public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
}
```

---

### Payment Gateway Configuration (Python)

**File**: `payment-gateway/rabbitmq_publisher.py`

```python
import pika
import json

class RabbitMQPublisher:
    def __init__(self):
        self.url = "amqps://exnddjpl:t2JWuxv4dfjNPSCw-L9V9519lSM2DMSG@cougar.rmq.cloudamqp.com/exnddjpl"
        self.params = pika.URLParameters(self.url)
        
    def publish_payment_event(self, orderId, userId, userRole, eventType, 
                             amount, currency, paymentMethod=None, 
                             transactionId=None, failureReason=None):
        connection = pika.BlockingConnection(self.params)
        channel = connection.channel()
        
        channel.exchange_declare(exchange='payment.exchange',
                                exchange_type='topic',
                                durable=True)
        
        routing_key = f"payment.{eventType.lower()}"
        
        message = {
            "orderId": orderId,
            "userId": userId,
            "userRole": userRole,
            "eventType": eventType,
            "amount": amount,
            "currency": currency,
            "paymentMethod": paymentMethod,
            "transactionId": transactionId,
            "timestamp": datetime.utcnow().isoformat(),
            "message": f"Payment {eventType.lower()}",
            "failureReason": failureReason
        }
        
        channel.basic_publish(
            exchange='payment.exchange',
            routing_key=routing_key,
            body=json.dumps(message),
            properties=pika.BasicProperties(
                delivery_mode=2,  # Persistent message
                content_type='application/json'
            )
        )
        
        connection.close()
```

---

## 🔑 Key Technical Details

### Message Durability
- **Queues**: All queues are durable (`durable=true`)
- **Messages**: Persistent delivery mode ensures messages survive broker restart
- **Exchanges**: Durable exchanges

### Message Conversion
- **Java Services**: Use `Jackson2JsonMessageConverter` for JSON serialization
- **Python Service**: Uses `json.dumps()` with `content_type='application/json'`

### Consumer Groups
- **notification-group**: Ensures only one instance of Notification Service processes each message (load balancing)

### Routing Patterns
- **Direct Routing**: `order.tracking` → Routes to specific queue
- **Wildcard Routing**: `order.status.*` → Routes all status changes
- **Wildcard Routing**: `payment.*` → Routes all payment events

### Dual Consumption Strategy
Notification Service consumes payment events using **both**:
1. **@RabbitListener** (traditional approach)
2. **Spring Cloud Stream** (functional approach)

This provides redundancy but may cause duplicate processing.

---

## 📊 Services Summary

| Service | Role | Technology | Exchanges Used | Queues Used |
|---------|------|------------|----------------|-------------|
| **Order Service** | Producer | Java + Spring AMQP | `order.exchange` | - |
| **Payment Gateway** | Producer | Python + Pika | `payment.exchange`, `order.exchange` | - |
| **Notification Service** | Consumer | Java + Spring Cloud Stream | - | `order.queue`, `order.status.queue`, `payment.events.queue` |
| **User Service** | External | Java + REST | - | - |
| **Product Service** | External | Java + REST | - | - |

---

## 🚀 Message Flow Execution Order

1. **Customer places order** → Order Service publishes to `order.queue`
2. **Notification Service logs** order creation
3. **Customer initiates payment** → Payment Gateway processes
4. **Payment Gateway publishes** payment event (SUCCESS/FAILED/PENDING)
5. **Notification Service receives** payment event → Sends email to customer
6. **Payment Gateway publishes** order status change (SHIPPED/DELIVERED)
7. **Notification Service receives** status change → Sends delivery notification

---

## 📈 Benefits of This Architecture

✅ **Asynchronous Processing**: Order creation doesn't wait for notifications  
✅ **Loose Coupling**: Services don't directly depend on each other  
✅ **Scalability**: Multiple notification service instances can share load  
✅ **Reliability**: Durable queues ensure no message loss  
✅ **Flexibility**: Easy to add new consumers for audit logs, analytics, etc.  
✅ **Language Agnostic**: Java and Python services communicate seamlessly  

---

## 🔧 Potential Improvements

1. **Dead Letter Queue (DLQ)**: Add DLQ for failed message handling
2. **Retry Mechanism**: Implement exponential backoff for failed notifications
3. **Message Deduplication**: Prevent duplicate email sends
4. **Monitoring**: Add RabbitMQ metrics (queue depth, consumer lag)
5. **Remove Dual Consumption**: Choose either @RabbitListener OR Spring Cloud Stream
6. **Add Correlation IDs**: Track message flow across services

---

**Generated**: 2026-04-07  
**Author**: GitHub Copilot CLI  
**Version**: 1.0
