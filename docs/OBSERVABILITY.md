# Observability Guide

Complete guide to the **LGTM Stack** (Loki, Grafana, Tempo/Zipkin, Metrics/Prometheus) implementation in the Bookstore Microservices project.

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Quick Start](#quick-start)
- [Components](#components)
- [Configuration](#configuration)
- [Usage Guide](#usage-guide)
- [Troubleshooting](#troubleshooting)
- [Best Practices](#best-practices)

---

## Overview

The observability stack provides comprehensive monitoring, logging, and tracing capabilities across all microservices:

- **Distributed Tracing** - Track requests across service boundaries
- **Centralized Logging** - Aggregate logs from all services with trace correlation
- **Metrics Collection** - Monitor service health and performance
- **Unified Dashboard** - Single pane of glass for all observability data

### Technology Stack

| Component | Technology | Port | Purpose |
|-----------|-----------|------|---------|
| Distributed Tracing | Zipkin | 9411 | Trace collection and visualization |
| Metrics | Prometheus | 9090 | Time-series metrics database |
| Logging | Loki | 3100 | Log aggregation and storage |
| Log Shipping | Promtail | - | Docker log collection |
| Visualization | Grafana | 3000 | Unified observability dashboard |

### Key Features

✅ **Automatic trace propagation** across all microservices  
✅ **Trace ID correlation** in logs for easy debugging  
✅ **Service dependency mapping** to visualize architecture  
✅ **Performance metrics** for all services and endpoints  
✅ **Centralized log search** with trace context  
✅ **Real-time monitoring** dashboards

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Client Request                          │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
              ┌───────────────┐
              │  API Gateway  │ ← Trace starts here
              │   (Sleuth)    │   (generates traceId)
              └───────┬───────┘
                      │
        ┌─────────────┼─────────────┐
        │             │             │
        ▼             ▼             ▼
    ┌───────┐    ┌───────┐    ┌───────┐
    │Catalog│    │ Order │    │ User  │ ← Trace propagates
    │Service│    │Service│    │Service│   (same traceId)
    └───┬───┘    └───┬───┘    └───┬───┘
        │            │            │
        │            │            │
        └────────────┼────────────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
        ▼            ▼            ▼
    ┌───────┐   ┌────────┐   ┌──────┐
    │Zipkin │   │Promtail│   │Prom- │
    │       │   │   ↓    │   │etheus│
    │       │   │ Loki   │   │      │
    └───┬───┘   └────┬───┘   └───┬──┘
        │            │           │
        └────────────┼───────────┘
                     │
                     ▼
              ┌──────────┐
              │ Grafana  │ ← Unified view
              └──────────┘
```

### Data Flow

1. **Request Initiation**: API Gateway receives request and generates `traceId` and `spanId`
2. **Trace Propagation**: Headers (`X-B3-TraceId`, `X-B3-SpanId`) propagate to downstream services
3. **Log Enrichment**: All log entries include `traceId` and `spanId` in structured format
4. **Metrics Export**: Services expose `/actuator/prometheus` endpoint for scraping
5. **Data Collection**:
   - Zipkin collects trace spans from all services
   - Promtail ships Docker logs to Loki
   - Prometheus scrapes metrics endpoints
6. **Visualization**: Grafana queries all data sources for unified view

---

## Quick Start

### 1. Start the Observability Stack

All observability components are included in the main `docker-compose.yml`:

```bash
# Start all services including observability stack
docker-compose up -d

# Verify observability services are running
docker-compose ps | grep -E "zipkin|prometheus|loki|grafana|promtail"
```

Expected output:
```
grafana-dashboard    grafana/grafana:latest    Up      0.0.0.0:3000->3000/tcp
loki-server          grafana/loki:latest       Up      0.0.0.0:3100->3100/tcp
prometheus-server    prom/prometheus:latest    Up      0.0.0.0:9090->9090/tcp
promtail-agent       grafana/promtail:latest   Up
zipkin-server        openzipkin/zipkin:latest  Up      0.0.0.0:9411->9411/tcp
```

### 2. Access Observability UIs

| Service | URL | Credentials |
|---------|-----|-------------|
| **Grafana** | http://localhost:3000 | admin / admin |
| **Zipkin** | http://localhost:9411 | None required |
| **Prometheus** | http://localhost:9090 | None required |

### 3. Generate Sample Traffic

```bash
# Run integration test to generate traces
./scripts/test-integration.sh

# Or manually create requests
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@bookstore.com","password":"admin123"}'

# Create an order (generates multi-service trace)
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "userId": 1,
    "items": [{"bookId": 1, "quantity": 2}]
  }'
```

### 4. View Traces in Zipkin

1. Open http://localhost:9411
2. Click **"Run Query"** to see recent traces
3. Click on a trace to see the full request flow across services
4. Observe:
   - Total request duration
   - Individual service spans
   - Service dependencies
   - Any errors or slow operations

---

## Components

### 1. Zipkin - Distributed Tracing

**Purpose**: Visualize request flows across microservices

**Access**: http://localhost:9411

**Features**:
- End-to-end request tracing
- Service dependency graph
- Latency analysis
- Error tracking

**How to Use**:

1. **View Recent Traces**:
   - Open Zipkin UI
   - Click "Run Query" (default: last 15 minutes)
   - Traces are sorted by timestamp

2. **Analyze a Trace**:
   - Click on any trace to see details
   - View the timeline of spans across services
   - Check duration of each service call
   - Identify bottlenecks

3. **Search Traces**:
   - Filter by service name (e.g., `api-gateway`)
   - Filter by span name (e.g., `GET /api/v1/books`)
   - Filter by minimum duration
   - Filter by tags (e.g., `error=true`)

4. **Service Dependencies**:
   - Click "Dependencies" tab
   - View service interaction graph
   - See call volumes between services

**Example Trace Flow**:
```
api-gateway (50ms)
  └─> order-service (30ms)
      └─> catalog-service (15ms)
```

### 2. Prometheus - Metrics Collection

**Purpose**: Collect and store time-series metrics

**Access**: http://localhost:9090

**Features**:
- Service health metrics
- JVM metrics (heap, threads, GC)
- HTTP request metrics
- Custom application metrics
- Alerting capabilities

**How to Use**:

1. **View Metrics**:
   - Open Prometheus UI
   - Go to "Graph" tab
   - Enter a metric query (PromQL)

2. **Common Queries**:

```promql
# HTTP request rate per service
rate(http_server_requests_seconds_count[5m])

# JVM memory usage
jvm_memory_used_bytes{area="heap"}

# Request duration 95th percentile
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))

# Error rate
rate(http_server_requests_seconds_count{status=~"5.."}[5m])
```

3. **View Targets**:
   - Go to "Status" → "Targets"
   - Verify all services are being scraped
   - Check for any scrape errors

**Available Metrics**:
- `http_server_requests_seconds_*` - HTTP request metrics
- `jvm_memory_*` - JVM memory usage
- `jvm_threads_*` - Thread statistics
- `jvm_gc_*` - Garbage collection metrics
- `system_cpu_*` - CPU usage
- `process_*` - Process metrics

### 3. Loki - Log Aggregation

**Purpose**: Centralized log storage with trace correlation

**Access**: http://localhost:3100 (API only, use Grafana for UI)

**Features**:
- Centralized log storage
- Efficient log indexing
- Trace ID correlation
- Label-based querying

**Log Format**:
All services log with trace context:
```
2026-01-04 18:30:15 [http-nio-8081-exec-1] INFO  c.b.catalog.controller.BookController - [traceId=abc123, spanId=def456] - Fetching book with ID: 1
```

**How to Query** (via Grafana):

1. Open Grafana → Explore
2. Select "Loki" data source
3. Use LogQL queries:

```logql
# All logs from catalog-service
{container="catalog-service"}

# Logs with specific trace ID
{container=~".*-service"} |= "traceId=abc123"

# Error logs across all services
{container=~".*-service"} |= "ERROR"

# Logs from order-service in last 5 minutes
{container="order-service"} |= "" [5m]
```

### 4. Promtail - Log Shipping

**Purpose**: Collect Docker container logs and ship to Loki

**Configuration**: `test-data/promtail/promtail-config.yml`

**Features**:
- Automatic Docker log discovery
- Container label extraction
- Real-time log streaming

**How It Works**:
1. Promtail connects to Docker socket
2. Discovers all running containers
3. Tails container logs in real-time
4. Adds container labels (name, image, etc.)
5. Ships logs to Loki

### 5. Grafana - Unified Dashboard

**Purpose**: Visualize all observability data in one place

**Access**: http://localhost:3000  
**Credentials**: admin / admin

**Pre-configured Data Sources**:
- ✅ Prometheus (metrics)
- ✅ Loki (logs)
- ✅ Zipkin (traces)

**How to Use**:

#### A. Explore Logs

1. Click "Explore" (compass icon)
2. Select "Loki" data source
3. Enter LogQL query or use builder
4. View logs with trace context
5. Click on trace ID to jump to Zipkin

#### B. Explore Metrics

1. Click "Explore"
2. Select "Prometheus" data source
3. Enter PromQL query or use builder
4. Visualize metrics as graph or table

#### C. Create Dashboard

1. Click "+" → "Dashboard"
2. Add panel
3. Select data source (Prometheus/Loki)
4. Configure query and visualization
5. Save dashboard

#### D. Correlate Data

**Scenario**: Debug a slow request

1. **Find Trace in Zipkin**:
   - Identify slow trace
   - Copy `traceId`

2. **View Logs in Grafana**:
   - Go to Explore → Loki
   - Query: `{container=~".*-service"} |= "traceId=YOUR_TRACE_ID"`
   - See all logs for that request

3. **Check Metrics**:
   - Go to Explore → Prometheus
   - Query service metrics during that time
   - Identify resource constraints

---

## Configuration

### Service Configuration

All services are configured with observability in their `application.yml`:

#### Spring Cloud Sleuth (Tracing)

```yaml
spring.sleuth:
  sampler:
    probability: 1.0  # Sample 100% of requests (use 0.1 for 10% in production)
```

#### Zipkin Export

```yaml
spring.zipkin:
  base-url: http://localhost:9411
  enabled: true
```

#### Logging Pattern

```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - [traceId=%X{traceId}, spanId=%X{spanId}] - %msg%n"
```

#### Actuator Endpoints

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### Prometheus Configuration

**File**: `test-data/prometheus/prometheus.yml`

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'api-gateway'
    static_configs:
      - targets: ['api-gateway:8080']
    metrics_path: '/actuator/prometheus'

  - job_name: 'catalog-service'
    static_configs:
      - targets: ['catalog-service:8081']
    metrics_path: '/actuator/prometheus'

  - job_name: 'order-service'
    static_configs:
      - targets: ['order-service:8082']
    metrics_path: '/actuator/prometheus'

  - job_name: 'user-service'
    static_configs:
      - targets: ['user-service:8083']
    metrics_path: '/actuator/prometheus'
```

### Promtail Configuration

**File**: `test-data/promtail/promtail-config.yml`

```yaml
server:
  http_listen_port: 9080
  grpc_listen_port: 0

positions:
  filename: /tmp/positions.yaml

clients:
  - url: http://loki:3100/loki/api/v1/push

scrape_configs:
  - job_name: docker
    docker_sd_configs:
      - host: unix:///var/run/docker.sock
        refresh_interval: 5s
    relabel_configs:
      - source_labels: ['__meta_docker_container_name']
        regex: '/(.*)'
        target_label: 'container'
```

### Maven Dependencies

**File**: `shared-common/pom.xml`

```xml
<!-- Spring Cloud Sleuth for Distributed Tracing -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>

<!-- Zipkin for Trace Export -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-sleuth-zipkin</artifactId>
</dependency>

<!-- Micrometer Prometheus for Metrics -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>

<!-- Actuator for Health and Metrics Endpoints -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

---

## Usage Guide

### Scenario 1: Debug a Failed Order

**Problem**: Order creation fails intermittently

**Steps**:

1. **Find Failed Traces in Zipkin**:
   ```
   - Open http://localhost:9411
   - Add annotation query: error=true
   - Click "Run Query"
   - Identify failed order creation traces
   ```

2. **Analyze Trace**:
   ```
   - Click on failed trace
   - Identify which service failed
   - Check span tags for error details
   - Note the traceId (e.g., "abc123def456")
   ```

3. **View Logs in Grafana**:
   ```
   - Open Grafana → Explore → Loki
   - Query: {container="order-service"} |= "traceId=abc123def456"
   - Review error logs with full stack trace
   ```

4. **Check Service Health**:
   ```
   - Grafana → Explore → Prometheus
   - Query: rate(http_server_requests_seconds_count{status="500"}[5m])
   - Identify error rate spike
   ```

### Scenario 2: Identify Performance Bottleneck

**Problem**: Orders are taking too long to process

**Steps**:

1. **Find Slow Traces**:
   ```
   - Zipkin → Set minDuration to 1000ms (1 second)
   - Run Query
   - Click on slowest trace
   ```

2. **Analyze Span Duration**:
   ```
   - Review timeline view
   - Identify longest span
   - Example: catalog-service taking 800ms
   ```

3. **Check Database Queries**:
   ```
   - Grafana → Loki
   - Query: {container="catalog-service"} |= "traceId=SLOW_TRACE_ID"
   - Look for slow SQL queries in logs
   ```

4. **Monitor Resource Usage**:
   ```
   - Prometheus query: jvm_memory_used_bytes{service="catalog-service"}
   - Check if service is under memory pressure
   ```

### Scenario 3: Monitor Service Health

**Create a Grafana Dashboard**:

1. **Create New Dashboard**:
   ```
   Grafana → + → Dashboard → Add new panel
   ```

2. **Add Panels**:

   **Panel 1: Request Rate**
   ```promql
   sum(rate(http_server_requests_seconds_count[5m])) by (service)
   ```

   **Panel 2: Error Rate**
   ```promql
   sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) by (service)
   ```

   **Panel 3: Response Time (p95)**
   ```promql
   histogram_quantile(0.95, 
     sum(rate(http_server_requests_seconds_bucket[5m])) by (service, le)
   )
   ```

   **Panel 4: JVM Memory**
   ```promql
   jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}
   ```

3. **Save Dashboard**: Name it "Microservices Overview"

### Scenario 4: Trace a Multi-Service Request

**Example**: Order creation flow

1. **Create Order**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/orders \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer TOKEN" \
     -d '{"userId": 1, "items": [{"bookId": 1, "quantity": 2}]}'
   ```

2. **View in Zipkin**:
   ```
   - Open Zipkin
   - Find latest trace
   - Observe flow:
     1. api-gateway receives request
     2. order-service processes order
     3. order-service calls catalog-service
     4. catalog-service queries database
     5. Response flows back
   ```

3. **Correlate Logs**:
   ```logql
   # In Grafana Loki
   {container=~".*-service"} |= "traceId=YOUR_TRACE_ID"
   ```

   You'll see logs from all services with the same traceId:
   ```
   [api-gateway]    - [traceId=abc123] - Routing request to order-service
   [order-service]  - [traceId=abc123] - Creating order for user 1
   [order-service]  - [traceId=abc123] - Calling catalog-service for book 1
   [catalog-service]- [traceId=abc123] - Fetching book with ID: 1
   [catalog-service]- [traceId=abc123] - Book found, stock: 10
   [order-service]  - [traceId=abc123] - Order created successfully
   ```

---

## Troubleshooting

### Issue: No Traces in Zipkin

**Symptoms**: Zipkin UI shows no traces

**Solutions**:

1. **Check Zipkin is running**:
   ```bash
   docker-compose ps zipkin
   curl http://localhost:9411/health
   ```

2. **Verify services can reach Zipkin**:
   ```bash
   docker exec -it api-gateway ping zipkin
   ```

3. **Check service configuration**:
   ```bash
   # Verify Zipkin URL in application.yml
   docker exec -it api-gateway cat /app/application.yml | grep zipkin
   ```

4. **Check service logs**:
   ```bash
   docker-compose logs api-gateway | grep -i zipkin
   ```

5. **Verify sampling rate**:
   ```yaml
   # Should be 1.0 for testing (100% sampling)
   spring.sleuth.sampler.probability: 1.0
   ```

### Issue: Metrics Not Showing in Prometheus

**Symptoms**: Prometheus targets are down or no data

**Solutions**:

1. **Check Prometheus targets**:
   ```
   - Open http://localhost:9090/targets
   - Verify all services are "UP"
   - Check for scrape errors
   ```

2. **Verify actuator endpoints**:
   ```bash
   curl http://localhost:8081/actuator/prometheus
   # Should return metrics in Prometheus format
   ```

3. **Check network connectivity**:
   ```bash
   docker exec -it prometheus-server ping catalog-service
   ```

4. **Verify Prometheus config**:
   ```bash
   docker exec -it prometheus-server cat /etc/prometheus/prometheus.yml
   ```

### Issue: No Logs in Loki

**Symptoms**: Grafana shows no logs from Loki

**Solutions**:

1. **Check Promtail is running**:
   ```bash
   docker-compose ps promtail
   docker-compose logs promtail
   ```

2. **Verify Promtail can reach Loki**:
   ```bash
   docker exec -it promtail-agent ping loki
   ```

3. **Check Promtail configuration**:
   ```bash
   docker exec -it promtail-agent cat /etc/promtail/config.yml
   ```

4. **Verify Docker socket access**:
   ```bash
   docker exec -it promtail-agent ls -la /var/run/docker.sock
   ```

5. **Test Loki API**:
   ```bash
   curl http://localhost:3100/ready
   ```

### Issue: Grafana Can't Connect to Data Sources

**Symptoms**: Data source health check fails

**Solutions**:

1. **Check data source configuration**:
   ```
   Grafana → Configuration → Data Sources
   - Prometheus: http://prometheus:9090
   - Loki: http://loki:3100
   - Zipkin: http://zipkin:9411
   ```

2. **Verify network connectivity**:
   ```bash
   docker exec -it grafana-dashboard ping prometheus
   docker exec -it grafana-dashboard ping loki
   docker exec -it grafana-dashboard ping zipkin
   ```

3. **Check all services are on same network**:
   ```bash
   docker network inspect bookstore-network
   ```

### Issue: Trace IDs Not in Logs

**Symptoms**: Logs don't show traceId and spanId

**Solutions**:

1. **Verify logging pattern**:
   ```yaml
   logging:
     pattern:
       console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - [traceId=%X{traceId}, spanId=%X{spanId}] - %msg%n"
   ```

2. **Check Sleuth is enabled**:
   ```bash
   # Look for Sleuth auto-configuration in logs
   docker-compose logs api-gateway | grep -i sleuth
   ```

3. **Verify dependencies**:
   ```bash
   # Check shared-common/pom.xml includes spring-cloud-starter-sleuth
   ```

---

## Best Practices

### 1. Trace Sampling

**Development**:
```yaml
spring.sleuth.sampler.probability: 1.0  # 100% sampling
```

**Production**:
```yaml
spring.sleuth.sampler.probability: 0.1  # 10% sampling
```

**Why**: Sampling all requests in production can be expensive. Sample 10-20% for most use cases.

### 2. Log Levels

**Development**:
```yaml
logging.level:
  com.bookstore: DEBUG
  org.springframework.cloud.gateway: DEBUG
```

**Production**:
```yaml
logging.level:
  com.bookstore: INFO
  org.springframework.cloud.gateway: WARN
```

**Why**: Reduce log volume and storage costs in production.

### 3. Metrics Retention

**Prometheus**:
```yaml
# Keep metrics for 15 days
--storage.tsdb.retention.time=15d
```

**Loki**:
```yaml
# Keep logs for 7 days
retention_period: 168h
```

### 4. Custom Metrics

Add custom metrics in your services:

```java
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;

@Service
public class OrderService {
    private final Counter orderCounter;
    
    public OrderService(MeterRegistry registry) {
        this.orderCounter = Counter.builder("orders.created")
            .description("Total orders created")
            .tag("service", "order-service")
            .register(registry);
    }
    
    public Order createOrder(OrderRequest request) {
        Order order = // ... create order
        orderCounter.increment();
        return order;
    }
}
```

Query in Prometheus:
```promql
rate(orders_created_total[5m])
```

### 5. Structured Logging

Use structured logging for better searchability:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class BookController {
    private static final Logger log = LoggerFactory.getLogger(BookController.class);
    
    @GetMapping("/books/{id}")
    public Book getBook(@PathVariable Long id) {
        log.info("Fetching book with ID: {}", id);
        // ... implementation
    }
}
```

### 6. Alerting

Set up alerts in Prometheus for critical issues:

```yaml
# prometheus-alerts.yml
groups:
  - name: microservices
    rules:
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          
      - alert: HighLatency
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High latency detected"
```

### 7. Dashboard Organization

**Create separate dashboards**:
- **Overview Dashboard**: High-level metrics for all services
- **Service Dashboard**: Detailed metrics per service
- **Infrastructure Dashboard**: JVM, CPU, memory metrics
- **Business Dashboard**: Order rates, user registrations, etc.

### 8. Security Considerations

**Production Setup**:
- Enable authentication for Grafana
- Restrict Prometheus/Zipkin access (internal only)
- Use HTTPS for all observability endpoints
- Implement API keys for Loki push API
- Sanitize sensitive data from logs and traces

---

## Additional Resources

### Documentation
- [Spring Cloud Sleuth](https://spring.io/projects/spring-cloud-sleuth)
- [Zipkin](https://zipkin.io/)
- [Prometheus](https://prometheus.io/docs/)
- [Grafana Loki](https://grafana.com/docs/loki/latest/)
- [Grafana](https://grafana.com/docs/)

### Useful Links
- [PromQL Cheat Sheet](https://promlabs.com/promql-cheat-sheet/)
- [LogQL Cheat Sheet](https://grafana.com/docs/loki/latest/logql/)
- [Grafana Dashboards](https://grafana.com/grafana/dashboards/)

### Next Steps
1. Create custom Grafana dashboards for your use cases
2. Set up alerting rules in Prometheus
3. Implement custom metrics for business KPIs
4. Configure log retention policies
5. Set up automated trace analysis

---

**Built with** ☕ **Java 11** | 🍃 **Spring Boot 2.7.18** | 📊 **LGTM Stack**
