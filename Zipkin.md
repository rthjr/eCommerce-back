# Zipkin Distributed Tracing Documentation

## 📋 Overview

This e-commerce microservices application uses **Zipkin** for distributed tracing, enabling end-to-end visibility of requests as they flow through multiple services. The tracing is implemented using **Micrometer Tracing** with the **Brave** bridge and automatic span propagation.

---

## 🏗️ Distributed Tracing Architecture

```mermaid
flowchart TB
    subgraph Client["🖥️ Client"]
        BROWSER[Web Browser / API Client]
    end

    subgraph Gateway["🚪 API Gateway :8080"]
        GW[Gateway Service<br/>Trace Origin]
    end

    subgraph Services["⚙️ Microservices"]
        USER[User Service<br/>:8082]
        PRODUCT[Product Service<br/>:8081]
        ORDER[Order Service<br/>:8083]
        NOTIFY[Notification Service<br/>:8084]
    end

    subgraph Tracing["📊 Tracing Infrastructure"]
        ZIPKIN[Zipkin Server<br/>:9411]
    end

    BROWSER -->|Request| GW
    GW -->|Trace: abc123<br/>Span: span1| USER
    GW -->|Trace: abc123<br/>Span: span2| PRODUCT
    GW -->|Trace: abc123<br/>Span: span3| ORDER
    
    ORDER -->|Trace: abc123<br/>Span: span4| PRODUCT
    ORDER -->|Trace: abc123<br/>Span: span5| NOTIFY

    GW -.->|Report Spans| ZIPKIN
    USER -.->|Report Spans| ZIPKIN
    PRODUCT -.->|Report Spans| ZIPKIN
    ORDER -.->|Report Spans| ZIPKIN
    NOTIFY -.->|Report Spans| ZIPKIN
```

---

## 🔄 Complete Trace Flow

```mermaid
sequenceDiagram
    participant CLIENT as Client
    participant GW as API Gateway<br/>:8080
    participant ORDER as Order Service<br/>:8083
    participant PRODUCT as Product Service<br/>:8081
    participant ZIPKIN as Zipkin<br/>:9411

    Note over CLIENT,ZIPKIN: Trace ID: abc-123-xyz propagated across all services

    CLIENT->>GW: POST /api/orders
    Note right of GW: Generate Trace ID: abc-123-xyz<br/>Span ID: span-gw-001
    
    GW->>ORDER: Forward Request<br/>Headers: X-B3-TraceId, X-B3-SpanId, X-B3-ParentSpanId
    Note right of ORDER: Create child span: span-order-001<br/>Parent: span-gw-001

    ORDER->>PRODUCT: GET /api/products/{id}<br/>Headers: X-B3-TraceId, X-B3-SpanId, X-B3-ParentSpanId
    Note right of PRODUCT: Create child span: span-product-001<br/>Parent: span-order-001
    
    PRODUCT-->>ORDER: Product Details
    ORDER-->>GW: Order Created
    GW-->>CLIENT: 201 Created

    Note over GW,ZIPKIN: Async span reporting
    GW--)ZIPKIN: Report span-gw-001
    ORDER--)ZIPKIN: Report span-order-001
    PRODUCT--)ZIPKIN: Report span-product-001

    Note over ZIPKIN: Correlate spans by Trace ID<br/>Build dependency graph
```

---

## 📦 Technology Stack

| Component | Library | Purpose |
|-----------|---------|---------|
| **Micrometer Tracing** | `micrometer-tracing-bridge-brave` | Tracing abstraction layer |
| **Brave** | `brave` | Distributed tracing instrumentation |
| **Zipkin Reporter** | `zipkin-reporter-brave` | Send spans to Zipkin |
| **Zipkin Server** | `openzipkin/zipkin` | Trace collection & visualization |

---

## ⚙️ Service Configuration

### Maven Dependencies (All Services)

```xml
<!-- Micrometer Tracing with Brave bridge -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>

<!-- Zipkin span reporter -->
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

### Application Configuration

```yaml
# All services: user-service.yml, product-service.yml, order-service.yml, gateway-service.yml
management:
  tracing:
    sampling:
      probability: 1.0  # 100% of requests are traced
```

---

## 🔗 Trace Context Propagation

### B3 Headers (Zipkin Standard)

```mermaid
flowchart LR
    subgraph Headers["📨 HTTP Headers"]
        H1["X-B3-TraceId: abc123def456"]
        H2["X-B3-SpanId: span789"]
        H3["X-B3-ParentSpanId: parent456"]
        H4["X-B3-Sampled: 1"]
    end

    subgraph Meaning["📖 Meaning"]
        M1["Unique ID for entire request chain"]
        M2["ID of current operation"]
        M3["ID of calling operation"]
        M4["Whether to sample this trace"]
    end

    H1 --> M1
    H2 --> M2
    H3 --> M3
    H4 --> M4
```

### Propagation in Order Service

```mermaid
flowchart TD
    subgraph OrderService["📦 Order Service"]
        CONFIG[RestClientConfig.java]
        TRACER[Micrometer Tracer]
        PROPAGATOR[Propagator]
        INTERCEPTOR[ClientHttpRequestInterceptor]
    end

    subgraph OutgoingRequest["📤 Outgoing Request"]
        HEADERS[HTTP Headers]
    end

    CONFIG -->|Inject| TRACER
    TRACER -->|Get Current Span| PROPAGATOR
    PROPAGATOR -->|Inject Context| INTERCEPTOR
    INTERCEPTOR -->|Add B3 Headers| HEADERS
```

### Code: RestClientConfig.java

```java
@Configuration
public class RestClientConfig {

    @Autowired(required = false)
    private Tracer tracer;

    @Autowired(required = false)
    private Propagator propagator;

    @Bean
    @LoadBalanced
    public RestClient.Builder restClientBuilder() {
        RestClient.Builder builder = RestClient.builder();
        if (observationRegistry != null) {
            builder.requestInterceptor(createTracingInterceptor());
        }
        return builder;
    }

    private ClientHttpRequestInterceptor createTracingInterceptor() {
        return ((request, body, execution) -> {
            if (tracer != null && propagator != null && tracer.currentSpan() != null) {
                // Inject trace context into outgoing request headers
                propagator.inject(
                    tracer.currentTraceContext().context(),
                    request.getHeaders(),
                    (carrier, key, value) -> carrier.add(key, value)
                );
            }
            return execution.execute(request, body);
        });
    }
}
```

---

## 🌳 Span Hierarchy

```mermaid
flowchart TD
    subgraph Trace["🔍 Trace: abc-123-xyz"]
        ROOT["Root Span<br/>Gateway: POST /api/orders<br/>Duration: 250ms"]
        
        subgraph OrderSpan["Order Service Spans"]
            ORDER["Child Span<br/>Order: createOrder<br/>Duration: 200ms"]
            CART["Child Span<br/>Order: getCart<br/>Duration: 15ms"]
            STOCK["Child Span<br/>Order: reduceStock<br/>Duration: 30ms"]
        end

        subgraph ProductSpan["Product Service Spans"]
            PRODUCT1["Child Span<br/>Product: getProductDetails<br/>Duration: 25ms"]
            PRODUCT2["Child Span<br/>Product: reduceStock<br/>Duration: 20ms"]
        end

        subgraph DBSpan["Database Spans"]
            DB1["Child Span<br/>PostgreSQL: SELECT<br/>Duration: 5ms"]
            DB2["Child Span<br/>PostgreSQL: UPDATE<br/>Duration: 8ms"]
        end
    end

    ROOT --> ORDER
    ORDER --> CART
    ORDER --> STOCK
    STOCK --> PRODUCT2
    ORDER --> PRODUCT1
    PRODUCT1 --> DB1
    PRODUCT2 --> DB2
```

---

## 📊 Zipkin UI Features

```mermaid
flowchart LR
    subgraph ZipkinUI["🖥️ Zipkin UI :9411"]
        SEARCH["🔍 Trace Search"]
        DEPS["🔗 Dependencies"]
        TIMELINE["⏱️ Timeline View"]
        DETAIL["📋 Span Details"]
    end

    subgraph Features["Features"]
        F1["Search by service, trace ID,<br/>time range, duration"]
        F2["Service dependency graph<br/>Request flow visualization"]
        F3["Waterfall view of spans<br/>Latency breakdown"]
        F4["Span tags, annotations,<br/>error information"]
    end

    SEARCH --> F1
    DEPS --> F2
    TIMELINE --> F3
    DETAIL --> F4
```

---

## 🔄 Service Communication Patterns

### Traced Communication Types

```mermaid
flowchart TB
    subgraph Sync["🔗 Synchronous (Traced)"]
        direction LR
        REST["REST API Calls<br/>via RestClient"]
        FEIGN["Feign Clients<br/>via OpenFeign"]
    end

    subgraph Async["📨 Asynchronous (Separate Traces)"]
        direction LR
        RABBIT["RabbitMQ Messages<br/>New trace per message"]
    end

    subgraph Services["Services"]
        ORDER["Order Service"]
        PRODUCT["Product Service"]
        USER["User Service"]
        NOTIFY["Notification Service"]
    end

    ORDER -->|RestClient| PRODUCT
    NOTIFY -->|FeignClient| USER
    ORDER -->|RabbitMQ| NOTIFY
```

### Service-to-Service Calls

| From | To | Method | Trace Propagation |
|------|-----|--------|-------------------|
| Gateway | All Services | Spring Cloud Gateway | ✅ Automatic |
| Order | Product | RestClient | ✅ Via Interceptor |
| Notification | User | Feign Client | ✅ Automatic |
| Order | Notification | RabbitMQ | ❌ New Trace |

---

## 🚀 Order Creation Trace Example

```mermaid
sequenceDiagram
    participant C as Client
    participant G as Gateway
    participant O as Order Service
    participant P as Product Service
    participant DB as PostgreSQL

    Note over C,DB: Trace ID: trace-001

    C->>G: POST /api/orders
    activate G
    Note right of G: span: gateway-root

    G->>O: Forward to order-service
    activate O
    Note right of O: span: order-create

    O->>O: Get Cart Items
    Note right of O: span: cart-get

    loop For each cart item
        O->>P: GET /api/products/{id}
        activate P
        Note right of P: span: product-get
        P->>DB: SELECT * FROM products
        Note right of DB: span: db-query
        DB-->>P: Product data
        P-->>O: ProductResponse
        deactivate P

        O->>P: PUT /api/products/{id}/reduce-stock
        activate P
        Note right of P: span: stock-reduce
        P->>DB: UPDATE products SET stock = stock - ?
        Note right of DB: span: db-update
        DB-->>P: Updated
        P-->>O: Success
        deactivate P
    end

    O->>DB: INSERT INTO orders
    Note right of DB: span: order-insert
    DB-->>O: Order created

    O-->>G: OrderResponse
    deactivate O

    G-->>C: 201 Created
    deactivate G

    Note over G,DB: All spans reported to Zipkin
```

---

## 📋 Span Annotations

```mermaid
flowchart TD
    subgraph Annotations["🏷️ Span Annotations"]
        CS["cs (Client Send)<br/>Request started"]
        CR["cr (Client Receive)<br/>Response received"]
        SR["sr (Server Receive)<br/>Request received"]
        SS["ss (Server Send)<br/>Response sent"]
    end

    subgraph Timeline["⏱️ Timeline"]
        T1["Client ──cs──────────────────cr──"]
        T2["Server ────sr────────────ss────"]
    end

    CS --> T1
    CR --> T1
    SR --> T2
    SS --> T2
```

### Common Span Tags

| Tag | Description | Example |
|-----|-------------|---------|
| `http.method` | HTTP method | GET, POST |
| `http.url` | Request URL | /api/orders |
| `http.status_code` | Response code | 200, 404 |
| `mvc.controller.class` | Controller name | OrderController |
| `mvc.controller.method` | Method name | createOrder |
| `error` | Error flag | true |

---

## ⚙️ Docker Compose Setup

```yaml
# docker-compose.yaml
services:
  zipkin:
    image: openzipkin/zipkin
    container_name: zipkin
    ports:
      - 9411:9411
    networks:
      - loki
```

---

## 🔍 Trace Correlation with Logs

```mermaid
flowchart LR
    subgraph Application["📱 Application"]
        LOG["Log Entry<br/>traceId: abc123<br/>spanId: span456"]
        SPAN["Span<br/>traceId: abc123<br/>spanId: span456"]
    end

    subgraph Observability["📊 Observability Stack"]
        LOKI["Loki<br/>Log Storage"]
        ZIPKIN["Zipkin<br/>Trace Storage"]
        GRAFANA["Grafana<br/>Unified View"]
    end

    LOG -->|Ship| LOKI
    SPAN -->|Report| ZIPKIN
    LOKI --> GRAFANA
    ZIPKIN --> GRAFANA
```

### Log Format with Trace Context

```
2024-01-15T10:30:45.123Z INFO [order-service,abc123,span456] --- OrderController : Creating order
                               └─ service ─┘ └trace┘ └span┘
```

---

## 📊 Sampling Strategies

```mermaid
flowchart TD
    subgraph Strategies["🎯 Sampling Strategies"]
        ALL["probability: 1.0<br/>100% sampling<br/>Development/Debug"]
        PARTIAL["probability: 0.1<br/>10% sampling<br/>Production"]
        RATE["Rate-limited<br/>N traces/second<br/>High traffic"]
    end

    subgraph Config["⚙️ Configuration"]
        C1["management.tracing.sampling.probability: 1.0"]
        C2["management.tracing.sampling.probability: 0.1"]
        C3["Custom sampler bean"]
    end

    ALL --> C1
    PARTIAL --> C2
    RATE --> C3
```

### Current Configuration (All Services)

```yaml
management:
  tracing:
    sampling:
      probability: 1.0  # 100% - suitable for development
```

---

## 🌐 Service Dependencies Visualization

```mermaid
flowchart LR
    subgraph External["🌍 External"]
        CLIENT[Client]
    end

    subgraph Internal["🏢 Internal Services"]
        GW((Gateway))
        USER((User<br/>Service))
        PRODUCT((Product<br/>Service))
        ORDER((Order<br/>Service))
        NOTIFY((Notification<br/>Service))
    end

    CLIENT ==>|100 req/s| GW
    GW ==>|30 req/s| USER
    GW ==>|40 req/s| PRODUCT
    GW ==>|30 req/s| ORDER
    ORDER ==>|60 req/s| PRODUCT
    ORDER -.->|async| NOTIFY
    NOTIFY ==>|30 req/s| USER
```

---

## 🛠️ Debugging with Zipkin

### Finding Slow Requests

```mermaid
flowchart TD
    START[Start] --> SEARCH["Search traces<br/>minDuration > 500ms"]
    SEARCH --> SELECT["Select slow trace"]
    SELECT --> ANALYZE["View timeline"]
    ANALYZE --> IDENTIFY["Identify slow span"]
    IDENTIFY --> ROOT["Root cause:<br/>- Slow DB query?<br/>- Network latency?<br/>- Service processing?"]
```

### Finding Errors

```mermaid
flowchart TD
    START[Start] --> FILTER["Filter: error = true"]
    FILTER --> SELECT["Select failed trace"]
    SELECT --> DETAIL["View span details"]
    DETAIL --> TAGS["Check error tags:<br/>- error.message<br/>- error.stack"]
    TAGS --> FIX["Fix the issue"]
```

---

## 🚀 Quick Start

### 1. Start Zipkin Server

```bash
cd additional/evaluate-prometheus
docker-compose up -d zipkin
```

### 2. Start Application Services

```bash
# Start services in order
# Config Server → Eureka → Gateway → Services
```

### 3. Access Zipkin UI

Open http://localhost:9411

### 4. Find Traces

1. Select service name (e.g., `order-service`)
2. Click **Run Query**
3. Select a trace to view details

---

## 📈 Performance Considerations

```mermaid
flowchart LR
    subgraph Overhead["⚡ Tracing Overhead"]
        CPU["CPU: ~2-5%"]
        MEM["Memory: ~50MB"]
        NET["Network: Span reports"]
    end

    subgraph Mitigation["🛡️ Mitigation"]
        SAMPLE["Reduce sampling rate"]
        ASYNC["Async reporting"]
        BATCH["Batch span submission"]
    end

    CPU --> SAMPLE
    MEM --> ASYNC
    NET --> BATCH
```

---

## 🎯 Best Practices

### Do ✅

| Practice | Reason |
|----------|--------|
| Use 100% sampling in dev | Full visibility |
| Use 1-10% sampling in prod | Reduce overhead |
| Add custom span tags | Better debugging |
| Correlate with logs | Unified observability |

### Don't ❌

| Anti-pattern | Problem |
|--------------|---------|
| 100% sampling in prod | Performance impact |
| Ignore async boundaries | Missing context |
| No error tagging | Hard to debug |

---

## 📊 Quick Reference

| Endpoint | URL | Purpose |
|----------|-----|---------|
| **Zipkin UI** | http://localhost:9411 | Search and view traces |
| **API: Search** | http://localhost:9411/api/v2/traces | Query traces |
| **API: Services** | http://localhost:9411/api/v2/services | List services |
| **API: Dependencies** | http://localhost:9411/api/v2/dependencies | Service graph |

---

## 🎯 Summary

```mermaid
flowchart TD
    subgraph E2E["End-to-End Distributed Tracing"]
        REQUEST["📱 Client Request"]
        GATEWAY["🚪 Gateway<br/>Generate Trace ID"]
        SERVICES["⚙️ Services<br/>Create Child Spans"]
        PROPAGATE["🔗 Propagate<br/>B3 Headers"]
        REPORT["📤 Report<br/>Spans to Zipkin"]
        VISUALIZE["📊 Visualize<br/>in Zipkin UI"]
    end

    REQUEST --> GATEWAY
    GATEWAY --> SERVICES
    SERVICES --> PROPAGATE
    PROPAGATE --> SERVICES
    SERVICES --> REPORT
    REPORT --> VISUALIZE
```

| Feature | Implementation |
|---------|----------------|
| **Tracing Library** | Micrometer Tracing + Brave |
| **Span Reporter** | zipkin-reporter-brave |
| **Context Propagation** | B3 Headers (automatic) |
| **Sampling** | 100% (configurable) |
| **Visualization** | Zipkin UI @ :9411 |
| **Traced Services** | Gateway, User, Product, Order |
