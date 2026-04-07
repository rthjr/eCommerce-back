# Prometheus Monitoring Documentation

## 📊 Overview

This e-commerce microservices application uses **Prometheus** for metrics collection and **Grafana** for visualization, forming a complete observability stack along with **Loki** for log aggregation and **Zipkin** for distributed tracing.

---

## 🏗️ Monitoring Architecture

```mermaid
flowchart TB
    subgraph Services["⚙️ Microservices (Metrics Sources)"]
        USER[User Service<br/>:8082/actuator/prometheus]
        PRODUCT[Product Service<br/>:8081/actuator/prometheus]
        ORDER[Order Service<br/>:8083/actuator/prometheus]
        PAYMENT[Payment Gateway<br/>:8976/metrics]
    end

    subgraph Monitoring["📊 Monitoring Stack"]
        PROM[Prometheus<br/>Port: 3987<br/>Internal: 9090]
        GRAFANA[Grafana<br/>Port: 3000]
        LOKI[Loki<br/>Port: 3100]
        ALLOY[Grafana Alloy<br/>Port: 12345]
        ZIPKIN[Zipkin<br/>Port: 9411]
    end

    subgraph Storage["💾 Storage"]
        MINIO[MinIO<br/>S3-Compatible Storage]
    end

    PROM -->|Scrape every 3s| USER
    PROM -->|Scrape every 3s| PRODUCT
    PROM -->|Scrape every 3s| ORDER
    PROM -->|Scrape every 3s| PAYMENT

    GRAFANA -->|Query Metrics| PROM
    GRAFANA -->|Query Logs| LOKI
    
    ALLOY -->|Ship Logs| LOKI
    LOKI -->|Store| MINIO
    
    USER -.->|Traces| ZIPKIN
    PRODUCT -.->|Traces| ZIPKIN
    ORDER -.->|Traces| ZIPKIN
```

---

## 🔄 Prometheus Metrics Collection Flow

```mermaid
sequenceDiagram
    participant PROM as Prometheus<br/>:9090
    participant USER as User Service<br/>:8082
    participant PRODUCT as Product Service<br/>:8081
    participant ORDER as Order Service<br/>:8083
    participant PAYMENT as Payment Gateway<br/>:8976

    Note over PROM: Scrape Interval: 3 seconds
    
    loop Every 3 seconds
        PROM->>USER: GET /actuator/prometheus
        USER-->>PROM: JVM, HTTP, Custom Metrics
        
        PROM->>PRODUCT: GET /actuator/prometheus
        PRODUCT-->>PROM: JVM, HTTP, Custom Metrics
        
        PROM->>ORDER: GET /actuator/prometheus
        ORDER-->>PROM: JVM, HTTP, Custom Metrics
        
        PROM->>PAYMENT: GET /metrics
        PAYMENT-->>PROM: FastAPI Metrics
    end
    
    Note over PROM: Store in Time-Series DB
```

---

## 📦 Technology Stack

| Component | Technology | Port | Purpose |
|-----------|------------|------|---------|
| **Prometheus** | prom/prometheus:v2.44.0 | 3987 (→9090) | Metrics collection & storage |
| **Grafana** | grafana/grafana:latest | 3000 | Visualization dashboards |
| **Loki** | grafana/loki:latest | 3100 | Log aggregation |
| **Alloy** | grafana/alloy:latest | 12345 | Log shipping agent |
| **Zipkin** | openzipkin/zipkin | 9411 | Distributed tracing |
| **MinIO** | minio/minio | 9000 | S3 storage for Loki |

---

## ⚙️ Service Instrumentation

### Spring Boot Services (Java)

All Spring Boot services use **Micrometer** with Prometheus registry:

```mermaid
flowchart LR
    subgraph SpringBoot["Spring Boot Service"]
        APP[Application Code]
        MICRO[Micrometer<br/>micrometer-registry-prometheus]
        ACTUATOR[Spring Actuator<br/>/actuator/prometheus]
    end
    
    subgraph Metrics["Exposed Metrics"]
        JVM[JVM Metrics<br/>Memory, GC, Threads]
        HTTP[HTTP Metrics<br/>Request count, latency]
        CUSTOM[Custom Metrics<br/>Business metrics]
    end
    
    APP --> MICRO
    MICRO --> ACTUATOR
    ACTUATOR --> JVM
    ACTUATOR --> HTTP
    ACTUATOR --> CUSTOM
```

**Dependencies (pom.xml):**
```xml
<!-- Prometheus metrics registry -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Distributed tracing -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
```

**Configuration (application.yml):**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"    # Expose all actuator endpoints
  tracing:
    sampling:
      probability: 1.0  # 100% trace sampling
```

### Payment Gateway (Python/FastAPI)

Uses **prometheus-fastapi-instrumentator** for automatic metrics:

```mermaid
flowchart LR
    subgraph FastAPI["FastAPI Service"]
        APP[app.py]
        INST[Instrumentator]
        ENDPOINT[/metrics endpoint]
    end
    
    subgraph AutoMetrics["Auto-Generated Metrics"]
        REQ[http_requests_total]
        LAT[http_request_duration_seconds]
        SIZE[http_request_size_bytes]
        RESP[http_response_size_bytes]
    end
    
    APP --> INST
    INST --> ENDPOINT
    ENDPOINT --> REQ
    ENDPOINT --> LAT
    ENDPOINT --> SIZE
    ENDPOINT --> RESP
```

**Code (app.py):**
```python
from prometheus_fastapi_instrumentator import Instrumentator

app = FastAPI()

# Auto-instrument and expose /metrics endpoint
Instrumentator().instrument(app).expose(app)
```

---

## 📋 Prometheus Configuration

```yaml
# prometheus/prometheus.yml
scrape_configs:
  - job_name: 'product-service'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 3s
    static_configs:
      - targets: ['host.docker.internal:8081']
        labels:
          application: 'product-service'

  - job_name: 'user-service'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 3s
    static_configs:
      - targets: ['host.docker.internal:8082']
        labels:
          application: 'user-service'

  - job_name: 'order-service'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 3s
    static_configs:
      - targets: ['host.docker.internal:8083']
        labels:
          application: 'order-service'

  - job_name: 'payment-service'
    metrics_path: '/metrics'
    scrape_interval: 3s
    static_configs:
      - targets: ['host.docker.internal:8976']
        labels:
          application: 'payment-service'
```

---

## 📈 Metrics Exposed by Services

### Spring Boot Services (via Micrometer)

```mermaid
mindmap
  root((Prometheus<br/>Metrics))
    JVM Metrics
      jvm_memory_used_bytes
      jvm_memory_max_bytes
      jvm_gc_pause_seconds
      jvm_threads_live_threads
      jvm_classes_loaded_classes
    HTTP Metrics
      http_server_requests_seconds_count
      http_server_requests_seconds_sum
      http_server_requests_seconds_max
    System Metrics
      system_cpu_usage
      process_uptime_seconds
      process_cpu_usage
    Hikari Pool
      hikaricp_connections_active
      hikaricp_connections_pending
      hikaricp_connections_idle
    Custom Business
      orders_created_total
      payments_processed_total
```

### FastAPI Payment Service

| Metric | Type | Description |
|--------|------|-------------|
| `http_requests_total` | Counter | Total HTTP requests |
| `http_request_duration_seconds` | Histogram | Request latency |
| `http_request_size_bytes` | Summary | Request body size |
| `http_response_size_bytes` | Summary | Response body size |
| `http_requests_in_progress` | Gauge | Current active requests |

---

## 🔍 Complete Observability Flow

```mermaid
flowchart TD
    subgraph Application["📱 Application Layer"]
        USER[User Service]
        PRODUCT[Product Service]
        ORDER[Order Service]
        PAYMENT[Payment Gateway]
    end

    subgraph Collection["📥 Data Collection"]
        direction TB
        PROM_SCRAPE[Prometheus Scraper]
        ALLOY_SHIP[Grafana Alloy<br/>Log Shipper]
        BRAVE[Brave Tracer<br/>Span Reporter]
    end

    subgraph Storage["💾 Storage Layer"]
        PROM_DB[(Prometheus TSDB)]
        LOKI_STORE[(Loki + MinIO)]
        ZIPKIN_STORE[(Zipkin Storage)]
    end

    subgraph Visualization["📊 Visualization"]
        GRAFANA[Grafana Dashboards]
        ZIPKIN_UI[Zipkin UI]
    end

    USER -->|Metrics| PROM_SCRAPE
    PRODUCT -->|Metrics| PROM_SCRAPE
    ORDER -->|Metrics| PROM_SCRAPE
    PAYMENT -->|Metrics| PROM_SCRAPE

    USER -->|Logs| ALLOY_SHIP
    PRODUCT -->|Logs| ALLOY_SHIP
    ORDER -->|Logs| ALLOY_SHIP
    PAYMENT -->|Logs| ALLOY_SHIP

    USER -->|Traces| BRAVE
    PRODUCT -->|Traces| BRAVE
    ORDER -->|Traces| BRAVE

    PROM_SCRAPE --> PROM_DB
    ALLOY_SHIP --> LOKI_STORE
    BRAVE --> ZIPKIN_STORE

    PROM_DB --> GRAFANA
    LOKI_STORE --> GRAFANA
    ZIPKIN_STORE --> ZIPKIN_UI
```

---

## 🚀 Starting the Monitoring Stack

```mermaid
sequenceDiagram
    participant DEV as Developer
    participant DOCKER as Docker Compose
    participant MINIO as MinIO
    participant LOKI as Loki (Read/Write)
    participant GW as Nginx Gateway
    participant PROM as Prometheus
    participant GRAFANA as Grafana
    participant ALLOY as Alloy

    DEV->>DOCKER: docker-compose up -d
    
    Note over DOCKER: Start infrastructure first
    DOCKER->>MINIO: Start S3 storage
    MINIO-->>DOCKER: Ready on :9000
    
    DOCKER->>LOKI: Start Loki Read node
    DOCKER->>LOKI: Start Loki Write node
    LOKI-->>DOCKER: Ready on :3101, :3102
    
    DOCKER->>GW: Start Nginx gateway
    GW-->>DOCKER: Ready on :3100
    
    Note over DOCKER: Start observability tools
    DOCKER->>PROM: Start Prometheus
    PROM-->>DOCKER: Ready on :3987
    
    DOCKER->>GRAFANA: Start Grafana
    GRAFANA->>PROM: Configure datasource
    GRAFANA->>LOKI: Configure datasource
    GRAFANA-->>DOCKER: Ready on :3000
    
    DOCKER->>ALLOY: Start log shipper
    ALLOY-->>DOCKER: Ready on :12345
    
    Note over DEV: Access Grafana at http://localhost:3000
```

**Command:**
```bash
cd additional/evaluate-prometheus
docker-compose up -d
```

---

## 🎛️ Grafana Data Sources

```yaml
# grafana/datasources/datasources.yml
apiVersion: 1
datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true

  - name: Loki
    type: loki
    access: proxy
    url: http://gateway:3100
    jsonData:
      httpHeaderName1: "X-Scope-OrgID"
    secureJsonData:
      httpHeaderValue1: "tenant1"
```

---

## 📂 Log Collection Architecture

```mermaid
flowchart TD
    subgraph Sources["📁 Log Sources"]
        SPRING_LOGS[Spring Boot Logs<br/>/logs/*.log<br/>Text Format]
        PYTHON_LOGS[Payment Service Logs<br/>/logs/payment/*.log<br/>JSON Format]
        DOCKER_LOGS[Docker Container Logs]
    end

    subgraph Alloy["🔄 Grafana Alloy Processing"]
        SPRING_MATCH[file_match: parent_app_logs]
        PYTHON_MATCH[file_match: payment_logs]
        DOCKER_DISC[discovery.docker]
        
        SPRING_PARSE[parse_spring<br/>Multiline + Regex]
        PYTHON_PARSE[parse_python<br/>JSON Parser]
    end

    subgraph Loki["📊 Loki Cluster"]
        GATEWAY[Nginx Gateway<br/>:3100]
        WRITE[Loki Write Node<br/>:3102]
        READ[Loki Read Node<br/>:3101]
        BACKEND[Loki Backend]
    end

    subgraph Storage["💾 Storage"]
        MINIO[(MinIO S3)]
    end

    SPRING_LOGS --> SPRING_MATCH
    PYTHON_LOGS --> PYTHON_MATCH
    DOCKER_LOGS --> DOCKER_DISC

    SPRING_MATCH --> SPRING_PARSE
    PYTHON_MATCH --> PYTHON_PARSE
    DOCKER_DISC --> GATEWAY

    SPRING_PARSE --> GATEWAY
    PYTHON_PARSE --> GATEWAY

    GATEWAY --> WRITE
    WRITE --> MINIO
    READ --> MINIO
    BACKEND --> MINIO
```

---

## 🔗 Distributed Tracing Flow

```mermaid
sequenceDiagram
    participant CLIENT as Client
    participant GW as API Gateway
    participant ORDER as Order Service
    participant PRODUCT as Product Service
    participant ZIPKIN as Zipkin

    Note over CLIENT,ZIPKIN: Trace ID propagated across services
    
    CLIENT->>GW: POST /api/orders
    Note right of GW: Generate Trace ID: abc123
    
    GW->>ORDER: Forward request<br/>X-B3-TraceId: abc123
    Note right of ORDER: Span ID: span1
    
    ORDER->>PRODUCT: GET /api/products/{id}<br/>X-B3-TraceId: abc123
    Note right of PRODUCT: Span ID: span2
    
    PRODUCT-->>ORDER: Product details
    ORDER-->>GW: Order created
    GW-->>CLIENT: 201 Created

    ORDER->>ZIPKIN: Report span1
    PRODUCT->>ZIPKIN: Report span2
    
    Note over ZIPKIN: Correlate spans by Trace ID
```

**Configuration:**
```yaml
management:
  tracing:
    sampling:
      probability: 1.0  # 100% of requests traced
```

---

## 📊 Key PromQL Queries for Grafana

### Request Rate
```promql
rate(http_server_requests_seconds_count{application="order-service"}[5m])
```

### Average Response Time
```promql
rate(http_server_requests_seconds_sum{application="product-service"}[5m]) 
/ rate(http_server_requests_seconds_count{application="product-service"}[5m])
```

### Error Rate
```promql
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) 
/ sum(rate(http_server_requests_seconds_count[5m])) * 100
```

### JVM Memory Usage
```promql
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} * 100
```

### Active Database Connections
```promql
hikaricp_connections_active{application="order-service"}
```

---

## 🎯 Summary

```mermaid
flowchart LR
    subgraph Pillars["Three Pillars of Observability"]
        METRICS[📊 Metrics<br/>Prometheus]
        LOGS[📝 Logs<br/>Loki]
        TRACES[🔗 Traces<br/>Zipkin]
    end

    subgraph Tools["Tooling"]
        GRAFANA[Grafana<br/>Unified Dashboard]
    end

    METRICS --> GRAFANA
    LOGS --> GRAFANA
    TRACES --> |Zipkin UI| GRAFANA
```

| Pillar | Tool | Data Source | Endpoint |
|--------|------|-------------|----------|
| **Metrics** | Prometheus | Micrometer / FastAPI Instrumentator | `/actuator/prometheus` or `/metrics` |
| **Logs** | Loki + Alloy | Log files | File-based collection |
| **Traces** | Zipkin | Brave Tracer | Span reporting |
| **Visualization** | Grafana | All above | http://localhost:3000 |

---

## 🔧 Quick Reference

| Service | Metrics URL |
|---------|-------------|
| User Service | http://localhost:8082/actuator/prometheus |
| Product Service | http://localhost:8081/actuator/prometheus |
| Order Service | http://localhost:8083/actuator/prometheus |
| Payment Gateway | http://localhost:8976/metrics |
| Prometheus UI | http://localhost:3987 |
| Grafana | http://localhost:3000 |
| Zipkin | http://localhost:9411 |
