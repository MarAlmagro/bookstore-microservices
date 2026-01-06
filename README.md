# Bookstore Microservices

Portfolio project demonstrating real-world microservices architecture using **Java 11** and **Spring Boot 2.7.18**, showcasing modularity, scalability, and clean service boundaries.

[![Java](https://img.shields.io/badge/Java-11-orange)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 Table of Contents

- [Architecture Overview](#architecture-overview)
- [Quick Start](#quick-start)
- [x] [Security Audit & Hardening](SECURITY.md)
- [x] [Testing](#testing)
- [x] [API Endpoints](#api-endpoints)
- [x] [Documentation](#documentation)
- [x] [Troubleshooting](#troubleshooting)
- [x] [Project Status](#project-status)

---

## Architecture Overview

This project implements a **polyglot persistence microservices architecture** with API Gateway, Service Discovery, and three core services:

```
                    ┌──────────────┐
                    │   Client     │
                    └──────┬───────┘
                           │
                           ▼
                    ┌──────────────┐
                    │ API Gateway  │  ← Single Entry Point
                    │  Port 8080   │
                    └──────┬───────┘
                           │
                           ├─────────► ┌──────────────┐
                           │           │Eureka Server │
                           │           │  Port 8761   │
                           │           └──────────────┘
                           │                   ▲
        ┌──────────────────┼──────────────────┼────────────┐
        │                  │                  │            │
        ▼                  ▼                  ▼            ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Catalog   │    │    Order    │    │    User     │
│   Service   │◄───┤   Service   │    │   Service   │
│  Port 8081  │    │  Port 8082  │    │  Port 8083  │
│             │    │             │    │             │
│   MySQL     │    │   MongoDB   │    │ PostgreSQL  │
└─────────────┘    └─────────────┘    └─────────────┘
        │                  │                  │
        └──────────────────┴──────────────────┘
                           │
                  Observability Stack
        ┌──────────────────┼──────────────────┐
        ▼                  ▼                  ▼
  ┌──────────┐      ┌──────────┐      ┌──────────┐
  │  Zipkin  │      │Prometheus│      │   Loki   │
  │  :9411   │      │  :9090   │      │  :3100   │
  └────┬─────┘      └────┬─────┘      └────┬─────┘
       │                 │                  │
       └─────────────────┴──────────────────┘
                         │
                         ▼
                  ┌──────────────┐
                  │   Grafana    │  ← Unified Dashboard
                  │   Port 3000  │
                  └──────────────┘
```

### Infrastructure Services

- **API Gateway** (Port 8080) - Single entry point for all client requests
  - Route management and load balancing
  - Service discovery integration
  - CORS configuration
  - Request logging and filtering

- **Eureka Server** (Port 8761) - Service discovery and registration
  - Dynamic service registration
  - Health monitoring
  - Load balancing support
  - Service instance management

### Observability Stack (LGTM)

- **Zipkin** (Port 9411) - Distributed tracing
  - End-to-end request tracing across services
  - Trace visualization and analysis
  - Service dependency mapping
  - Performance bottleneck identification

- **Prometheus** (Port 9090) - Metrics collection
  - Time-series metrics database
  - Service health metrics
  - Custom application metrics
  - Alerting capabilities

- **Loki** (Port 3100) - Log aggregation
  - Centralized log storage
  - Trace ID correlation with logs
  - Efficient log querying
  - Docker container log collection

- **Grafana** (Port 3000) - Unified observability dashboard
  - Metrics visualization from Prometheus
  - Log exploration from Loki
  - Trace analysis from Zipkin
  - Custom dashboards and alerts

### Core Services

- **Catalog Service** (Port 8081) - Book inventory management using MySQL
  - CRUD operations for books
  - Search and filtering
  - Stock management
  - Registered with Eureka
  
- **Order Service** (Port 8082) - Order lifecycle management using MongoDB
  - Order creation with stock validation
  - Service-to-service communication via OpenFeign
  - Resilience patterns (Circuit Breaker, Retry, Bulkhead)
  - Order history and status tracking
  - Registered with Eureka
  
- **User Service** (Port 8083) - User authentication and authorization using PostgreSQL
  - JWT-based authentication
  - User registration and login
  - Role-based access control (CUSTOMER, ADMIN)
  - Registered with Eureka

### Technology Stack

- **Java**: 11
- **Spring Boot**: 2.7.18
- **Spring Cloud**: 2021.0.8
- **Build Tool**: Maven 3.8+
- **Databases**: MySQL 8.0, MongoDB 6.0, PostgreSQL 15
- **Security**: Spring Security + JWT
- **API Gateway**: Spring Cloud Gateway
- **Service Discovery**: Netflix Eureka
- **Resilience**: Resilience4j (Circuit Breaker, Retry, Bulkhead)
- **Service Communication**: OpenFeign
- **Distributed Tracing**: Spring Cloud Sleuth + Zipkin
- **Metrics**: Micrometer + Prometheus
- **Logging**: Loki + Promtail
- **Monitoring**: Grafana
- **API Documentation**: SpringDoc OpenAPI 3 (Swagger)
- **Containerization**: Docker & Docker Compose

## Project Structure

```
bookstore-microservices/
├── pom.xml                          # Parent POM with Spring Cloud
├── shared-common/                   # Shared Dtos, exceptions, constants
├── eureka-server/                   # Service discovery server
├── api-gateway/                     # API Gateway (entry point)
├── catalog-service/                 # Book catalog microservice
├── order-service/                   # Order management microservice
├── user-service/                    # User authentication microservice
├── docker-compose.yml               # Full stack orchestration
├── test-data/                       # Test scenarios and configs
│   ├── promtail/                    # Promtail configuration
│   └── scenarios/                   # Observability test scenarios
└── .env.example                     # Environment variables template
```

## Prerequisites

- **Java 11** - [Download OpenJDK 11](https://adoptium.net/)
- **Maven 3.8+** - [Download Maven](https://maven.apache.org/download.cgi)
- **Docker Desktop** - [Download Docker](https://www.docker.com/products/docker-desktop/)
- **Git** - [Download Git](https://git-scm.com/downloads)

Verify installations:
```bash
java -version    # Should show Java 11
mvn -version     # Should show Maven 3.8+
docker --version # Should show Docker version
```

## Quick Start

### 🚀 One-Command Setup (Docker Compose)

```bash
# Clone the repository
git clone https://github.com/yourusername/bookstore-microservices.git
cd bookstore-microservices

# Start all services (databases + microservices)
docker-compose up -d

# Wait for services to be healthy (30-60 seconds)
docker-compose ps
```

**That's it!** All services are now running with sample data loaded.

### ✅ Verify Installation

```bash
# Check API Gateway (main entry point)
curl http://localhost:8080/actuator/health

# Check Eureka Server
curl http://localhost:8761/actuator/health

# Access Eureka Dashboard to see registered services
# Open browser: http://localhost:8761

# All should return: {"status":"UP"}
```

### 🧪 Run Integration Tests

```bash
# Run full E2E test (registers user, creates order, validates services)
./scripts/test-integration.sh

# Or test individual services
./scripts/test-catalog.sh
./scripts/test-user.sh
./scripts/test-order.sh
```

### 🌐 Access Services

**Primary Access (via API Gateway)**:

| Service | URL | Description |
|---------|-----|-------------|
| **API Gateway** | http://localhost:8080 | Single entry point for all API requests |
| **Eureka Dashboard** | http://localhost:8761 | Service registry and health monitoring |

**Observability Stack**:

| Service | URL | Description |
|---------|-----|-------------|
| **Grafana** | http://localhost:3000 | Unified observability dashboard (admin/admin) |
| **Zipkin** | http://localhost:9411 | Distributed tracing UI |
| **Prometheus** | http://localhost:9090 | Metrics and monitoring |
| **Loki** | http://localhost:3100 | Log aggregation (API only) |

**Direct Service Access** (for development/debugging):

| Service | URL | Swagger UI |
|---------|-----|------------|
| Catalog Service | http://localhost:8081 | http://localhost:8081/swagger-ui.html |
| Order Service | http://localhost:8082 | http://localhost:8082/swagger-ui.html |
| User Service | http://localhost:8083 | http://localhost:8083/swagger-ui.html |

**Note**: In production, all requests should go through the API Gateway (port 8080).

## API Endpoints

**All endpoints are accessed via API Gateway at `http://localhost:8080`**

### Catalog Service (via Gateway)
```
GET    http://localhost:8080/api/v1/books           # List all books
GET    http://localhost:8080/api/v1/books/{id}      # Get book by ID
POST   http://localhost:8080/api/v1/books           # Create book (admin only)
PUT    http://localhost:8080/api/v1/books/{id}      # Update book (admin only)
DELETE http://localhost:8080/api/v1/books/{id}      # Delete book (admin only)
GET    http://localhost:8080/api/v1/books/search    # Search books
```

### Order Service (via Gateway)
```
GET    http://localhost:8080/api/v1/orders          # List user's orders
GET    http://localhost:8080/api/v1/orders/{id}     # Get order details
POST   http://localhost:8080/api/v1/orders          # Create new order
PUT    http://localhost:8080/api/v1/orders/{id}/status  # Update order status
```

### User Service (via Gateway)
```
POST   http://localhost:8080/api/v1/auth/register   # User registration
POST   http://localhost:8080/api/v1/auth/login      # User login (returns JWT)
GET    http://localhost:8080/api/v1/users/profile   # Get user profile
PUT    http://localhost:8080/api/v1/users/profile   # Update user profile
```

### Gateway Management
```
GET    http://localhost:8080/actuator/gateway/routes  # View configured routes
GET    http://localhost:8080/actuator/health          # Gateway health check
```

## Development Workflow

### Running Tests
```bash
# Run all tests
mvn test

# Run tests for specific service
cd catalog-service && mvn test

# Run with coverage report
mvn test jacoco:report
```

### Building Individual Services
```bash
# Build specific service
cd catalog-service
mvn clean package

# Build and skip tests
mvn clean package -DskipTests
```

### Database Management

#### View Database Contents
```bash
# MySQL (Catalog)
docker exec -it bookstore-mysql mysql -uroot -ppassword bookstore_catalog

# MongoDB (Order)
docker exec -it bookstore-mongodb mongosh bookstore_orders

# PostgreSQL (User)
docker exec -it bookstore-postgres psql -U postgres -d bookstore_users
```

#### Reset Databases
```bash
# Stop and remove containers with volumes
docker-compose down -v

# Restart fresh
docker-compose up -d
```

## Testing

### Automated Test Scripts

Comprehensive test scripts are provided in the `scripts/` directory:

```bash
# Full E2E integration test (recommended)
./scripts/test-integration.sh

# Individual service tests
./scripts/test-catalog.sh    # Book CRUD operations
./scripts/test-user.sh        # Authentication flow
./scripts/test-order.sh       # Order creation & validation

# Resilience testing (chaos engineering)
./scripts/test-resilience.sh  # Circuit breaker, retry, and bulkhead patterns
```

### Test Credentials

**Admin Account**:
```
Email: admin@bookstore.com
Password: admin123
Role: ADMIN
```

**Customer Account**:
```
Email: test@test.com
Password: test123
Role: CUSTOMER
```

### Manual Testing Examples

#### 1. Register a User
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

#### 2. Login to Get JWT Token
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@bookstore.com",
    "password": "admin123"
  }'
```

#### 3. Browse Books (No Auth Required)
```bash
curl http://localhost:8080/api/v1/books
```

#### 4. Create an Order (Auth Required)
```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "userId": 1,
    "items": [
      {
        "bookId": 1,
        "quantity": 2
      }
    ]
  }'
```

**Note**: The Order Service automatically:
- Fetches book details from Catalog Service via Eureka
- Validates stock availability
- Calculates total amount
- Sets order status to PENDING
- All service-to-service communication uses service discovery

## Troubleshooting

### Services Not Starting

```bash
# Check Docker is running
docker ps

# View service logs
docker-compose logs catalog-service
docker-compose logs order-service
docker-compose logs user-service

# Restart all services
docker-compose restart

# Full restart with rebuild
docker-compose down
docker-compose up --build -d
```

### Port Conflicts

**Windows**:
```powershell
netstat -ano | findstr :8081
taskkill /PID <PID> /F
```

**Linux/Mac**:
```bash
lsof -i :8081
kill -9 <PID>
```

### Database Connection Issues

```bash
# Check database containers
docker-compose ps

# Restart databases
docker-compose restart mysql-catalog mongodb-order postgres-user

# Reset all data (WARNING: deletes everything)
docker-compose down -v
docker-compose up -d
```

### JWT Token Expired

Tokens expire after 1 hour. Login again to get a new token:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@bookstore.com","password":"admin123"}'
```

### No Books Available

If the catalog is empty, reinitialize databases:
```bash
docker-compose down -v
docker-compose up -d
sleep 60  # Wait for all services to register with Eureka
curl http://localhost:8080/api/v1/books
```

For more troubleshooting help, see **[docs/TESTING.md](docs/TESTING.md#troubleshooting)**

## Documentation

- **[Resilience Guide](docs/RESILIENCE.md)** - Circuit breaker, retry, and bulkhead patterns
- **[Observability Guide](docs/OBSERVABILITY.md)** - Distributed tracing, logging, and metrics setup
- **[Testing Guide](docs/TESTING.md)** - Comprehensive testing instructions
- **[Architecture](docs/ARCHITECTURE.md)** - Detailed system design and patterns
- **[API Gateway Guide](docs/GATEWAY.md)** - Gateway configuration and routing
- **[Test Scenarios](test-data/scenarios/)** - Happy path, error cases, integration tests
- **[Test Results](test-data/TEST_RESULTS.md)** - Phase 6.4 validation results

## Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `JWT_SECRET` | Secret key for JWT signing | `your-256-bit-secret` | Yes |
| `JWT_EXPIRATION` | Access token expiration (ms) | `3600000` (1 hour) | No |
| `DB_USER` | Database username | `root` | No |
| `DB_PASSWORD` | Database password | `password` | No |
| `CATALOG_SERVICE_URL` | Catalog service URL | `http://localhost:8081` | No |

See `.env.example` for complete list.

## Project Status

### ✅ Completed
- [x] Parent POM configuration with Spring Cloud
- [x] Shared common module (Dtos, exceptions, constants)
- [x] Catalog service implementation
- [x] Order service implementation
- [x] User service with JWT authentication
- [x] **API Gateway (Spring Cloud Gateway)**
- [x] **Service Discovery (Netflix Eureka)**
- [x] **Service-to-service communication via OpenFeign**
- [x] **Resilience Patterns (Resilience4j)**
  - [x] Circuit Breaker with fallback
  - [x] Retry with exponential backoff
  - [x] Bulkhead for concurrent call limiting
  - [x] Resilience metrics in Grafana
- [x] **Observability Stack (LGTM)**
  - [x] Distributed Tracing (Sleuth + Zipkin)
  - [x] Metrics Collection (Prometheus)
  - [x] Log Aggregation (Loki + Promtail)
  - [x] Unified Dashboard (Grafana)
- [x] Docker containerization with health checks
- [x] Database initialization scripts
- [x] Integration tests and test scripts
- [x] Chaos engineering tests
- [x] Swagger/OpenAPI documentation
- [x] Comprehensive documentation
- [x] **Security Audit & Hardening (NVD Verified)**
  - [x] SnakeYAML 2.x upgrade (0 CVEs)
  - [x] Tomcat 9.0.x hardening
  - [x] Dependency-Check Maven integration
  - [x] [SECURITY.md](SECURITY.md) documentation

### 🔄 Future Enhancements
- [ ] Message Queue (RabbitMQ/Kafka)
- [ ] Caching Layer (Redis)
- [ ] Admin UI (Thymeleaf + Tailwind)
- [ ] Customer Frontend (Angular SPA)
- [ ] Kubernetes Deployment

## Contributing

This is a portfolio project, but suggestions are welcome! Please open an issue to discuss changes.

## License

This project is open source and available under the [MIT License](LICENSE).

## Contact

For questions or feedback, please open an issue on GitHub.

---

**Built with** ☕ **Java 11** | 🍃 **Spring Boot 2.7.18** | 🐳 **Docker**
