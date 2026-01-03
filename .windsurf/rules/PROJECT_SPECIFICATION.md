# Spring Boot Microservices Bookstore - Project Specification

## Project Overview
Portfolio project demonstrating real-world microservices architecture using Java 11 and Spring Boot, showcasing modularity, scalability, and clean service boundaries.

---

## Technical Stack

### Core Framework
- **Java Version**: 11
- **Spring Boot Version**: 2.7.18 (latest Java 11 compatible)
- **Build Tool**: Maven 3.8+
- **Packaging**: JAR files

### Spring Dependencies (Core)
- spring-boot-starter-web
- spring-boot-starter-data-jpa (Catalog, User)
- spring-boot-starter-data-mongodb (Order)
- spring-boot-starter-security
- spring-boot-starter-validation
- spring-boot-starter-test

---

## Architecture Overview

### 3 Core Microservices

#### 1. Catalog Service
- **Purpose**: Book inventory management (CRUD, search, metadata)
- **Port**: 8081
- **Database**: MySQL 8.0
- **Base Package**: `com.bookstore.catalog`

#### 2. Order Service
- **Purpose**: Order lifecycle management (cart, checkout, order history)
- **Port**: 8082
- **Database**: MongoDB 6.0
- **Base Package**: `com.bookstore.order`

#### 3. User Service
- **Purpose**: User authentication, registration, role management
- **Port**: 8083
- **Database**: PostgreSQL 15 (via Supabase)
- **Base Package**: `com.bookstore.user`

### Supporting Components

#### API Gateway (Future Phase)
- **Port**: 8080
- **Technology**: Spring Cloud Gateway
- **Purpose**: Single entry point, routing, load balancing

#### Shared Common Module
- **Package**: `com.bookstore.common`
- **Contents**: DTOs, exceptions, constants, validation annotations

---

## Database Strategy (Polyglot Persistence)

### Catalog Service - MySQL
```yaml
Database: bookstore_catalog
Tables: books, categories, authors
Rationale: Relational structure for normalized book data
```

### Order Service - MongoDB
```yaml
Database: bookstore_orders
Collections: orders, cart_items
Rationale: Flexible schema for varying order structures
```

### User Service - PostgreSQL (Supabase)
```yaml
Database: bookstore_users
Tables: users, roles, permissions
Rationale: Cloud-managed, built-in auth features, ACID compliance
```

---

## Project Structure (Maven Multi-Module)

```
bookstore-microservices/
├── pom.xml                          # Parent POM
├── shared-common/                   # Shared module
│   ├── pom.xml
│   └── src/main/java/com/bookstore/common/
│       ├── dto/                     # Data Transfer Objects
│       │   ├── BookDTO.java
│       │   ├── OrderDTO.java
│       │   └── UserDTO.java
│       ├── exception/               # Custom exceptions
│       │   ├── ResourceNotFoundException.java
│       │   ├── InvalidRequestException.java
│       │   └── GlobalExceptionHandler.java
│       ├── constants/               # Application constants
│       │   ├── OrderStatus.java
│       │   └── UserRole.java
│       └── validation/              # Custom validators
│
├── catalog-service/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/
│       │   ├── java/com/bookstore/catalog/
│       │   │   ├── CatalogServiceApplication.java
│       │   │   ├── controller/      # REST Controllers
│       │   │   ├── service/         # Business logic
│       │   │   ├── repository/      # Data access
│       │   │   ├── entity/          # JPA entities
│       │   │   ├── mapper/          # Entity-DTO mappers
│       │   │   └── config/          # Configuration classes
│       │   └── resources/
│       │       ├── application.yml
│       │       └── application-dev.yml
│       └── test/
│           └── java/com/bookstore/catalog/
│               ├── controller/      # Controller tests
│               ├── service/         # Service tests
│               └── integration/     # Integration tests
│
├── order-service/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/
│       │   ├── java/com/bookstore/order/
│       │   │   ├── OrderServiceApplication.java
│       │   │   ├── controller/
│       │   │   ├── service/
│       │   │   ├── repository/
│       │   │   ├── document/        # MongoDB documents
│       │   │   ├── mapper/
│       │   │   └── config/
│       │   └── resources/
│       │       ├── application.yml
│       │       └── application-dev.yml
│       └── test/
│           └── java/com/bookstore/order/
│
├── user-service/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/
│       │   ├── java/com/bookstore/user/
│       │   │   ├── UserServiceApplication.java
│       │   │   ├── controller/
│       │   │   ├── service/
│       │   │   ├── repository/
│       │   │   ├── entity/
│       │   │   ├── security/        # Security configs
│       │   │   ├── mapper/
│       │   │   └── config/
│       │   └── resources/
│       │       ├── application.yml
│       │       └── application-dev.yml
│       └── test/
│           └── java/com/bookstore/user/
│
├── docker-compose.yml               # Orchestrate all services
├── .env.example                     # Environment variables template
└── README.md                        # Project documentation
```

---

## REST API Design Patterns

### Naming Conventions
- Use **plural nouns** for resources: `/books`, `/orders`, `/users`
- Use **HTTP verbs** for actions: GET, POST, PUT, DELETE
- Use **path parameters** for IDs: `/books/{id}`
- Use **query parameters** for filters: `/books?category=fiction&author=xyz`

### Standard Endpoints Pattern

#### Catalog Service (Port 8081)
```
GET    /api/v1/books                # List all books (with pagination)
GET    /api/v1/books/{id}           # Get book by ID
POST   /api/v1/books                # Create new book (admin only)
PUT    /api/v1/books/{id}           # Update book (admin only)
DELETE /api/v1/books/{id}           # Delete book (admin only)
GET    /api/v1/books/search         # Search books by criteria
```

#### Order Service (Port 8082)
```
GET    /api/v1/orders               # List user's orders
GET    /api/v1/orders/{id}          # Get order details
POST   /api/v1/orders               # Create new order
PUT    /api/v1/orders/{id}/status   # Update order status
POST   /api/v1/cart/items           # Add item to cart
GET    /api/v1/cart                 # View cart
```

#### User Service (Port 8083)
```
POST   /api/v1/auth/register        # User registration
POST   /api/v1/auth/login           # User login (returns JWT)
POST   /api/v1/auth/refresh         # Refresh token
GET    /api/v1/users/profile        # Get user profile
PUT    /api/v1/users/profile        # Update user profile
```

---

## Service-to-Service Communication

### HTTP Client Strategy
- **Technology**: RestTemplate (Spring Boot 2.7.x compatible)
- **Alternative** (Future): WebClient (reactive) or Spring Cloud OpenFeign

### Communication Patterns
1. **Order Service → Catalog Service**
   - Purpose: Validate book availability, fetch book details
   - Endpoint: `http://localhost:8081/api/v1/books/{id}`

2. **Order Service → User Service**
   - Purpose: Validate user, fetch user details
   - Endpoint: `http://localhost:8083/api/v1/users/{id}`

### Error Handling
- Implement Circuit Breaker pattern (Future: Resilience4j)
- Use proper HTTP status codes
- Return consistent error response format:
```json
{
  "timestamp": "2025-01-02T10:15:30",
  "status": 404,
  "error": "Not Found",
  "message": "Book not found with id: 123",
  "path": "/api/v1/books/123"
}
```

---

## Security Implementation

### Authentication & Authorization
- **Technology**: Spring Security + JWT
- **Token Type**: Bearer tokens
- **Token Expiry**: 1 hour (access), 7 days (refresh)

### User Roles
```java
public enum UserRole {
    CUSTOMER,  // Can browse, order books
    ADMIN      // Can manage books, view all orders
}
```

### Security Flow
1. User registers → User Service stores credentials (BCrypt hashed)
2. User logs in → User Service validates, returns JWT
3. User requests protected resource → Service validates JWT
4. Service extracts user info from JWT, enforces role-based access

### Endpoint Security Matrix
| Endpoint Pattern | CUSTOMER | ADMIN | Anonymous |
|-----------------|----------|-------|-----------|
| GET /books | ✓ | ✓ | ✓ |
| POST /books | ✗ | ✓ | ✗ |
| POST /orders | ✓ | ✓ | ✗ |
| GET /users/profile | ✓ (own) | ✓ (all) | ✗ |

---

## Configuration Management

### Application Configuration Pattern
Each service has:
- `application.yml` - Default config
- `application-dev.yml` - Development profile
- `application-prod.yml` - Production profile (Future)

### Example: Catalog Service `application.yml`
```yaml
spring:
  application:
    name: catalog-service
  profiles:
    active: dev
  datasource:
    url: jdbc:mysql://localhost:3306/bookstore_catalog
    username: ${DB_USER:root}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect

server:
  port: 8081
  servlet:
    context-path: /

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html

logging:
  level:
    com.bookstore.catalog: DEBUG
    org.springframework.web: INFO
```

---

## Testing Strategy

### Test Layers

#### 1. Unit Tests
- **Framework**: JUnit 5
- **Mocking**: Mockito
- **Coverage Target**: 80%+
- **Focus**: Service layer, business logic

```java
@ExtendWith(MockitoExtension.class)
class BookServiceTest {
    @Mock
    private BookRepository bookRepository;
    
    @InjectMocks
    private BookService bookService;
    
    @Test
    void shouldReturnBookWhenExists() {
        // Test implementation
    }
}
```

#### 2. Controller Tests
- **Framework**: MockMvc
- **Focus**: Request/response mapping, validation

```java
@WebMvcTest(BookController.class)
class BookControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private BookService bookService;
    
    @Test
    void shouldReturnBookList() throws Exception {
        mockMvc.perform(get("/api/v1/books"))
               .andExpect(status().isOk());
    }
}
```

#### 3. Integration Tests
- **Framework**: TestRestTemplate
- **Database**: Embedded H2 (Catalog, User), Embedded MongoDB (Order)
- **Focus**: End-to-end flows, service-to-service communication

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class BookIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldCreateAndRetrieveBook() {
        // Test implementation
    }
}
```

### Test Data Strategy
- Use **Test Data Builders** pattern
- Create reusable fixtures per service
- No shared-test module (maintain service independence)

---

## API Documentation

### Swagger/OpenAPI 3
- **Library**: springdoc-openapi-ui
- **Access**: `http://localhost:{port}/swagger-ui.html`
- **Configuration**: Automatically generated from annotations

### Annotations Strategy
```java
@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "Books", description = "Book management APIs")
public class BookController {
    
    @Operation(summary = "Get all books", description = "Returns list of all books with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<BookDTO>> getAllBooks() {
        // Implementation
    }
}
```

---

## Docker Configuration

### Docker Compose Structure
- Each service has its own Dockerfile
- Shared docker-compose.yml orchestrates everything
- Includes databases (MySQL, MongoDB, PostgreSQL)

### Service Dependencies
```yaml
version: '3.8'

services:
  mysql-catalog:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: bookstore_catalog
      MYSQL_ROOT_PASSWORD: password
    ports:
      - "3306:3306"
  
  mongodb-order:
    image: mongo:6.0
    ports:
      - "27017:27017"
  
  postgres-user:
    image: postgres:15
    environment:
      POSTGRES_DB: bookstore_users
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
  
  catalog-service:
    build: ./catalog-service
    ports:
      - "8081:8081"
    depends_on:
      - mysql-catalog
  
  order-service:
    build: ./order-service
    ports:
      - "8082:8082"
    depends_on:
      - mongodb-order
  
  user-service:
    build: ./user-service
    ports:
      - "8083:8083"
    depends_on:
      - postgres-user
```

### Dockerfile Pattern (Per Service)
```dockerfile
FROM openjdk:11-jre-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Development Workflow

### Build Commands
```bash
# Build all services
mvn clean install

# Build specific service
cd catalog-service && mvn clean install

# Skip tests
mvn clean install -DskipTests

# Run tests only
mvn test
```

### Run Commands (Local Development)

#### Option 1: Run Individually (Without Docker)
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

#### Option 2: Docker Compose (Recommended)
```bash
# Start all services
docker-compose up --build

# Start in background
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f catalog-service
```

### Development Tools URLs
- Catalog Service Swagger: http://localhost:8081/swagger-ui.html
- Order Service Swagger: http://localhost:8082/swagger-ui.html
- User Service Swagger: http://localhost:8083/swagger-ui.html

---

## Environment Variables

### Required Environment Variables
Create `.env` file in project root:
```bash
# Database Credentials
DB_USER=root
DB_PASSWORD=password

# JWT Configuration
JWT_SECRET=your-256-bit-secret-key-here
JWT_EXPIRATION=3600000

# Supabase Configuration (User Service)
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_KEY=your-supabase-key
```

---

## Future Enhancements (Post-MVP)

### Phase 2: UI Development
1. **Admin Panel**: Thymeleaf + Tailwind CSS
   - Book management interface
   - Order dashboard
   
2. **Customer Frontend**: Angular SPA
   - Book browsing
   - Shopping cart
   - Order history
   - CORS configuration required

### Phase 3: Advanced Features
- API Gateway (Spring Cloud Gateway)
- Service Discovery (Eureka)
- Distributed Tracing (Sleuth + Zipkin)
- Circuit Breaker (Resilience4j)
- Centralized Configuration (Spring Cloud Config)
- Message Queue (RabbitMQ/Kafka for async communication)
- Caching (Redis)

---

## Project Goals & Learning Outcomes

### Portfolio Value
- ✓ Demonstrates microservices architecture understanding
- ✓ Shows polyglot persistence knowledge
- ✓ Proves REST API design skills
- ✓ Exhibits security implementation (JWT)
- ✓ Showcases testing proficiency
- ✓ Displays Docker/containerization capability

### Interview Talking Points
1. "Why different databases?" → Explain polyglot persistence benefits
2. "Why multi-module?" → Discuss development convenience vs production separation
3. "How do services communicate?" → Explain synchronous HTTP vs future async messaging
4. "How do you handle failures?" → Discuss error handling, future circuit breakers
5. "How do you secure APIs?" → Explain JWT flow, role-based access

---

## Development Principles

### Code Quality
- Follow SOLID principles
- Use meaningful variable/method names
- Keep methods focused (Single Responsibility)
- Write self-documenting code
- Add comments only for complex logic

### Git Strategy
- Feature branches: `feature/catalog-crud`
- Commit messages: `feat(catalog): add book search endpoint`
- One feature per pull request

### Documentation
- Update README.md with setup instructions
- Document API contracts
- Add inline Javadoc for public methods
- Maintain CHANGELOG.md

---

## Getting Started Checklist

- [ ] Install Java 11
- [ ] Install Maven 3.8+
- [ ] Install Docker Desktop
- [ ] Install Windsurf IDE
- [ ] Configure Claude API in Windsurf
- [ ] Clone project repository
- [ ] Set up .env file
- [ ] Run `mvn clean install`
- [ ] Start Docker Compose
- [ ] Access Swagger UI for each service
- [ ] Run test suite

---

## Support & Resources

### Documentation References
- Spring Boot Docs: https://docs.spring.io/spring-boot/docs/2.7.18/reference/html/
- Spring Security JWT: https://spring.io/guides/tutorials/spring-boot-oauth2/
- SpringDoc OpenAPI: https://springdoc.org/
- Docker Compose: https://docs.docker.com/compose/

### Command Reference
```bash
# Maven
mvn clean install          # Build all modules
mvn test                   # Run tests
mvn spring-boot:run        # Run service

# Docker
docker-compose up          # Start all services
docker-compose down        # Stop all services
docker-compose logs -f     # Follow logs
docker ps                  # List running containers

# Git
git checkout -b feature/xyz    # Create feature branch
git commit -m "feat: xyz"      # Commit with convention
git push origin feature/xyz    # Push to remote
```

---

**Last Updated**: January 2, 2025  
**Version**: 1.0.0  
**Status**: Ready for Implementation
