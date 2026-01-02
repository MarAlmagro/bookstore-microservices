# Bookstore Microservices

Portfolio project demonstrating real-world microservices architecture using **Java 11** and **Spring Boot 2.7.18**, showcasing modularity, scalability, and clean service boundaries.

## Architecture Overview

This project implements a polyglot persistence microservices architecture with three core services:

- **Catalog Service** (Port 8081) - Book inventory management using MySQL
- **Order Service** (Port 8082) - Order lifecycle management using MongoDB
- **User Service** (Port 8083) - User authentication and authorization using PostgreSQL

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

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/bookstore-microservices.git
cd bookstore-microservices
```

### 2. Configure Environment Variables
```bash
# Copy the example environment file
cp .env.example .env

# Edit .env and update values (especially JWT_SECRET for production)
```

### 3. Start Databases with Docker Compose
```bash
# Start all database containers
docker-compose up -d

# Verify containers are running
docker ps

# Check logs if needed
docker-compose logs -f
```

### 4. Build All Services
```bash
# Build entire project (parent + all modules)
mvn clean install

# Or skip tests for faster build
mvn clean install -DskipTests
```

### 5. Run Services Individually

#### Option A: Run with Maven (Development)
```bash
# Terminal 1 - Catalog Service
cd catalog-service
mvn spring-boot:run

# Terminal 2 - Order Service
cd order-service
mvn spring-boot:run

# Terminal 3 - User Service
cd user-service
mvn spring-boot:run
```

#### Option B: Run JAR files
```bash
# Terminal 1
java -jar catalog-service/target/catalog-service-1.0.0-SNAPSHOT.jar

# Terminal 2
java -jar order-service/target/order-service-1.0.0-SNAPSHOT.jar

# Terminal 3
java -jar user-service/target/user-service-1.0.0-SNAPSHOT.jar
```

### 6. Access Services

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

## Testing the Full Workflow

### 1. Register a User
```bash
curl -X POST http://localhost:8083/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### 2. Login to Get JWT Token
```bash
curl -X POST http://localhost:8083/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

### 3. Create a Book (Admin Only)
```bash
curl -X POST http://localhost:8081/api/v1/books \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "isbn": "978-0134685991",
    "title": "Effective Java",
    "author": "Joshua Bloch",
    "price": 45.99,
    "stock": 100,
    "category": "Programming"
  }'
```

### 4. Browse Books
```bash
curl http://localhost:8081/api/v1/books
```

### 5. Create an Order
```bash
curl -X POST http://localhost:8082/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "userId": "user-id-from-login",
    "items": [
      {
        "bookId": "book-id-from-catalog",
        "quantity": 2,
        "price": 45.99
      }
    ]
  }'
```

## Troubleshooting

### Port Already in Use
```bash
# Find process using port
netstat -ano | findstr :8081  # Windows
lsof -i :8081                  # Mac/Linux

# Kill the process or change port in application.yml
```

### Database Connection Issues
```bash
# Check if database containers are running
docker ps

# Restart database containers
docker-compose restart mysql-catalog
docker-compose restart mongodb-order
docker-compose restart postgres-user

# View database logs
docker-compose logs -f mysql-catalog
```

### Maven Build Failures
```bash
# Clean Maven cache
mvn clean

# Force update dependencies
mvn clean install -U

# Skip tests to isolate build issues
mvn clean install -DskipTests
```

### JWT Token Issues
- Ensure `JWT_SECRET` in `.env` matches across all services
- Check token expiration (default 1 hour)
- Verify `Authorization: Bearer TOKEN` header format

## Project Status

- [x] Parent POM configuration
- [ ] Shared common module
- [ ] Catalog service implementation
- [ ] Order service implementation
- [ ] User service with JWT authentication
- [ ] Docker containerization
- [ ] Integration tests
- [ ] API Gateway (future enhancement)

## Contributing

This is a portfolio project, but suggestions are welcome! Please open an issue to discuss changes.

## License

This project is open source and available under the [MIT License](LICENSE).

## Contact

For questions or feedback, please open an issue on GitHub.

---

**Built with** ☕ **Java 11** | 🍃 **Spring Boot 2.7.18** | 🐳 **Docker**
