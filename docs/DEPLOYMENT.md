# Deployment & Operations

This document covers Docker Compose configuration, environment variables, data persistence, and the observability stack.

---

## Docker Compose Overview

The primary orchestration file is `docker-compose.yml` in the project root. It defines **13 services**:

| Category | Services |
|----------|----------|
| **Infrastructure** | eureka-server, api-gateway, redis |
| **Business** | catalog-service, order-service, user-service |
| **Databases** | mysql-catalog (MySQL 8.0), mongodb-order (MongoDB 6.0), postgres-user (PostgreSQL 15) |
| **Observability** | zipkin, prometheus, loki, promtail, grafana |

### Startup Order

Docker Compose uses `depends_on` with health checks to enforce startup order:

```
1. Databases (MySQL, MongoDB, PostgreSQL) + Redis
2. Eureka Server
3. Business Services (catalog, order, user)
4. API Gateway
5. Observability (Zipkin, Prometheus, Loki, Promtail, Grafana)
```

### Common Commands

| Action | Command |
|--------|---------|
| Start all services | `docker-compose up --build -d` |
| Stop (preserve data) | `docker-compose down` |
| Stop and delete all data | `docker-compose down -v` |
| Rebuild one service | `docker-compose build catalog-service && docker-compose up -d catalog-service` |
| View all logs | `docker-compose logs -f` |
| View one service's logs | `docker-compose logs -f order-service` |
| Check status | `docker-compose ps` |
| Restart one service | `docker-compose restart api-gateway` |

---

## Environment Variables

All configurable values are defined via environment variables, with defaults in `docker-compose.yml`. Override them in a `.env` file at the project root.

### Application

| Variable | Default | Description |
|----------|---------|-------------|
| `CORS_ALLOWED_ORIGIN` | `http://localhost:4200` | Allowed CORS origin for the SPA frontend |
| `CORS_ADMIN_ORIGIN` | `http://localhost:8084` | Allowed CORS origin for the admin UI |
| `JWT_SECRET` | *(64-char default)* | HMAC secret for JWT signing (min 256 bits) |

### Databases

| Variable | Default | Description |
|----------|---------|-------------|
| `MYSQL_DATABASE` | `bookstore_catalog` | MySQL database name |
| `DB_PASSWORD` | `password` | MySQL root password |
| `MONGO_DATABASE` | `bookstore_orders` | MongoDB database name |
| `POSTGRES_DATABASE` | `bookstore_users` | PostgreSQL database name |
| `POSTGRES_USER` | `postgres` | PostgreSQL username |
| `POSTGRES_PASSWORD` | `password` | PostgreSQL password |

### Ports

| Variable | Default | Description |
|----------|---------|-------------|
| `MYSQL_PORT` | `3306` | MySQL host port |
| `MONGO_PORT` | `27017` | MongoDB host port |
| `POSTGRES_PORT` | `5432` | PostgreSQL host port |
| `REDIS_PORT` | `6379` | Redis host port |

> **Security:** The `.env` file is listed in `.gitignore`. Never commit real credentials.

---

## Data Persistence

Docker named volumes store database data:

| Volume | Service | Data |
|--------|---------|------|
| `mysql-data` | mysql-catalog | Book catalog |
| `mongodb-data` | mongodb-order | Orders |
| `postgres-data` | postgres-user | Users and credentials |
| `redis-data` | redis | Rate-limit counters (ephemeral) |
| `prometheus-data` | prometheus | Metrics history |
| `loki-data` | loki | Log history |
| `grafana-data` | grafana | Dashboards and settings |

### Persistence Behavior

| Scenario | Data Preserved? |
|----------|----------------|
| `docker-compose stop` / `start` | Yes |
| `docker-compose down` / `up` | Yes |
| Host machine restart | Yes |
| `docker-compose down -v` | **No** — volumes are deleted, init scripts re-run on next start |

---

## Database Initialization

On first start (when volumes are empty), Docker entrypoint scripts seed the databases:

| Container | Script | Mounted From |
|-----------|--------|-------------|
| mysql-catalog | `/docker-entrypoint-initdb.d/init-catalog.sql` | `scripts/init-catalog.sql` |
| postgres-user | `/docker-entrypoint-initdb.d/init-users.sql` | `scripts/init-users.sql` |
| mongodb-order | `/docker-entrypoint-initdb.d/init-orders.js` | `scripts/init-orders.js` |

To re-seed, remove volumes and restart:

```bash
docker-compose down -v
docker-compose up --build -d
```

See [scripts/README.md](../scripts/README.md) for details on the seed data.

---

## Observability Stack

### Distributed Tracing — Zipkin

| Item | Value |
|------|-------|
| URL | http://localhost:9411 |
| Integration | Spring Cloud Sleuth auto-instruments all services |

Trace IDs propagate through the API Gateway to downstream services. Open Zipkin to search traces by service name or trace ID.

### Metrics — Prometheus + Grafana

| Item | Value |
|------|-------|
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 (login: `admin` / `admin`) |

Prometheus scrapes `/actuator/prometheus` endpoints from each service. Grafana is pre-connected to Prometheus as a data source.

**Useful Prometheus queries:**

```promql
# Request rate per service
rate(http_server_requests_seconds_count[5m])

# 95th percentile latency
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))

# JVM memory usage
jvm_memory_used_bytes{area="heap"}
```

### Centralized Logging — Loki + Promtail + Grafana

| Item | Value |
|------|-------|
| Loki | http://localhost:3100 |
| Promtail | Sidecar agent collecting Docker container logs |

Promtail reads container logs via the Docker socket and ships them to Loki. In Grafana, select the Loki data source to query logs:

```logql
{container_name="bookstore-catalog-service"} |= "ERROR"
```

### Health Checks

All services expose Spring Actuator health endpoints:

```bash
curl http://localhost:8080/actuator/health   # Gateway
curl http://localhost:8081/actuator/health   # Catalog
curl http://localhost:8082/actuator/health   # Orders
curl http://localhost:8083/actuator/health   # Users
curl http://localhost:8761/actuator/health   # Eureka
```

---

## Service Ports Summary

| Service | Port | Protocol |
|---------|------|----------|
| API Gateway | 8080 | HTTP |
| Catalog Service | 8081 | HTTP |
| Order Service | 8082 | HTTP |
| User Service | 8083 | HTTP |
| Eureka Server | 8761 | HTTP |
| MySQL | 3306 | TCP |
| MongoDB | 27017 | TCP |
| PostgreSQL | 5432 | TCP |
| Redis | 6379 | TCP |
| Zipkin | 9411 | HTTP |
| Prometheus | 9090 | HTTP |
| Grafana | 3000 | HTTP |
| Loki | 3100 | HTTP |

---

## Quality Compose File

A separate `docker-compose.quality.yml` can be used to run quality checks:

```bash
docker-compose -f docker-compose.quality.yml up
```

This runs Checkstyle, SpotBugs, and other static analysis tools in containers.

---

[← Back to Documentation Hub](README.md) · [Testing Guide →](TESTING.md)
