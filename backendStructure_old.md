# E-commerce System Architecture

## 1. System Overview

This e-commerce platform follows a **microservices architecture** pattern built with **Spring Boot 3.4.x** and **Spring Cloud 2024.0.x**. The system is designed for scalability, fault tolerance, and maintainability with clear separation of concerns.

### Architecture Pattern: Microservices + Event-Driven Hybrid

The system combines:
- **Synchronous REST APIs** for real-time operations (user actions, queries)
- **Asynchronous messaging** (RabbitMQ/Kafka) for event-driven workflows (order processing, notifications)
- **Service mesh patterns** via Spring Cloud (discovery, configuration, circuit breakers)

### Key Design Principles

| Principle | Implementation |
|-----------|----------------|
| Database per Service | Each service owns its data (MongoDB, PostgreSQL) |
| API Gateway Pattern | Single entry point with routing, rate limiting, authentication |
| Service Discovery | Netflix Eureka for dynamic service registration |
| Centralized Configuration | Spring Cloud Config Server with Git/Native backends |
| Circuit Breaker | Resilience4j for fault tolerance |
| Event Sourcing | RabbitMQ/Kafka for async communication |

---

## 2. Service Breakdown

### Infrastructure Services

| Service | Responsibility | Tech Stack | Port |
|---------|----------------|------------|------|
| **Eureka Server** | Service discovery & registration | Spring Cloud Netflix Eureka | 8761 |
| **Config Server** | Centralized configuration management | Spring Cloud Config | 8888 |
| **API Gateway** | Routing, authentication, rate limiting, circuit breakers | Spring Cloud Gateway, Resilience4j, Redis | 8080 |

### Business Services

| Service | Responsibility | Tech Stack | Database | Port |
|---------|----------------|------------|----------|------|
| **User Service** | Authentication, authorization, user management, loyalty, OAuth2 | Spring Boot, Spring Security, JWT | MongoDB Atlas | 8082 |
| **Product Service** | Product catalog, inventory, campaigns, reviews, FAQs | Spring Boot, JPA | PostgreSQL | 8081 |
| **Order Service** | Orders, cart, shipping, returns, refunds, seller management | Spring Boot, JPA, Resilience4j | PostgreSQL | 8083 |
| **Payment Gateway** | Payment processing, Bakong KHQR integration | Python FastAPI, Motor | MongoDB Atlas | 8976 |
| **Notification Service** | Event-driven notifications (email, Telegram) | Spring Boot, Kafka | - | 8084 |

### Supporting Services

| Service | Responsibility | Tech Stack |
|---------|----------------|------------|
| **RabbitMQ** | Message broker for service communication | CloudAMQP (managed) |
| **Apache Kafka** | Event streaming for notifications | Kafka Cluster |
| **Redis** | Caching, session storage, rate limiting | Redis 7.x |
| **PostgreSQL** | Relational data storage | PostgreSQL 14 |
| **MongoDB** | Document storage for users & payments | MongoDB Atlas |

---

## 3. Architecture Diagrams

### 3.1 High-Level System Architecture

```mermaid
flowchart TB
    subgraph Clients["Client Layer"]
        WEB["Web Application<br/>(React/Vue)"]
        MOBILE["Mobile App<br/>(React Native)"]
        ADMIN["Admin Dashboard"]
    end

    subgraph Gateway["API Gateway Layer"]
        GW["Spring Cloud Gateway<br/>:8080"]
        REDIS["Redis Cache"]
        GW -->|Rate Limiting| REDIS
    end

    subgraph Discovery["Service Discovery"]
        EUREKA["Eureka Server<br/>:8761"]
        CONFIG["Config Server<br/>:8888"]
    end

    subgraph Services["Business Services"]
        USER["User Service<br/>:8082<br/>(Spring Boot)"]
        PRODUCT["Product Service<br/>:8081<br/>(Spring Boot)"]
        ORDER["Order Service<br/>:8083<br/>(Spring Boot)"]
        PAYMENT["Payment Gateway<br/>:8976<br/>(Python FastAPI)"]
        NOTIFY["Notification Service<br/>:8084<br/>(Spring Boot)"]
    end

    subgraph DataLayer["Data Layer"]
        MONGO_USER[("MongoDB Atlas<br/>User Data")]
        PG_PRODUCT[("PostgreSQL<br/>Product DB")]
        PG_ORDER[("PostgreSQL<br/>Order DB")]
        MONGO_PAY[("MongoDB Atlas<br/>Payment Data")]
    end

    subgraph Messaging["Message Brokers"]
        RABBIT["RabbitMQ<br/>(CloudAMQP)"]
        KAFKA["Apache Kafka"]
    end

    subgraph External["External Services"]
        BAKONG["Bakong KHQR<br/>Payment"]
        GMAIL["Gmail SMTP"]
        TELEGRAM["Telegram Bot"]
        OAUTH["OAuth2 Providers<br/>(Google, GitHub, Facebook)"]
    end

    %% Client connections
    WEB & MOBILE & ADMIN --> GW

    %% Gateway routing
    GW --> USER & PRODUCT & ORDER & PAYMENT

    %% Service Discovery
    USER & PRODUCT & ORDER & NOTIFY -.->|Register| EUREKA
    USER & PRODUCT & ORDER & NOTIFY -.->|Fetch Config| CONFIG

    %% Database connections
    USER --> MONGO_USER
    PRODUCT --> PG_PRODUCT
    ORDER --> PG_ORDER
    PAYMENT --> MONGO_PAY

    %% Messaging
    ORDER -->|Publish Events| RABBIT
    RABBIT -->|Route| KAFKA
    KAFKA -->|Consume| NOTIFY

    %% External integrations
    PAYMENT --> BAKONG
    USER --> OAUTH
    NOTIFY --> GMAIL & TELEGRAM

    %% Config refresh
    CONFIG -->|Bus Refresh| RABBIT
```

### 3.2 Service Communication Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant GW as API Gateway
    participant US as User Service
    participant PS as Product Service
    participant OS as Order Service
    participant PG as Payment Gateway
    participant RMQ as RabbitMQ
    participant KFK as Kafka
    participant NS as Notification Service

    %% Authentication Flow
    C->>GW: POST /api/auth/login
    GW->>US: Forward request
    US-->>GW: JWT Token
    GW-->>C: Token Response

    %% Order Creation Flow
    C->>GW: POST /api/orders (with JWT)
    GW->>GW: Validate JWT
    GW->>OS: Create Order
    OS->>PS: GET /api/products/{id} (verify stock)
    PS-->>OS: Product Details
    OS->>OS: Calculate Total
    OS->>RMQ: Publish OrderCreatedEvent
    OS-->>GW: Order Created
    GW-->>C: Order Response

    %% Payment Flow
    C->>GW: POST /orders (Payment Gateway)
    GW->>PG: Create Payment
    PG->>PG: Generate Bakong KHQR
    PG-->>GW: QR Code + Payment ID
    GW-->>C: Display QR

    %% Payment Confirmation
    C->>GW: GET /orders/{id}/status
    GW->>PG: Check Payment Status
    PG->>PG: Verify via Bakong API
    PG-->>GW: Payment Confirmed
    GW-->>C: Status: PAID

    %% Notification Flow (Async)
    RMQ->>KFK: Forward Event
    KFK->>NS: Consume OrderCreatedEvent
    NS->>NS: Send Email/Telegram
```

### 3.3 Microservices Deployment Architecture

```mermaid
flowchart TB
    subgraph Docker["Docker Environment"]
        subgraph InfraContainers["Infrastructure Containers"]
            E_C["eureka-server"]
            CF_C["config-server"]
            GW_C["gateway-service"]
        end

        subgraph ServiceContainers["Service Containers"]
            U_C["user-service"]
            P_C["product-service"]
            O_C["order-service"]
            PY_C["payment-gateway"]
            N_C["notification-service"]
        end

        subgraph DataContainers["Data Containers"]
            PG_C["PostgreSQL 14"]
            RD_C["Redis 7"]
            RMQ_C["RabbitMQ"]
        end
    end

    subgraph CloudServices["Managed Cloud Services"]
        MONGO_CLOUD["MongoDB Atlas"]
        AMQP_CLOUD["CloudAMQP (RabbitMQ)"]
        KAFKA_CLOUD["Kafka Cluster"]
    end

    subgraph Observability["Observability Stack"]
        PROM["Prometheus"]
        GRAF["Grafana"]
        LOKI["Loki"]
        ALLOY["Grafana Alloy"]
        ZIPKIN["Zipkin"]
    end

    ServiceContainers --> DataContainers
    ServiceContainers --> CloudServices
    ServiceContainers -.->|Metrics| PROM
    ServiceContainers -.->|Logs| ALLOY
    ALLOY --> LOKI
    PROM & LOKI --> GRAF
    ServiceContainers -.->|Traces| ZIPKIN
```

---

## 4. Communication Flow

### 4.1 Synchronous Communication (REST APIs)

| From | To | Purpose | Protocol |
|------|-----|---------|----------|
| Gateway | User Service | Authentication, user data | REST/HTTP |
| Gateway | Product Service | Product catalog, search | REST/HTTP |
| Gateway | Order Service | Order management | REST/HTTP |
| Gateway | Payment Gateway | Payment processing | REST/HTTP |
| Order Service | Product Service | Stock verification | REST/HTTP (Circuit Breaker) |
| Order Service | User Service | User validation | REST/HTTP (Circuit Breaker) |

### 4.2 Asynchronous Communication (Events)

```mermaid
flowchart LR
    subgraph Publishers
        OS["Order Service"]
        US["User Service"]
        PS["Product Service"]
    end

    subgraph MessageBrokers
        RMQ["RabbitMQ<br/>order-exchange"]
        KFK["Kafka<br/>order.exchange topic"]
    end

    subgraph Consumers
        NS["Notification Service"]
        INV["Inventory Handler"]
    end

    OS -->|OrderCreatedEvent| RMQ
    OS -->|OrderPaidEvent| RMQ
    OS -->|OrderShippedEvent| RMQ
    US -->|UserRegisteredEvent| RMQ
    PS -->|LowStockAlert| RMQ

    RMQ --> KFK
    KFK --> NS
    RMQ --> INV
```

### 4.3 Event Definitions

| Event | Publisher | Consumer(s) | Payload |
|-------|-----------|-------------|---------|
| `OrderCreatedEvent` | Order Service | Notification Service | orderId, userId, items, total |
| `OrderPaidEvent` | Payment Gateway | Order Service, Notification | orderId, paymentId, amount |
| `OrderShippedEvent` | Order Service | Notification Service | orderId, trackingNumber |
| `UserRegisteredEvent` | User Service | Notification Service | userId, email, name |
| `LowStockAlert` | Product Service | Notification Service | productId, currentStock |

### 4.4 RabbitMQ Configuration

```yaml
Exchange: order-exchange (Topic)
Queues:
  - order.queue (routing key: order.tracker)
  - notification.queue (routing key: order.*)
  - inventory.queue (routing key: stock.*)
```

---

## 5. Database Design

### 5.1 Database per Service Pattern

```mermaid
erDiagram
    USER_SERVICE ||--o{ MONGODB_USER : uses
    PRODUCT_SERVICE ||--o{ POSTGRESQL_PRODUCT : uses
    ORDER_SERVICE ||--o{ POSTGRESQL_ORDER : uses
    PAYMENT_SERVICE ||--o{ MONGODB_PAYMENT : uses

    MONGODB_USER {
        string _id PK
        string email UK
        string password
        string role
        object addresses
        object loyaltyAccount
        datetime createdAt
    }

    POSTGRESQL_PRODUCT {
        bigint id PK
        string name
        decimal price
        int stockQuantity
        string category
        string sellerId
        jsonb images
        jsonb sizes
        jsonb colors
    }

    POSTGRESQL_ORDER {
        bigint id PK
        string userId FK
        decimal totalAmount
        string status
        jsonb items
        jsonb shippingAddress
        jsonb paymentResult
    }

    MONGODB_PAYMENT {
        string _id PK
        string orderId FK
        string status
        decimal amount
        string qrCode
        datetime createdAt
    }
```

### 5.2 Database Technology Selection

| Service | Database | Rationale |
|---------|----------|-----------|
| User Service | MongoDB | Flexible schema for user profiles, addresses, sessions; horizontal scaling |
| Product Service | PostgreSQL | Complex queries, full-text search, ACID transactions for inventory |
| Order Service | PostgreSQL | Transactional integrity for orders, complex joins for reporting |
| Payment Gateway | MongoDB | Fast writes, flexible payment metadata, audit logs |

### 5.3 Data Consistency Strategy

| Pattern | Usage |
|---------|-------|
| **Saga Pattern** | Multi-service transactions (order → payment → inventory) |
| **Eventual Consistency** | Cross-service data sync via events |
| **Idempotency Keys** | Prevent duplicate payments/orders |
| **Optimistic Locking** | Concurrent inventory updates |

---

## 6. Message Queue / Event System

### 6.1 Technology Stack

| Technology | Purpose | Use Case |
|------------|---------|----------|
| **RabbitMQ (CloudAMQP)** | Service-to-service messaging | Config refresh, order events, direct routing |
| **Apache Kafka** | Event streaming | High-throughput notification processing |

### 6.2 RabbitMQ Architecture

```mermaid
flowchart TB
    subgraph Producers
        OS["Order Service"]
        CONFIG["Config Server"]
    end

    subgraph RabbitMQ["RabbitMQ (CloudAMQP)"]
        EX1["order-exchange<br/>(topic)"]
        EX2["springCloudBus<br/>(topic)"]

        Q1["order.queue"]
        Q2["notification.queue"]
        Q3["config.refresh.queue"]
    end

    subgraph Consumers
        NS["Notification Service"]
        ALL["All Services<br/>(Config Refresh)"]
    end

    OS -->|order.created| EX1
    OS -->|order.paid| EX1
    CONFIG -->|config.refresh| EX2

    EX1 -->|order.*| Q1
    EX1 -->|order.*| Q2
    EX2 -->|*.refresh| Q3

    Q2 --> NS
    Q3 --> ALL
```

### 6.3 Kafka Configuration

```yaml
# Notification Service - Kafka Consumer
spring:
  cloud:
    stream:
      kafka:
        binder:
          brokers: localhost:9092
      bindings:
        orderEventConsumer-in-0:
          destination: order.exchange
          group: notification-group
```

### 6.4 Event Flow Example: Order Processing

```mermaid
sequenceDiagram
    participant Client
    participant OrderService
    participant RabbitMQ
    participant Kafka
    participant NotificationService
    participant PaymentGateway
    participant InventoryService

    Client->>OrderService: Create Order
    OrderService->>OrderService: Validate & Save
    OrderService->>RabbitMQ: Publish OrderCreatedEvent
    OrderService-->>Client: Order Created

    par Parallel Processing
        RabbitMQ->>Kafka: Forward to Kafka
        Kafka->>NotificationService: Consume Event
        NotificationService->>NotificationService: Send Email
    and
        RabbitMQ->>InventoryService: Reserve Stock
        InventoryService->>InventoryService: Update Inventory
    end

    Client->>PaymentGateway: Pay Order
    PaymentGateway->>PaymentGateway: Process Bakong KHQR
    PaymentGateway->>RabbitMQ: Publish OrderPaidEvent

    RabbitMQ->>OrderService: OrderPaidEvent
    OrderService->>OrderService: Update Status
```

---

## 7. Deployment Architecture

### 7.1 Docker Compose Setup

```yaml
# docker-compose.yml structure
version: '3.8'

services:
  # Infrastructure
  eureka-server:
    build: ./eureka
    ports: ["8761:8761"]

  config-server:
    build: ./configserver
    ports: ["8888:8888"]
    depends_on: [eureka-server]

  gateway:
    build: ./gateway
    ports: ["8080:8080"]
    depends_on: [eureka-server, config-server]

  # Business Services
  user-service:
    build: ./user
    ports: ["8082:8082"]
    depends_on: [eureka-server, config-server]

  product-service:
    build: ./product
    ports: ["8081:8081"]
    depends_on: [eureka-server, config-server, postgres]

  order-service:
    build: ./order
    ports: ["8083:8083"]
    depends_on: [eureka-server, config-server, postgres, rabbitmq]

  payment-gateway:
    build: ./payment-gateway
    ports: ["8976:8976"]

  notification-service:
    build: ./notification
    ports: ["8084:8084"]
    depends_on: [kafka]

  # Data Services
  postgres:
    image: postgres:14
    ports: ["5432:5432"]
    volumes: [postgres_data:/var/lib/postgresql/data]

  redis:
    image: redis:7-alpine
    ports: ["6379:6379"]

  rabbitmq:
    image: rabbitmq:3-management
    ports: ["5672:5672", "15672:15672"]

  # Observability
  prometheus:
    image: prom/prometheus
    ports: ["9090:9090"]

  grafana:
    image: grafana/grafana
    ports: ["3000:3000"]

  loki:
    image: grafana/loki
    ports: ["3100:3100"]
```

### 7.2 Kubernetes Deployment (Future)

```mermaid
flowchart TB
    subgraph K8S["Kubernetes Cluster"]
        subgraph Ingress
            ING["NGINX Ingress<br/>Controller"]
        end

        subgraph Namespace_Infra["Namespace: infra"]
            EUR_D["Eureka<br/>Deployment"]
            CFG_D["Config<br/>Deployment"]
            GW_D["Gateway<br/>Deployment"]
        end

        subgraph Namespace_Services["Namespace: services"]
            USER_D["User Service<br/>Deployment (3 replicas)"]
            PROD_D["Product Service<br/>Deployment (3 replicas)"]
            ORD_D["Order Service<br/>Deployment (3 replicas)"]
            PAY_D["Payment Gateway<br/>Deployment (2 replicas)"]
            NOT_D["Notification<br/>Deployment (2 replicas)"]
        end

        subgraph Namespace_Data["Namespace: data"]
            PG_SS["PostgreSQL<br/>StatefulSet"]
            RD_SS["Redis<br/>StatefulSet"]
        end

        subgraph ConfigMaps
            CM["ConfigMaps"]
            SEC["Secrets"]
        end
    end

    ING --> GW_D
    GW_D --> USER_D & PROD_D & ORD_D & PAY_D
    USER_D & PROD_D & ORD_D -.-> EUR_D
    USER_D & PROD_D & ORD_D -.-> CFG_D
    PROD_D & ORD_D --> PG_SS
    GW_D --> RD_SS
```

### 7.3 Service Startup Order

```mermaid
flowchart LR
    PG["PostgreSQL"] --> PS["Product Service"]
    PG --> OS["Order Service"]

    EUR["Eureka Server"] --> CFG["Config Server"]
    CFG --> US["User Service"]
    CFG --> PS
    CFG --> OS
    CFG --> GW["Gateway"]

    RMQ["RabbitMQ"] --> OS
    KFK["Kafka"] --> NS["Notification Service"]

    US & PS & OS --> GW
```

---

## 8. Security Design

### 8.1 Authentication & Authorization Flow

```mermaid
sequenceDiagram
    participant User
    participant Gateway
    participant UserService
    participant JWT
    participant ProtectedService

    User->>Gateway: POST /api/auth/login
    Gateway->>UserService: Forward Login Request
    UserService->>UserService: Validate Credentials
    UserService->>JWT: Generate Access + Refresh Tokens
    JWT-->>UserService: Tokens
    UserService-->>Gateway: {accessToken, refreshToken}
    Gateway-->>User: Tokens + Set HttpOnly Cookie

    User->>Gateway: GET /api/orders (Authorization: Bearer token)
    Gateway->>Gateway: Validate JWT
    Gateway->>Gateway: Extract Claims
    Gateway->>ProtectedService: Forward + Add X-User-Id Header
    ProtectedService-->>Gateway: Response
    Gateway-->>User: Response
```

### 8.2 Security Components

| Layer | Security Measure | Implementation |
|-------|------------------|----------------|
| **API Gateway** | Rate Limiting | Resilience4j RateLimiter (2 req/4s default) |
| **API Gateway** | Circuit Breaker | Resilience4j CircuitBreaker |
| **API Gateway** | JWT Validation | Spring Security + JJWT |
| **User Service** | Password Hashing | BCrypt |
| **User Service** | OAuth2 | Google, GitHub, Facebook |
| **User Service** | Session Management | Multi-device session tracking |
| **All Services** | HTTPS | TLS/SSL termination at Gateway |
| **All Services** | CORS | Configured per service |

### 8.3 JWT Token Structure

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user_id",
    "email": "user@example.com",
    "role": "CUSTOMER|SELLER|ADMIN",
    "iat": 1234567890,
    "exp": 1234571490
  }
}
```

### 8.4 Security Best Practices Implemented

| Practice | Implementation |
|----------|----------------|
| Token Expiry | Access: 15min, Refresh: 7 days |
| Password Reset | 6-digit code with expiry |
| Session Invalidation | Logout terminates all sessions option |
| Trust Scoring | Customer behavior analysis |
| Loyalty Security | Referral code validation |

### 8.5 API Gateway Security Configuration

```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10000
        permittedNumberOfCallsInHalfOpenState: 3

  ratelimiter:
    configs:
      default:
        limitForPeriod: 2
        limitRefreshPeriod: 4s
        timeoutDuration: 5s

  retry:
    configs:
      default:
        maxAttempts: 5
        waitDuration: 5000
```

---

## 9. Scalability Strategy

### 9.1 Horizontal Scaling

```mermaid
flowchart TB
    subgraph LoadBalancer["Load Balancer"]
        LB["NGINX / Cloud LB"]
    end

    subgraph GatewayCluster["Gateway Cluster"]
        GW1["Gateway 1"]
        GW2["Gateway 2"]
        GW3["Gateway 3"]
    end

    subgraph ServiceCluster["Service Instances"]
        subgraph UserCluster["User Service"]
            US1["Instance 1"]
            US2["Instance 2"]
            US3["Instance 3"]
        end

        subgraph ProductCluster["Product Service"]
            PS1["Instance 1"]
            PS2["Instance 2"]
        end

        subgraph OrderCluster["Order Service"]
            OS1["Instance 1"]
            OS2["Instance 2"]
            OS3["Instance 3"]
        end
    end

    subgraph Eureka["Service Registry"]
        EUR["Eureka Server<br/>(Client-side LB)"]
    end

    LB --> GW1 & GW2 & GW3
    GW1 & GW2 & GW3 --> EUR
    EUR --> UserCluster & ProductCluster & OrderCluster
```

### 9.2 Scaling Strategies by Service

| Service | Scaling Strategy | Considerations |
|---------|------------------|----------------|
| **Gateway** | Horizontal (3+ instances) | Stateless, use Redis for rate limit sync |
| **User Service** | Horizontal (2-3 instances) | Stateless JWT, MongoDB handles scale |
| **Product Service** | Horizontal + Read Replicas | PostgreSQL read replicas for queries |
| **Order Service** | Horizontal + Saga Pattern | Ensure idempotency, distributed locking |
| **Payment Gateway** | Horizontal (2 instances) | Idempotency keys for payments |
| **Notification Service** | Horizontal (auto-scale) | Based on Kafka partition count |

### 9.3 Caching Strategy

```mermaid
flowchart LR
    subgraph Clients
        C["Client Requests"]
    end

    subgraph CacheLayers["Cache Layers"]
        CDN["CDN Cache<br/>(Static Assets)"]
        REDIS["Redis Cache<br/>(Session, Rate Limit)"]
        APP["Application Cache<br/>(Spring Cache)"]
    end

    subgraph Services
        GW["Gateway"]
        PS["Product Service"]
    end

    subgraph Database
        DB["PostgreSQL"]
    end

    C --> CDN
    CDN --> GW
    GW --> REDIS
    GW --> PS
    PS --> APP
    APP -->|Cache Miss| DB
    APP -->|Cache Hit| PS
```

### 9.4 Caching Implementation

| Cache Type | Technology | TTL | Use Case |
|------------|------------|-----|----------|
| Session Cache | Redis | 30 min | User sessions, JWT blacklist |
| Rate Limit | Redis | 4 sec | API rate limiting counters |
| Product Cache | Spring Cache | 5 min | Product listings, categories |
| Config Cache | Config Server | On refresh | Application configuration |

### 9.5 Database Scaling

| Database | Scaling Approach |
|----------|------------------|
| **MongoDB Atlas** | Built-in sharding, auto-scaling |
| **PostgreSQL** | Read replicas, connection pooling (HikariCP) |
| **Redis** | Redis Cluster for high availability |

---

## 10. Observability Stack

### 10.1 Monitoring Architecture

```mermaid
flowchart TB
    subgraph Services["Application Services"]
        US["User Service<br/>/actuator/prometheus"]
        PS["Product Service<br/>/actuator/prometheus"]
        OS["Order Service<br/>/actuator/prometheus"]
    end

    subgraph Collection["Metric Collection"]
        PROM["Prometheus<br/>:9090"]
    end

    subgraph Visualization["Dashboards"]
        GRAF["Grafana<br/>:3000"]
    end

    subgraph Alerting
        AM["Alert Manager"]
    end

    US & PS & OS -->|Scrape /prometheus| PROM
    PROM --> GRAF
    PROM --> AM
    AM -->|Slack/Email| NOTIFY["Notifications"]
```

### 10.2 Logging Architecture (Loki Stack)

```mermaid
flowchart LR
    subgraph Apps["Application Logs"]
        A1["user-service.log"]
        A2["product-service.log"]
        A3["order-service.log"]
        DC["Docker Containers"]
    end

    subgraph Collection
        ALLOY["Grafana Alloy<br/>(Log Collector)"]
    end

    subgraph Storage
        LOKI["Loki<br/>(Log Aggregation)"]
        MINIO["MinIO<br/>(S3 Storage)"]
    end

    subgraph Query
        GRAF["Grafana<br/>(LogQL)"]
    end

    A1 & A2 & A3 --> ALLOY
    DC --> ALLOY
    ALLOY --> LOKI
    LOKI --> MINIO
    LOKI --> GRAF
```

### 10.3 Distributed Tracing (Zipkin)

```mermaid
flowchart LR
    subgraph Request["Incoming Request"]
        REQ["GET /api/orders/123"]
    end

    subgraph Tracing["Trace Propagation"]
        GW["Gateway<br/>traceId: abc123"]
        OS["Order Service<br/>spanId: def456"]
        PS["Product Service<br/>spanId: ghi789"]
        US["User Service<br/>spanId: jkl012"]
    end

    subgraph Collector
        ZIP["Zipkin Server"]
    end

    REQ --> GW
    GW -->|X-B3-TraceId| OS
    OS -->|X-B3-TraceId| PS
    OS -->|X-B3-TraceId| US

    GW & OS & PS & US -.->|Report Spans| ZIP
```

### 10.4 Metrics Exposed

| Metric Category | Examples |
|-----------------|----------|
| **JVM Metrics** | heap memory, GC pauses, thread count |
| **HTTP Metrics** | request count, latency percentiles, error rate |
| **Business Metrics** | orders created, payments processed |
| **Circuit Breaker** | state, failure rate, slow call rate |

---

## 11. Future Improvements

### 11.1 Short-term Enhancements

| Improvement | Priority | Benefit |
|-------------|----------|---------|
| Add Zipkin Server deployment | High | Complete distributed tracing |
| Externalize secrets (Vault) | High | Security compliance |
| Implement CQRS for Orders | Medium | Better read/write scaling |
| Add API versioning | Medium | Backward compatibility |
| GraphQL Gateway | Medium | Flexible client queries |

### 11.2 Long-term Evolution

| Improvement | Description |
|-------------|-------------|
| **Kubernetes Migration** | Full K8s deployment with Helm charts |
| **Service Mesh (Istio)** | Advanced traffic management, mTLS |
| **Event Sourcing** | Full audit trail for orders/payments |
| **Multi-region Deployment** | Geographic redundancy |
| **AI/ML Integration** | Recommendation engine, fraud detection |

### 11.3 Architecture Evolution Roadmap

```mermaid
timeline
    title Architecture Evolution Roadmap

    section Phase 1 - Foundation
        Current : Docker Compose deployment
               : Basic observability (Prometheus, Loki)
               : RabbitMQ messaging

    section Phase 2 - Hardening
        Q2 2026 : Kubernetes migration
               : Secrets management (HashiCorp Vault)
               : Enhanced security (mTLS)

    section Phase 3 - Scale
        Q4 2026 : Multi-region deployment
               : Event sourcing implementation
               : CQRS for high-traffic services

    section Phase 4 - Intelligence
        2027 : ML-based recommendations
            : Real-time fraud detection
            : Predictive inventory management
```

---

## 12. Quick Reference

### 12.1 Service Endpoints

| Service | Local URL | Health Check |
|---------|-----------|--------------|
| Eureka Dashboard | http://localhost:8761 | /actuator/health |
| Config Server | http://localhost:8888 | /actuator/health |
| API Gateway | http://localhost:8080 | /actuator/health |
| User Service | http://localhost:8082 | /actuator/health |
| Product Service | http://localhost:8081 | /actuator/health |
| Order Service | http://localhost:8083 | /actuator/health |
| Payment Gateway | http://localhost:8976 | /health |
| Notification Service | http://localhost:8084 | /actuator/health |

### 12.2 External Dashboards

| Tool | URL | Purpose |
|------|-----|---------|
| Grafana | http://localhost:3000 | Metrics & Logs visualization |
| Prometheus | http://localhost:9090 | Metrics queries |
| RabbitMQ | http://localhost:15672 | Message broker management |
| PGAdmin | http://localhost:5050 | PostgreSQL administration |

### 12.3 Key Configuration Files

| File | Purpose |
|------|---------|
| `configserver/src/main/resources/config/*.yml` | Centralized service configs |
| `docker-compose.yml` | Container orchestration |
| `additional/evaluate-prometheus/prometheus/prometheus.yml` | Prometheus scrape targets |
| `additional/evaluate-prometheus/logging/loki-config.yaml` | Loki log aggregation |

---

## 13. Feature Analysis (Detailed)

### 13.1 User Authentication & Management

| Feature | Description | API Endpoint |
|---------|-------------|--------------|
| **User Registration** | Email-based registration with password validation | `POST /api/auth/register` |
| **User Login** | JWT-based authentication with session tracking | `POST /api/auth/login` |
| **Token Refresh** | Access token renewal using refresh tokens | `POST /api/auth/refresh` |
| **Password Reset** | 6-digit code via email for password recovery | `POST /api/auth/forgot-password` |
| **Session Management** | Multi-device session tracking and termination | `GET /api/auth/sessions` |
| **OAuth2 Integration** | Google, GitHub, Facebook social login | `GET /api/oauth2/{provider}` |
| **User Profile** | Profile management with addresses | `GET/PUT /api/users/{id}` |
| **Trust Scoring** | Customer behavior analysis for fraud prevention | Internal service |

### 13.2 Product Catalog & Management

| Feature | Description | API Endpoint |
|---------|-------------|--------------|
| **Product CRUD** | Create, read, update, delete products | `POST/GET/PUT/DELETE /api/products` |
| **Product Search** | Keyword-based full-text search | `GET /api/products/search` |
| **Advanced Filtering** | Filter by category, price, colors, sizes | `GET /api/products/filter` |
| **Product Reviews** | Customer reviews with ratings | `POST/GET /api/products/{id}/reviews` |
| **Product FAQs** | Question and answer per product | `POST/GET /api/products/{id}/faqs` |
| **Top Products** | Bestseller and trending products | `GET /api/products/top` |
| **Seller Products** | Products by specific seller | `GET /api/products/seller/{sellerId}` |

### 13.3 Shopping Cart

| Feature | Description | API Endpoint |
|---------|-------------|--------------|
| **Add to Cart** | Add products with quantity and variants | `POST /api/cart` |
| **View Cart** | Get all items in user's cart | `GET /api/cart` |
| **Remove Item** | Remove specific product from cart | `DELETE /api/cart/items/{productId}` |
| **Stock Validation** | Real-time stock check before adding | Internal validation |

### 13.4 Order Processing

| Feature | Description | API Endpoint |
|---------|-------------|--------------|
| **Create Order** | Convert cart to order with shipping | `POST /api/orders` |
| **View Orders** | List all user orders | `GET /api/orders/myorders` |
| **Order Details** | Single order information | `GET /api/orders/{id}` |
| **Mark as Paid** | Update payment status | `PUT /api/orders/{id}/pay` |
| **Mark as Delivered** | Update delivery status | `PUT /api/orders/{id}/deliver` |

**Order States:**
```
PENDING → PAID → PROCESSING → SHIPPED → DELIVERED
                    ↓
              CANCELLED / REFUNDED
```

### 13.5 Payment Integration (Bakong KHQR)

| Feature | Description | API Endpoint |
|---------|-------------|--------------|
| **QR Code Generation** | Bakong KHQR payment QR | `POST /orders` (Python) |
| **Payment Status Check** | Real-time payment verification | `GET /orders/{id}/status` |
| **Multi-currency** | Support for USD | Built-in |

### 13.6 Inventory Management

| Feature | Description | API Endpoint |
|---------|-------------|--------------|
| **Inventory Overview** | Stock summary dashboard | `GET /api/inventory/overview` |
| **Stock Alerts** | Low stock notifications | `GET /api/inventory/alerts` |
| **Add Stock** | Increase product quantity | `POST /api/inventory/products/{id}/add-stock` |
| **Remove Stock** | Decrease product quantity | `POST /api/inventory/products/{id}/remove-stock` |
| **Stock History** | Movement audit trail | `GET /api/inventory/products/{id}/history` |
| **Low Stock Report** | Products below threshold | `GET /api/inventory/low-stock` |

### 13.7 Shipping & Delivery

| Feature | Description | API Endpoint |
|---------|-------------|--------------|
| **Shipping Quote** | Calculate shipping cost | `POST /api/shipping/quote` |
| **Province-based Pricing** | Cambodia province rates | Admin configuration |
| **Delivery Tracking** | Shipment status updates | `GET /api/delivery/{id}` |

### 13.8 Returns & Refunds

| Feature | Description | API Endpoint |
|---------|-------------|--------------|
| **Create Return Request** | Initiate product return | `POST /api/returns` |
| **View Returns** | List user's return requests | `GET /api/returns` |
| **Admin Returns** | All returns for admin | `GET /api/returns/admin` |
| **Approve Return** | Accept return request | `PUT /api/returns/{id}/approve` |
| **Reject Return** | Deny return request | `PUT /api/returns/{id}/reject` |
| **Process Refund** | Issue refund | `POST /api/returns/{id}/refund` |

### 13.9 Loyalty & Rewards Program

| Feature | Description | API Endpoint |
|---------|-------------|--------------|
| **Loyalty Account** | Points balance and tier | `GET /api/loyalty/account` |
| **Transaction History** | Points earned/spent | `GET /api/loyalty/transactions` |
| **Redeem Points** | Convert points to discount | `POST /api/loyalty/redeem` |
| **Referral Code** | Generate unique referral link | `GET /api/loyalty/referral-code` |
| **Apply Referral** | Use friend's referral code | `POST /api/loyalty/apply-referral` |

### 13.10 Seller Management

| Feature | Description | API Endpoint |
|---------|-------------|--------------|
| **Financial Overview** | Revenue, balance, fees | `GET /api/sellers/financials/overview` |
| **Transactions** | Sales transaction history | `GET /api/sellers/financials/transactions` |
| **Payouts** | Request/view payouts | `GET/POST /api/sellers/financials/payouts` |
| **Bank Accounts** | Manage payout destinations | `GET/POST /api/sellers/financials/bank-accounts` |
| **Revenue Reports** | Date-range revenue analysis | `GET /api/sellers/financials/reports/revenue` |

### 13.11 Analytics & Reporting

| Feature | Description | API Endpoint |
|---------|-------------|--------------|
| **Overview Dashboard** | Sales, orders, revenue | `GET /api/analytics/overview` |
| **Sales Trends** | Daily/weekly/monthly trends | `GET /api/analytics/sales-trend` |
| **Category Performance** | Sales by category | `GET /api/analytics/category-performance` |
| **Payment Methods** | Payment method breakdown | `GET /api/analytics/payment-methods` |

---

## 14. API Reference Summary

### Complete Endpoint Reference

#### Authentication (`/api/auth`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/register` | User registration |
| POST | `/login` | User login |
| POST | `/refresh` | Token refresh |
| POST | `/logout` | User logout |
| GET | `/me` | Current user info |
| POST | `/forgot-password` | Request password reset |
| POST | `/verify-reset-code` | Verify 6-digit code |
| POST | `/reset-password` | Reset password |
| GET | `/sessions` | Active sessions |
| DELETE | `/sessions/{id}` | Terminate session |

#### Products (`/api/products`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | List all products |
| POST | `/` | Create product |
| GET | `/{id}` | Get product by ID |
| PUT | `/{id}` | Update product |
| DELETE | `/{id}` | Delete product |
| GET | `/search` | Search products |
| GET | `/filter` | Filter products |
| GET | `/top` | Top products |
| GET | `/{id}/reviews` | Product reviews |
| POST | `/{id}/reviews` | Create review |
| GET | `/{id}/faqs` | Product FAQs |

#### Orders (`/api/orders`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create order |
| GET | `/` | All orders |
| GET | `/{id}` | Order by ID |
| GET | `/myorders` | User's orders |
| PUT | `/{id}/pay` | Mark as paid |
| PUT | `/{id}/deliver` | Mark as delivered |

#### Cart (`/api/cart`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Add to cart |
| GET | `/` | View cart |
| DELETE | `/items/{productId}` | Remove from cart |

#### Shipping (`/api/shipping`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/quote` | Get shipping quote |

#### Returns (`/api/returns`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create return request |
| GET | `/` | User's returns |
| GET | `/admin` | Admin view returns |
| PUT | `/{id}/approve` | Approve return |
| PUT | `/{id}/reject` | Reject return |
| POST | `/{id}/refund` | Process refund |

#### Loyalty (`/api/loyalty`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/account` | Loyalty account |
| GET | `/transactions` | Point history |
| POST | `/redeem` | Redeem points |
| GET | `/referral-code` | Get referral code |
| POST | `/apply-referral` | Apply referral |

#### Seller Financials (`/api/sellers/financials`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/overview` | Financial overview |
| GET | `/transactions` | Transaction history |
| GET | `/payouts` | View payouts |
| POST | `/payouts/request` | Request payout |
| GET | `/bank-accounts` | Bank accounts |
| POST | `/bank-accounts` | Add bank account |

#### Analytics (`/api/analytics`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/overview` | Analytics overview |
| GET | `/sales-trend` | Sales trend |
| GET | `/category-performance` | Category stats |
| GET | `/payment-methods` | Payment breakdown |

#### Inventory (`/api/inventory`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/overview` | Inventory overview |
| GET | `/alerts` | Stock alerts |
| POST | `/products/{id}/add-stock` | Add stock |
| POST | `/products/{id}/remove-stock` | Remove stock |
| GET | `/products/{id}/history` | Stock history |
| GET | `/low-stock` | Low stock products |

---

*Document Version: 2.0*
*Last Updated: March 2026*
*Architecture Review: Quarterly*
