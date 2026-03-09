# Getting Started

Step-by-step guide to build, run, and verify the Bookstore Microservices stack on your local machine.

---

## Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| **Docker Desktop** | Latest | WSL2 backend recommended on Windows |
| **Java** | 11 | Only needed if building from source |
| **Maven** | 3.8+ | Only needed if building from source |
| **Git** | Any | To clone the repository |

> **Disk space:** The full Docker stack (images + volumes) requires approximately 5–6 GB.

---

## 1. Clone the Repository

```bash
git clone https://github.com/MarAlmagworeno/bookstore-microservices.git
cd bookstore-microservices
```

---

## 2. Environment Configuration

The project uses a `.env` file in the project root for runtime configuration. Create one from the defaults if it does not exist:

```env
# CORS — adjust origins to match your frontend
CORS_ALLOWED_ORIGIN=http://localhost:4200
CORS_ADMIN_ORIGIN=http://localhost:8084

# Database credentials (defaults used by docker-compose)
DB_PASSWORD=password
POSTGRES_USER=postgres
POSTGRES_PASSWORD=password

# JWT secret (minimum 256 bits for HS256)
JWT_SECRET=mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLongForHS256Algorithm
```

> **Security note:** Never commit real secrets. The `.env` file is git-ignored.

---

## 3. Build from Source

```bash
mvn clean install -DskipTests
```

This compiles all modules and produces Docker-ready JAR files under each service's `target/` directory.

---

## 4. Start the Stack

```bash
docker-compose up --build -d
```

This brings up **13 containers**: Eureka, API Gateway, 3 business services, 3 databases, Redis, Zipkin, Prometheus, Loki + Promtail, and Grafana.

Wait approximately **60–90 seconds** for all health checks to pass.

---

## 5. Verify Services

### Check container health

```bash
docker-compose ps
```

All services should show `healthy` or `running`.

### Test API endpoints

```bash
# Catalog — public, no auth required
curl http://localhost:8080/api/v1/books

# Auth — login with a seeded user
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john.doe@bookstore.com","password":"customer123"}'
```

### Swagger UI

Each business service exposes interactive API documentation:

| Service | Swagger URL |
|---------|-------------|
| Catalog | http://localhost:8081/swagger-ui.html |
| Orders | http://localhost:8082/swagger-ui.html |
| Users | http://localhost:8083/swagger-ui.html |

### Eureka Dashboard

Open http://localhost:8761 to see all registered services.

---

## 6. Pre-Seeded Data

On first start, initialization scripts populate the databases automatically:

| Database | Records | Details |
|----------|---------|---------|
| MySQL (Catalog) | 20 books | Programming, Fiction, Sci-Fi, Business, Mystery, etc. |
| PostgreSQL (Users) | 6 users | 1 admin + 5 customers |
| MongoDB (Orders) | 10 orders | Various statuses: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED |

### Test Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@bookstore.com` | `admin123` |
| Customer | `john.doe@bookstore.com` | `customer123` |
| Customer | `jane.smith@bookstore.com` | `customer123` |

> See [scripts/README.md](../scripts/README.md) for the full data catalog.

---

## 7. Stopping and Resetting

| Action | Command |
|--------|---------|
| Stop services (data preserved) | `docker-compose down` |
| Stop and **delete all data** | `docker-compose down -v` |
| Restart services | `docker-compose up -d` |
| Full rebuild from scratch | `docker-compose down -v && mvn clean install -DskipTests && docker-compose up --build -d` |
| View live logs | `docker-compose logs -f` |
| Logs for one service | `docker-compose logs -f catalog-service` |

---

## 8. Monitoring (Optional)

The observability stack starts alongside the application:

| Tool | URL | Credentials | Purpose |
|------|-----|-------------|---------|
| Eureka | http://localhost:8761 | — | Service registry dashboard |
| Grafana | http://localhost:3000 | `admin` / `admin` | Metrics & log dashboards |
| Prometheus | http://localhost:9090 | — | Metrics collection & queries |
| Zipkin | http://localhost:9411 | — | Distributed tracing |

---

## Troubleshooting

### Port conflicts

```bash
# Windows: find process using a port
netstat -ano | findstr :8080

# Kill by PID
taskkill /PID <pid> /F
```

### Services not becoming healthy

```bash
docker-compose logs -f api-gateway
docker-compose logs -f catalog-service
docker-compose logs -f user-service
docker-compose logs -f order-service
```

### CORS errors from a frontend

1. Verify `.env` contains the correct `CORS_ALLOWED_ORIGIN`.
2. Restart the gateway: `docker-compose restart api-gateway`.
3. Ensure the frontend runs on the exact origin specified (e.g., `http://localhost:4200`, not `127.0.0.1:4200`).

### Database connection failures

```bash
docker-compose restart mysql-catalog mongodb-order postgres-user
docker-compose logs mysql-catalog
```

### Clean rebuild

```bash
mvn clean install -DskipTests -U
docker-compose build --no-cache
docker-compose up -d
```

---

[← Back to Documentation Hub](README.md) · [Architecture →](ARCHITECTURE.md)
