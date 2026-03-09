# Bookstore Microservices

A portfolio-grade microservices application built with **Spring Boot 2.7**, **Spring Cloud 2021.0**, and **Docker Compose**. It demonstrates service discovery, API gateway routing, JWT authentication, distributed tracing, and observability — all orchestrated in containers.

> **AI-Assisted Development:** This project was designed, implemented, and tested with the help of AI coding assistants (GitHub Copilot, Windsurf Cascade). AI tools were used for code generation, test authoring, architecture decisions, documentation, and code review. All output was validated and curated by the project author.

---

## Architecture at a Glance

```
                        ┌──────────────┐
                        │  SPA / Admin │
                        │  Frontend    │
                        └──────┬───────┘
                               │  HTTP :8080
                               ▼
                      ┌─────────────────┐
                      │   API Gateway   │──── Redis (rate limiting)
                      └────┬────┬────┬──┘
                           │    │    │
              ┌────────────┘    │    └────────────┐
              ▼                 ▼                  ▼
     ┌────────────────┐ ┌────────────────┐ ┌────────────────┐
     │ catalog-service│ │ order-service  │ │  user-service  │
     │   (MySQL 8.0)  │ │  (MongoDB 6.0)│ │ (PostgreSQL 15)│
     │   Port 8081    │ │   Port 8082   │ │   Port 8083    │
     └────────────────┘ └────────────────┘ └────────────────┘
              │                 │                  │
              └─────────┬──────┘──────────────────┘
                        ▼
              ┌─────────────────┐
              │  Eureka Server  │
              │   Port 8761     │
              └─────────────────┘
```

| Service | Port | Database | Purpose |
|---------|------|----------|---------|
| **API Gateway** | 8080 | Redis | Single entry point, JWT validation, rate limiting, CORS |
| **Catalog Service** | 8081 | MySQL 8.0 | Book inventory CRUD, batch import |
| **Order Service** | 8082 | MongoDB 6.0 | Order lifecycle, cart management |
| **User Service** | 8083 | PostgreSQL 15 | Authentication, user profiles, JWT issuing |
| **Eureka Server** | 8761 | — | Service discovery registry |
| **Admin UI** | — | — | Server-side rendered admin dashboard (Thymeleaf) |
| **shared-common** | — | — | Shared DTOs, exceptions, JWT utilities |

---

## Quick Start

### Prerequisites

| Requirement | Notes |
|-------------|-------|
| **Docker Desktop** | WSL2 backend recommended on Windows |
| **Java 11** | Only if building from source |
| **Maven 3.8+** | Only if building from source |

### Run Everything

```bash
# 1. Build Maven artifacts
mvn clean install -DskipTests

# 2. Start the full stack (services + databases + observability)
docker-compose up --build -d

# 3. Wait ~90 seconds, then verify
docker-compose ps
curl http://localhost:8080/api/v1/books
```

> Databases are pre-seeded with sample data (20 books, 6 users, 10 orders) on first run.

### Stop / Reset

```bash
docker-compose down          # Stop (data preserved in volumes)
docker-compose down -v       # Stop AND wipe all data
```

---

## Documentation

All detailed documentation lives in the [`docs/`](docs/README.md) directory:

| Document | Description |
|----------|-------------|
| [Getting Started](docs/GETTING-STARTED.md) | Prerequisites, build, run, and verify step by step |
| [Architecture](docs/ARCHITECTURE.md) | System design, service interactions, security model |
| [API Reference](docs/API-REFERENCE.md) | Endpoints, authentication, Swagger links |
| [Development Guide](docs/DEVELOPMENT.md) | Building, coding conventions, Maven profiles, contributing |
| [Testing Guide](docs/TESTING.md) | Test strategy, running tests, coverage |
| [Deployment & Operations](docs/DEPLOYMENT.md) | Docker Compose, environment variables, observability |
| [Database Scripts](scripts/README.md) | Initialization scripts and sample data |
| [SPA Frontend Spec](docs/spa-frontend-spec/README.md) | OpenAPI contract, UI spec, and agentic coding rules for building a frontend |

---

## Technology Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Java 11 |
| **Framework** | Spring Boot 2.7.18 |
| **Cloud** | Spring Cloud 2021.0.8 (Eureka, Gateway) |
| **Security** | Spring Security + JWT (jjwt 0.11.5) |
| **Databases** | MySQL 8.0, MongoDB 6.0, PostgreSQL 15, Redis 7 |
| **API Docs** | SpringDoc OpenAPI 1.8.0 |
| **Build** | Maven multi-module |
| **Containers** | Docker Compose |
| **Observability** | Zipkin, Prometheus, Grafana, Loki |
| **Quality** | JaCoCo, Checkstyle, SpotBugs, OWASP Dependency-Check |
| **Admin UI** | Thymeleaf + Tailwind CSS |

---

## Project Structure

```
bookstore-microservices/
├── api-gateway/            # Spring Cloud Gateway
├── catalog-service/        # Book catalog (MySQL)
├── order-service/          # Order management (MongoDB)
├── user-service/           # Auth & users (PostgreSQL)
├── eureka-server/          # Service discovery
├── admin-ui/               # Thymeleaf admin dashboard
├── shared-common/          # Shared library (DTOs, exceptions, JWT)
├── scripts/                # DB init scripts & test scripts
├── test-fixtures/          # Test data, Prometheus/Grafana configs
├── docs/                   # Full project documentation
├── docker-compose.yml      # Full stack orchestration
├── docker-compose.quality.yml
└── pom.xml                 # Parent POM (multi-module)
```

---

## Test Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@bookstore.com` | `admin123` |
| Customer | `john.doe@bookstore.com` | `customer123` |

---

## License

This project is licensed under the [MIT License](LICENSE).

**Author:** María del Mar Almagro Moreno
