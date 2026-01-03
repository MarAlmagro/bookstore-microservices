# Bookstore Microservices

Portfolio project demonstrating real-world microservices architecture using **Java 11** and **Spring Boot 2.7.18**, showcasing modularity, scalability, and clean service boundaries.

[![Java](https://img.shields.io/badge/Java-11-orange)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 Table of Contents

- [Architecture Overview](#architecture-overview)
- [Quick Start](#quick-start)
- [Testing](#testing)
- [API Endpoints](#api-endpoints)
- [Documentation](#documentation)
- [Troubleshooting](#troubleshooting)
- [Project Status](#project-status)

---

## Architecture Overview

This project implements a **polyglot persistence microservices architecture** with three core services:

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Catalog   │     │    Order    │     │    User     │
│   Service   │◄────┤   Service   │     │   Service   │
│  Port 8081  │     │  Port 8082  │     │  Port 8083  │
│             │     │             │     │             │
│   MySQL     │     │   MongoDB   │     │ PostgreSQL  │
└─────────────┘     └─────────────┘     └─────────────┘
```

### Core Services

- **Catalog Service** (Port 8081) - Book inventory management using MySQL
  - CRUD operations for books
  - Search and filtering
  - Stock management
  
- **Order Service** (Port 8082) - Order lifecycle management using MongoDB
  - Order creation with stock validation
  - Service-to-service communication with Catalog
  - Order history and status tracking
  
- **User Service** (Port 8083) - User authentication and authorization using PostgreSQL
  - JWT-based authentication
  - User registration and login
  - Role-based access control (CUSTOMER, ADMIN)

### Technology Stack

- **Java**: 11
- **Spring Boot**: 2.7.18
- **Build Tool**: Maven 3.8+
- **Databases**: MySQL 8.0, MongoDB 6.0, PostgreSQL 15
- **Security**: Spring Security + JWT
- **API Documentation**: SpringDoc OpenAPI 3 (Swagger)
- **Containerization**: Docker & Docker Compose

## Project Structure

```
bookstore-microservices/
├── pom.xml                          # Parent POM
├── shared-common/                   # Shared DTOs, exceptions, constants
├── catalog-service/                 # Book catalog microservice
├── order-service/                   # Order management microservice
├── user-service/                    # User authentication microservice
├── docker-compose.yml               # Database orchestration
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
# Check service health
curl http://localhost:8081/actuator/health  # Catalog
curl http://localhost:8082/actuator/health  # Order
curl http://localhost:8083/actuator/health  # User

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

| Service | URL | Swagger UI |
|---------|-----|------------|
| Catalog Service | http://localhost:8081 | http://localhost:8081/swagger-ui.html |
| Order Service | http://localhost:8082 | http://localhost:8082/swagger-ui.html |
| User Service | http://localhost:8083 | http://localhost:8083/swagger-ui.html |

## API Endpoints

### Catalog Service (Port 8081)
```
GET    /api/v1/books           # List all books
GET    /api/v1/books/{id}      # Get book by ID
POST   /api/v1/books           # Create book (admin only)
PUT    /api/v1/books/{id}      # Update book (admin only)
DELETE /api/v1/books/{id}      # Delete book (admin only)
GET    /api/v1/books/search    # Search books
```

### Order Service (Port 8082)
```
GET    /api/v1/orders          # List user's orders
GET    /api/v1/orders/{id}     # Get order details
POST   /api/v1/orders          # Create new order
PUT    /api/v1/orders/{id}/status  # Update order status
```

### User Service (Port 8083)
```
POST   /api/v1/auth/register   # User registration
POST   /api/v1/auth/login      # User login (returns JWT)
GET    /api/v1/users/profile   # Get user profile
PUT    /api/v1/users/profile   # Update user profile
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
curl -X POST http://localhost:8083/api/v1/auth/register \
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
curl -X POST http://localhost:8083/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@bookstore.com",
    "password": "admin123"
  }'
```

#### 3. Browse Books (No Auth Required)
```bash
curl http://localhost:8081/api/v1/books
```

#### 4. Create an Order (Auth Required)
```bash
curl -X POST http://localhost:8082/api/v1/orders \
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
- Fetches book details from Catalog Service
- Validates stock availability
- Calculates total amount
- Sets order status to PENDING

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
curl -X POST http://localhost:8083/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@bookstore.com","password":"admin123"}'
```

### No Books Available

If the catalog is empty, reinitialize databases:
```bash
docker-compose down -v
docker-compose up -d
sleep 30  # Wait for initialization
curl http://localhost:8081/api/v1/books
```

For more troubleshooting help, see **[docs/TESTING.md](docs/TESTING.md#troubleshooting)**

## Documentation

- **[Testing Guide](docs/TESTING.md)** - Comprehensive testing instructions
- **[Architecture](docs/ARCHITECTURE.md)** - Detailed system design and patterns
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
- [x] Parent POM configuration
- [x] Shared common module (DTOs, exceptions, constants)
- [x] Catalog service implementation
- [x] Order service implementation
- [x] User service with JWT authentication
- [x] Docker containerization with health checks
- [x] Database initialization scripts
- [x] Integration tests and test scripts
- [x] Swagger/OpenAPI documentation
- [x] Service-to-service communication
- [x] Comprehensive documentation

### 🔄 Future Enhancements
- [ ] API Gateway (Spring Cloud Gateway)
- [ ] Service Discovery (Eureka)
- [ ] Circuit Breaker (Resilience4j)
- [ ] Distributed Tracing (Sleuth + Zipkin)
- [ ] Message Queue (RabbitMQ/Kafka)
- [ ] Caching Layer (Redis)
- [ ] Admin UI (Thymeleaf + Tailwind)
- [ ] Customer Frontend (Angular SPA)

## Contributing

This is a portfolio project, but suggestions are welcome! Please open an issue to discuss changes.

## License

This project is open source and available under the [MIT License](LICENSE).

## Contact

For questions or feedback, please open an issue on GitHub.

---

**Built with** ☕ **Java 11** | 🍃 **Spring Boot 2.7.18** | 🐳 **Docker**
