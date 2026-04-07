# Grafana Loki Log Aggregation Documentation

## 📋 Overview

This e-commerce microservices application uses **Grafana Loki** for centralized log aggregation, with **Grafana Alloy** as the log shipping agent. Loki is deployed in a scalable **read/write/backend** microservices mode with **MinIO** as S3-compatible object storage.

---

## 🏗️ Loki Architecture Overview

```mermaid
flowchart TB
    subgraph Applications["📱 Application Log Sources"]
        USER[User Service<br/>logs/user-service.log]
        PRODUCT[Product Service<br/>logs/product-service.log]
        ORDER[Order Service<br/>logs/order-service.log]
        PAYMENT[Payment Gateway<br/>logs/payment-service.log<br/>JSON Format]
        DOCKER[Docker Containers<br/>Container Logs]
    end

    subgraph Alloy["🔄 Grafana Alloy (Log Shipper)"]
        SPRING_PARSER[Spring Boot Parser<br/>Regex + Multiline]
        PYTHON_PARSER[Python JSON Parser]
        DOCKER_SCRAPER[Docker Discovery]
    end

    subgraph LokiCluster["📊 Loki Cluster (Microservices Mode)"]
        GATEWAY[Nginx Gateway<br/>:3100]
        WRITE[Loki Write Node<br/>:3102]
        READ[Loki Read Node<br/>:3101]
        BACKEND[Loki Backend<br/>Compactor]
    end

    subgraph Storage["💾 Object Storage"]
        MINIO[(MinIO S3<br/>:9000)]
    end

    subgraph Visualization["📊 Visualization"]
        GRAFANA[Grafana<br/>:3000]
    end

    USER --> SPRING_PARSER
    PRODUCT --> SPRING_PARSER
    ORDER --> SPRING_PARSER
    PAYMENT --> PYTHON_PARSER
    DOCKER --> DOCKER_SCRAPER

    SPRING_PARSER --> GATEWAY
    PYTHON_PARSER --> GATEWAY
    DOCKER_SCRAPER --> GATEWAY

    GATEWAY -->|Push| WRITE
    GATEWAY -->|Query| READ
    
    WRITE --> MINIO
    READ --> MINIO
    BACKEND --> MINIO

    GRAFANA -->|LogQL Queries| GATEWAY
```

---

## 🔄 Complete Log Flow Process

```mermaid
sequenceDiagram
    participant APP as Application
    participant FILE as Log File
    participant ALLOY as Grafana Alloy
    participant GW as Nginx Gateway
    participant WRITE as Loki Write
    participant MINIO as MinIO S3
    participant READ as Loki Read
    participant GRAFANA as Grafana

    Note over APP,FILE: Step 1: Application generates logs
    APP->>FILE: Write log entry
    
    Note over FILE,ALLOY: Step 2: Alloy tails log files
    ALLOY->>FILE: Tail (every 5s sync)
    FILE-->>ALLOY: New log lines
    
    Note over ALLOY: Step 3: Parse & Label
    ALLOY->>ALLOY: Apply regex/JSON parsing
    ALLOY->>ALLOY: Extract labels (level, logger, thread)
    
    Note over ALLOY,GW: Step 4: Push to Loki
    ALLOY->>GW: POST /loki/api/v1/push<br/>tenant_id: tenant1
    GW->>WRITE: Forward to write node
    
    Note over WRITE,MINIO: Step 5: Store in S3
    WRITE->>MINIO: Store chunks in loki-data bucket
    WRITE->>MINIO: Store index in index_ prefix
    
    Note over GRAFANA,READ: Step 6: Query logs
    GRAFANA->>GW: LogQL query
    GW->>READ: Forward to read node
    READ->>MINIO: Fetch chunks
    MINIO-->>READ: Log data
    READ-->>GW: Query results
    GW-->>GRAFANA: Display logs
```

---

## 📦 Component Details

### Service Ports & Roles

| Service | Port | Role | Description |
|---------|------|------|-------------|
| **Nginx Gateway** | 3100 | Load Balancer | Routes push/query to correct nodes |
| **Loki Write** | 3102 | Ingester | Receives and batches log entries |
| **Loki Read** | 3101 | Querier | Executes LogQL queries |
| **Loki Backend** | 3100 | Compactor | Compacts and indexes chunks |
| **MinIO** | 9000 | Storage | S3-compatible object storage |
| **Alloy** | 12345 | Agent | Log collection and shipping |
| **Grafana** | 3000 | UI | Visualization and querying |

---

## 🔧 Grafana Alloy Configuration

### Log Sources Pipeline

```mermaid
flowchart TD
    subgraph Sources["📁 Log File Sources"]
        SPRING["/logs-parent/*.log<br/>Spring Boot Services"]
        PYTHON["/logs/payment/*.log<br/>Payment Service"]
        SYSTEM["/var/log/*.log<br/>System Logs"]
        DOCKER["Docker Socket<br/>Container Logs"]
    end

    subgraph FileMatch["🔍 File Discovery"]
        SPRING_MATCH["local.file_match<br/>parent_app_logs"]
        PYTHON_MATCH["local.file_match<br/>payment_logs"]
        SYSTEM_MATCH["local.file_match<br/>system_logs"]
        DOCKER_DISC["discovery.docker<br/>flog_scrape"]
    end

    subgraph FileScrape["📖 File Readers"]
        SPRING_SCRAPE["loki.source.file<br/>parent_app_file_scrape"]
        PYTHON_SCRAPE["loki.source.file<br/>payment_file_scrape"]
        SYSTEM_SCRAPE["loki.source.file<br/>system_file_scrape"]
        DOCKER_SCRAPE["loki.source.docker<br/>flog_scrape"]
    end

    subgraph Processing["⚙️ Log Processing"]
        SPRING_PARSE["loki.process<br/>parse_spring"]
        PYTHON_PARSE["loki.process<br/>parse_python"]
    end

    subgraph Output["📤 Output"]
        LOKI_WRITE["loki.write<br/>default"]
    end

    SPRING --> SPRING_MATCH
    PYTHON --> PYTHON_MATCH
    SYSTEM --> SYSTEM_MATCH
    DOCKER --> DOCKER_DISC

    SPRING_MATCH --> SPRING_SCRAPE
    PYTHON_MATCH --> PYTHON_SCRAPE
    SYSTEM_MATCH --> SYSTEM_SCRAPE
    DOCKER_DISC --> DOCKER_SCRAPE

    SPRING_SCRAPE --> SPRING_PARSE
    PYTHON_SCRAPE --> PYTHON_PARSE
    SYSTEM_SCRAPE --> LOKI_WRITE
    DOCKER_SCRAPE --> LOKI_WRITE

    SPRING_PARSE --> LOKI_WRITE
    PYTHON_PARSE --> LOKI_WRITE
```

---

## 📝 Spring Boot Log Parsing

### Log Format Example
```
2024-01-15T10:30:45.123Z INFO 1 --- [order-service] [http-nio-8083-exec-1] c.e.order.controller.OrderController : Creating order for user: user123
```

### Parsing Pipeline

```mermaid
flowchart TD
    subgraph Input["📥 Raw Log Line"]
        RAW["2024-01-15T10:30:45.123Z INFO 1 --- [order-service] [http-nio-8083-exec-1] c.e.order.controller.OrderController : Creating order"]
    end

    subgraph Multiline["🔗 Multiline Stage"]
        MULTI["Merge stack traces<br/>firstline: ^\\d{4}-\\d{2}-\\d{2}T<br/>max_wait_time: 3s"]
    end

    subgraph Regex["🔍 Regex Stage"]
        EXTRACT["Extract named groups:<br/>• timestamp: 2024-01-15T10:30:45.123Z<br/>• level: INFO<br/>• app: order-service<br/>• thread: http-nio-8083-exec-1<br/>• logger: c.e.order.controller.OrderController"]
    end

    subgraph Labels["🏷️ Labels Stage"]
        LABEL["Add Loki labels:<br/>• level = INFO<br/>• logger = c.e.order.controller.OrderController<br/>• thread = http-nio-8083-exec-1"]
    end

    subgraph Output["📤 To Loki"]
        PUSH["Push with labels"]
    end

    RAW --> MULTI
    MULTI --> REGEX
    REGEX --> EXTRACT
    EXTRACT --> Labels
    Labels --> LABEL
    LABEL --> Output
```

### Alloy Configuration for Spring Boot
```yaml
loki.process "parse_spring" {
    // Merge multi-line stack traces
    stage.multiline {
        firstline     = "^\\d{4}-\\d{2}-\\d{2}T"
        max_wait_time = "3s"
    }

    stage.regex {
        expression = "^(?P<timestamp>\\S+)\\s+(?P<level>\\w+)\\s+\\d+\\s+---\\s+\\[(?P<app>[^\\]]+)\\]\\s+\\[(?P<thread>[^\\]]+)\\].*?(?P<logger>[\\w\\.\\$]+)\\s*:"
    }

    stage.labels {
        values = {
            level  = "level",
            logger = "logger",
            thread = "thread",
        }
    }

    forward_to = [loki.write.default.receiver]
}
```

---

## 🐍 Python (FastAPI) Log Parsing

### Log Format (JSON via python-json-logger)
```json
{"asctime": "2024-01-15T10:30:45", "levelname": "INFO", "name": "payment-service", "message": "Processing payment for order ORD-123"}
```

### Parsing Pipeline

```mermaid
flowchart TD
    subgraph Input["📥 JSON Log Line"]
        RAW["{\"asctime\": \"...\", \"levelname\": \"INFO\", \"name\": \"payment-service\", \"message\": \"...\"}"]
    end

    subgraph JSONStage["🔍 JSON Stage"]
        EXTRACT["Extract fields:<br/>• level = levelname<br/>• logger = name<br/>• message = message"]
    end

    subgraph Labels["🏷️ Labels Stage"]
        LABEL["Add Loki labels:<br/>• level = INFO<br/>• logger = payment-service<br/>• service = payment-service<br/>• env = local"]
    end

    subgraph Output["📤 To Loki"]
        PUSH["Push with labels"]
    end

    RAW --> JSONStage
    JSONStage --> EXTRACT
    EXTRACT --> Labels
    Labels --> LABEL
    LABEL --> Output
```

### Python Logging Configuration
```python
# logging_config.py
from pythonjsonlogger import jsonlogger

formatter = jsonlogger.JsonFormatter(
    fmt="%(asctime)s %(levelname)s %(name)s %(message)s",
    datefmt="%Y-%m-%dT%H:%M:%S",
)

# File handler — Alloy tails this file
file_handler = logging.FileHandler("logs/payment-service.log")
file_handler.setFormatter(formatter)
```

---

## 🐋 Docker Container Log Collection

```mermaid
flowchart TD
    subgraph Containers["🐋 Docker Containers"]
        FLOG["flog<br/>Fake log generator"]
        OTHER["Other containers"]
    end

    subgraph Discovery["🔍 Docker Discovery"]
        SOCKET["Docker Socket<br/>/var/run/docker.sock"]
        DISCOVER["discovery.docker<br/>refresh: 5s"]
    end

    subgraph Relabel["🏷️ Relabel Rules"]
        RULE["Extract container name<br/>__meta_docker_container_name → container"]
    end

    subgraph Source["📖 Docker Log Source"]
        SCRAPE["loki.source.docker<br/>Forward to Loki"]
    end

    Containers --> SOCKET
    SOCKET --> DISCOVER
    DISCOVER --> Relabel
    Relabel --> RULE
    RULE --> Source
    Source --> LOKI["loki.write.default"]
```

---

## 🏢 Loki Microservices Architecture

```mermaid
flowchart TB
    subgraph Ingress["🚪 Ingress Layer"]
        NGINX["Nginx Gateway<br/>:3100<br/>Load Balancer"]
    end

    subgraph WriteLayer["✍️ Write Path"]
        WRITE["Loki Write Node<br/>-target=write<br/>:3102"]
    end

    subgraph ReadLayer["📖 Read Path"]
        READ["Loki Read Node<br/>-target=read<br/>:3101"]
    end

    subgraph BackendLayer["⚙️ Backend"]
        BACKEND["Loki Backend<br/>-target=backend<br/>Compactor + Ruler"]
    end

    subgraph Memberlist["🔗 Memberlist Cluster"]
        GOSSIP["Gossip Protocol<br/>Port: 7946<br/>Members: read, write, backend"]
    end

    subgraph Storage["💾 S3 Storage (MinIO)"]
        CHUNKS["loki-data bucket<br/>Log chunks"]
        RULES["loki-ruler bucket<br/>Alert rules"]
    end

    NGINX -->|/loki/api/v1/push| WRITE
    NGINX -->|/loki/api/v1/query*| READ
    
    WRITE --> CHUNKS
    READ --> CHUNKS
    BACKEND --> CHUNKS
    BACKEND --> RULES

    WRITE <-.->|gossip| GOSSIP
    READ <-.->|gossip| GOSSIP
    BACKEND <-.->|gossip| GOSSIP
```

---

## ⚙️ Loki Configuration Details

### Schema & Storage Configuration
```yaml
# loki-config.yaml
schema_config:
  configs:
    - from: 2023-01-01
      store: tsdb          # Time-series database index
      object_store: s3     # MinIO S3 storage
      schema: v13          # Latest schema version
      index:
        prefix: index_
        period: 24h        # Daily index rotation

common:
  storage:
    s3:
      endpoint: minio:9000
      insecure: true
      bucketnames: loki-data
      access_key_id: loki
      secret_access_key: supersecret
      s3forcepathstyle: true
```

### Memberlist Cluster Configuration
```yaml
memberlist:
  join_members: ["read", "write", "backend"]
  dead_node_reclaim_time: 30s
  gossip_to_dead_nodes_time: 15s
  left_ingesters_timeout: 30s
  bind_addr: ['0.0.0.0']
  bind_port: 7946
  gossip_interval: 2s
```

---

## 🔀 Nginx Gateway Routing

```mermaid
flowchart LR
    subgraph Clients["📱 Clients"]
        ALLOY["Alloy Agent"]
        GRAFANA["Grafana"]
    end

    subgraph Gateway["🚪 Nginx Gateway :3100"]
        PUSH_ROUTE["/loki/api/v1/push"]
        QUERY_ROUTE["/loki/api/v1/*"]
        TAIL_ROUTE["/loki/api/v1/tail"]
    end

    subgraph Loki["📊 Loki Nodes"]
        WRITE["Write Node :3102"]
        READ["Read Node :3101"]
    end

    ALLOY -->|Push logs| PUSH_ROUTE
    GRAFANA -->|Query logs| QUERY_ROUTE
    GRAFANA -->|Live tail| TAIL_ROUTE

    PUSH_ROUTE -->|proxy_pass| WRITE
    QUERY_ROUTE -->|proxy_pass| READ
    TAIL_ROUTE -->|WebSocket upgrade| READ
```

### Nginx Configuration
```nginx
server {
    listen 3100;

    # Push endpoint → Write node
    location = /loki/api/v1/push {
        proxy_pass http://write:3100$request_uri;
    }

    # Live tail → Read node (WebSocket)
    location = /loki/api/v1/tail {
        proxy_pass http://read:3100$request_uri;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # All other queries → Read node
    location ~ /loki/api/.* {
        proxy_pass http://read:3100$request_uri;
    }
}
```

---

## 📊 Grafana Integration

### Data Source Configuration
```yaml
# grafana/datasources/datasources.yml
apiVersion: 1
datasources:
  - name: Loki
    type: loki
    access: proxy
    url: http://gateway:3100
    jsonData:
      httpHeaderName1: "X-Scope-OrgID"
    secureJsonData:
      httpHeaderValue1: "tenant1"
```

### Multi-Tenancy

```mermaid
flowchart LR
    subgraph Tenants["👥 Tenants"]
        T1["tenant1"]
        T2["tenant2"]
    end

    subgraph Headers["📨 HTTP Headers"]
        H1["X-Scope-OrgID: tenant1"]
        H2["X-Scope-OrgID: tenant2"]
    end

    subgraph Storage["💾 Isolated Storage"]
        S1["tenant1/chunks/"]
        S2["tenant2/chunks/"]
    end

    T1 --> H1
    T2 --> H2
    H1 --> S1
    H2 --> S2
```

---

## 🔍 LogQL Query Examples

### Basic Queries
```logql
# All logs from order-service
{service="order-service"}

# Error logs only
{level="ERROR"}

# Specific logger
{logger="c.e.order.controller.OrderController"}

# Combined filters
{service="payment-service", level=~"ERROR|WARN"}
```

### Pattern Matching
```logql
# Search for specific text
{service="order-service"} |= "Creating order"

# Regex pattern
{service="user-service"} |~ "user.*login"

# Exclude pattern
{level="ERROR"} != "timeout"
```

### Aggregations
```logql
# Error count by service (last 5m)
sum by (service) (count_over_time({level="ERROR"}[5m]))

# Log volume rate
sum(rate({service=~".+"}[1m])) by (service)
```

---

## 🚀 Startup Sequence

```mermaid
sequenceDiagram
    participant DOCKER as Docker Compose
    participant MINIO as MinIO
    participant WRITE as Loki Write
    participant READ as Loki Read
    participant GW as Nginx Gateway
    participant BACKEND as Loki Backend
    participant ALLOY as Alloy
    participant GRAFANA as Grafana

    Note over DOCKER: Start storage first
    DOCKER->>MINIO: Start MinIO
    MINIO->>MINIO: Create buckets<br/>loki-data, loki-ruler
    MINIO-->>DOCKER: Ready on :9000

    Note over DOCKER: Start Loki nodes
    DOCKER->>WRITE: Start write node
    WRITE->>MINIO: Connect to S3
    WRITE-->>DOCKER: Ready on :3102

    DOCKER->>READ: Start read node
    READ->>MINIO: Connect to S3
    READ-->>DOCKER: Ready on :3101

    Note over DOCKER: Start gateway
    DOCKER->>GW: Start Nginx
    GW->>WRITE: Health check
    GW->>READ: Health check
    GW-->>DOCKER: Ready on :3100

    Note over DOCKER: Start backend & agents
    DOCKER->>BACKEND: Start backend (compactor)
    BACKEND-->>DOCKER: Ready

    DOCKER->>ALLOY: Start log agent
    ALLOY->>GW: Test connection
    ALLOY-->>DOCKER: Ready on :12345

    DOCKER->>GRAFANA: Start Grafana
    GRAFANA->>GW: Configure Loki datasource
    GRAFANA-->>DOCKER: Ready on :3000

    Note over DOCKER,GRAFANA: ✅ Stack ready
```

---

## 📁 Volume Mappings

```mermaid
flowchart LR
    subgraph Host["🖥️ Host Machine"]
        LOGS_PARENT["../../logs/"]
        PAYMENT_LOGS["../../payment-gateway/logs/"]
        DOCKER_SOCK["/var/run/docker.sock"]
        MINIO_DATA["./.data/minio/"]
    end

    subgraph Alloy["🔄 Alloy Container"]
        A_PARENT["/logs-parent/:ro"]
        A_PAYMENT["/logs/payment/:ro"]
        A_DOCKER["/var/run/docker.sock"]
    end

    subgraph MinIO["💾 MinIO Container"]
        M_DATA["/data/"]
    end

    LOGS_PARENT --> A_PARENT
    PAYMENT_LOGS --> A_PAYMENT
    DOCKER_SOCK --> A_DOCKER
    MINIO_DATA --> M_DATA
```

---

## 🛠️ Docker Compose Services Summary

```mermaid
flowchart TB
    subgraph Network["🌐 loki network"]
        subgraph Storage["Storage Layer"]
            MINIO["minio<br/>S3 Storage"]
        end

        subgraph Loki["Loki Cluster"]
            WRITE["write<br/>Ingester"]
            READ["read<br/>Querier"]
            BACKEND["backend<br/>Compactor"]
            GW["gateway<br/>Nginx LB"]
        end

        subgraph Agents["Collection Layer"]
            ALLOY["alloy<br/>Log Shipper"]
            FLOG["flog<br/>Test Logs"]
        end

        subgraph Visualization["Visualization"]
            GRAFANA["grafana<br/>Dashboards"]
            PROM["prometheus<br/>Metrics"]
        end

        subgraph Tracing["Tracing"]
            ZIPKIN["zipkin<br/>Traces"]
        end
    end

    ALLOY --> GW
    FLOG --> GW
    GW --> WRITE
    GW --> READ
    WRITE --> MINIO
    READ --> MINIO
    BACKEND --> MINIO
    GRAFANA --> GW
    GRAFANA --> PROM
```

---

## 🎯 Best Practices

### Label Cardinality
```mermaid
flowchart LR
    subgraph Good["✅ Good Labels"]
        G1["level (DEBUG/INFO/WARN/ERROR)"]
        G2["service (user/product/order)"]
        G3["env (dev/staging/prod)"]
    end

    subgraph Bad["❌ Bad Labels (High Cardinality)"]
        B1["user_id"]
        B2["request_id"]
        B3["timestamp"]
    end
```

### Log Retention Strategy
| Environment | Retention | Chunk Compaction |
|-------------|-----------|------------------|
| Development | 7 days | Daily |
| Staging | 14 days | Daily |
| Production | 30 days | Daily |

---

## 📊 Quick Reference

| Component | URL | Purpose |
|-----------|-----|---------|
| **Grafana** | http://localhost:3000 | Log visualization |
| **Loki Gateway** | http://localhost:3100 | API endpoint |
| **Loki Read** | http://localhost:3101 | Query node |
| **Loki Write** | http://localhost:3102 | Ingest node |
| **Alloy UI** | http://localhost:12345 | Agent status |
| **MinIO Console** | http://localhost:9000 | Storage UI |

### Start the Stack
```bash
cd additional/evaluate-prometheus
docker-compose up -d
```

### View Logs in Grafana
1. Open http://localhost:3000
2. Go to **Explore** → Select **Loki** datasource
3. Run LogQL query: `{service=~".+"}`

---

## 🎯 Summary

```mermaid
flowchart TD
    subgraph E2E["End-to-End Log Flow"]
        APP["📱 Application<br/>Generate Logs"] 
        FILE["📁 Log File<br/>Text/JSON"]
        ALLOY["🔄 Alloy<br/>Parse & Ship"]
        LOKI["📊 Loki<br/>Index & Store"]
        GRAFANA["📈 Grafana<br/>Query & Visualize"]
    end

    APP -->|Write| FILE
    FILE -->|Tail| ALLOY
    ALLOY -->|Push| LOKI
    LOKI -->|Query| GRAFANA
```

| Feature | Implementation |
|---------|----------------|
| **Log Collection** | Grafana Alloy (file tailing + Docker) |
| **Log Parsing** | Regex (Spring Boot) + JSON (Python) |
| **Log Storage** | Loki with MinIO S3 backend |
| **Log Querying** | LogQL via Grafana |
| **Multi-tenancy** | X-Scope-OrgID header |
| **High Availability** | Read/Write/Backend microservices mode |
