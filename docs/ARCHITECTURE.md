# Architecture

This document describes the system design, service responsibilities, communication patterns, and cross-cutting concerns of the Bookstore Microservices platform.

---

## System Overview

```
  Clients (SPA / Admin UI)
           │
           │  HTTP :8080
           ▼
  ┌─────────────────────┐
  │    API Gateway       │──── Redis 7 (rate limiting)
  │  (Spring Cloud GW)  │
  └──┬──────┬──────┬────┘
     │      │      │          Eureka Server :8761
     │      │      │          (service discovery)
     ▼      ▼      ▼
  ┌──────┐┌──────┐┌──────┐
  │Catalog││Order ││ User │
  │Service││Service││Service│
  │:8081 ││:8082 ││:8083 │
  └──┬───┘└──┬───┘└──┬───┘
     │       │       │
     ▼       ▼       ▼
  MySQL   MongoDB  PostgreSQL
   8.0      6.0       15
```

### Observability Sidecar

```
  Services ──metrics──▶ Prometheus :9090 ──▶ Grafana :3000
  Services ──logs────▶ Promtail ──▶ Loki :3100 ──▶ Grafana
  Services ──traces──▶ Zipkin :9411
```

---

## Service Responsibilities

### API Gateway (`api-gateway`)

- **Role:** Single entry point for all client traffic.
- **Technology:** Spring Cloud Gateway (reactive).
- **Key features:**
  - Route requests to downstream services via Eureka service discovery.
  - Validate JWT tokens on protected routes.
  - Rate limiting per endpoint group (backed by Redis).
  - CORS configuration (origins set via environment variables).

### Catalog Service (`catalog-service`)

- **Role:** Book inventory management.
- **Database:** MySQL 8.0 (`bookstore_catalog`).
- **Key features:**
  - CRUD operations for books.
  - Paginated listing and search (by keyword, category).
  - Spring Batch import job for bulk catalog ingestion from CSV.
  - Public GET endpoints; POST/PUT/DELETE require `ADMIN` role.

### Order Service (`order-service`)

- **Role:** Order lifecycle and shopping cart management.
- **Database:** MongoDB 6.0 (`bookstore_orders`).
- **Key features:**
  - Create, view, and manage orders.
  - Shopping cart (add/remove items).
  - Order status transitions: PENDING → CONFIRMED → SHIPPED → DELIVERED / CANCELLED.
  - Spring Batch export job for mainframe-format order reporting.
  - Cross-service call to Catalog Service for stock validation.

### User Service (`user-service`)

- **Role:** Authentication and user profile management.
- **Database:** PostgreSQL 15 (`bookstore_users`).
- **Key features:**
  - User registration and login.
  - JWT token issuance and refresh.
  - BCrypt password hashing.
  - Role-based access: `ADMIN` and `CUSTOMER`.

### Eureka Server (`eureka-server`)

- **Role:** Service discovery registry.
- **Technology:** Spring Cloud Netflix Eureka Server.
- All business services register on startup and are discovered by the API Gateway.

### Admin UI (`admin-ui`)

- **Role:** Server-side rendered administration dashboard.
- **Technology:** Spring Boot + Thymeleaf + Tailwind CSS.
- Communicates with backend services via OpenFeign clients.

### Shared Common (`shared-common`)

- **Role:** Shared library consumed by all services.
- **Contents:** DTOs, exception classes, error codes, JWT utilities, page mapping helpers.

---

## Database Strategy

Each service owns its database — no shared schemas.

| Service | Engine | Schema | Rationale |
|---------|--------|--------|-----------|
| Catalog | MySQL 8.0 | `bookstore_catalog` | Relational model for structured book data with JPA/Hibernate |
| Orders | MongoDB 6.0 | `bookstore_orders` | Flexible document model for orders with nested items and addresses |
| Users | PostgreSQL 15 | `bookstore_users` | Relational model with strong transactional guarantees for auth |

Data is persisted in Docker named volumes (`mysql-data`, `mongodb-data`, `postgres-data`) and survives container restarts.

---

## Authentication & Authorization

### JWT Flow

```
1. Client  ──POST /api/v1/auth/login──▶  User Service
   Client  ◀── { token, refreshToken, user } ──

2. Client  ──GET /api/v1/orders──▶  API Gateway
   (Authorization: Bearer <token>)
   Gateway validates token → forwards to Order Service

3. Token expired:
   Client  ──POST /api/v1/auth/refresh──▶  User Service
   Client  ◀── { token, refreshToken } ──
```

### Role-Based Access Control

| Capability | CUSTOMER | ADMIN |
|-----------|----------|-------|
| Browse/search books | ✓ | ✓ |
| View own profile | ✓ | ✓ |
| Create orders | ✓ | ✓ |
| View own orders | ✓ | ✓ |
| Manage book catalog | ✗ | ✓ |
| View all orders | ✗ | ✓ |
| Manage users | ✗ | ✓ |
| Update order status | ✗ | ✓ |

---

## API Gateway Routing

| Route Pattern | Target Service | Auth Required |
|---------------|----------------|---------------|
| `/api/v1/auth/**` | user-service | No (public) |
| `/api/v1/users/**` | user-service | Yes |
| `GET /api/v1/books/**` | catalog-service | No (public) |
| `POST/PUT/DELETE /api/v1/books/**` | catalog-service | Yes (ADMIN) |
| `/api/v1/orders/**` | order-service | Yes |
| `/api/v1/cart/**` | order-service | Yes |

### Rate Limiting

| Endpoint Group | Requests/sec | Burst |
|----------------|-------------|-------|
| Auth endpoints | 5 | 10 |
| Book endpoints | 10 | 20 |
| Order endpoints | 10 | 20 |

---

## Error Handling

All services return a standardized error response:

```json
{
  "timestamp": "2026-03-09T17:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: email is required",
  "path": "/api/v1/auth/register"
}
```

Machine-readable error codes (e.g., `ERR_AUTH_001`) are defined in `shared-common` for consistent cross-service error identification.

---

## Cross-Cutting Concerns

| Concern | Implementation |
|---------|---------------|
| **Service Discovery** | Eureka Server; all services register and discover via Eureka client |
| **Configuration** | Spring Boot externalized config via environment variables and `.env` |
| **Distributed Tracing** | Spring Cloud Sleuth + Zipkin |
| **Metrics** | Spring Boot Actuator → Prometheus → Grafana |
| **Centralized Logging** | Promtail → Loki → Grafana |
| **Health Checks** | Spring Actuator `/actuator/health`; Docker Compose health checks |
| **Resilience** | Gateway rate limiting; service restart policies |

---

## Batch Processing

| Service | Job | Trigger | Description |
|---------|-----|---------|-------------|
| Catalog | CSV Import | Manual / scheduled | Imports books from CSV files via Spring Batch |
| Orders | Mainframe Export | Scheduled | Exports orders in mainframe-compatible format |

---

[← Back to Documentation Hub](README.md) · [API Reference →](API-REFERENCE.md)
