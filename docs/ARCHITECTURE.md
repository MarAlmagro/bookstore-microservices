# Architecture Documentation - Bookstore Microservices

This document provides detailed architectural information about the Bookstore Microservices system.

---

## Table of Contents

1. [System Overview](#system-overview)
2. [Architecture Diagram](#architecture-diagram)
3. [Service Details](#service-details)
4. [Database Strategy](#database-strategy)
5. [Communication Patterns](#communication-patterns)
6. [Security Architecture](#security-architecture)
7. [Data Flow](#data-flow)
8. [Design Decisions](#design-decisions)
9. [Scalability Considerations](#scalability-considerations)
10. [Future Enhancements](#future-enhancements)

---

## System Overview

The Bookstore Microservices application is a distributed system built using Spring Boot 2.7.18 and Java 11. It demonstrates modern microservices architecture patterns including:

- **Service Independence**: Each service has its own database and can be deployed independently
- **Polyglot Persistence**: Different databases for different data models
- **RESTful APIs**: HTTP-based communication between services
- **JWT Authentication**: Stateless authentication across services
- **Docker Containerization**: Consistent deployment across environments

### Key Characteristics

- **3 Core Microservices**: Catalog, Order, User
- **3 Database Technologies**: MySQL, MongoDB, PostgreSQL
- **Synchronous Communication**: REST/HTTP
- **Stateless Design**: JWT tokens for authentication
- **API Documentation**: Swagger/OpenAPI 3

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Layer                             │
│  (Web Browser, Mobile App, API Clients)                         │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     │ HTTP/REST
                     │
┌────────────────────┴────────────────────────────────────────────┐
│                    API Gateway (Future)                          │
│  - Routing                                                       │
│  - Load Balancing                                                │
│  - Rate Limiting                                                 │
└────────────────────┬────────────────────────────────────────────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
        ▼            ▼            ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│   Catalog    │ │    Order     │ │     User     │
│   Service    │ │   Service    │ │   Service    │
│  Port 8081   │ │  Port 8082   │ │  Port 8083   │
│              │ │              │ │              │
│  - Books     │ │  - Orders    │ │  - Auth      │
│  - Search    │ │  - Cart      │ │  - Profile   │
│  - Inventory │ │  - Checkout  │ │  - Roles     │
└──────┬───────┘ └──────┬───────┘ └──────┬───────┘
       │                │                │
       │ JDBC           │ MongoDB        │ JDBC
       │                │ Driver         │
       ▼                ▼                ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│    MySQL     │ │   MongoDB    │ │  PostgreSQL  │
│              │ │              │ │              │
│  - books     │ │  - orders    │ │  - users     │
│  - categories│ │  - cart_items│ │  - roles     │
│              │ │              │ │              │
└──────────────┘ └──────────────┘ └──────────────┘

Service-to-Service Communication:
Order Service ──HTTP──> Catalog Service (validate books, check stock)
All Services ──JWT──> User Service (authentication validation)
```

---

## Service Details

### 1. Catalog Service

**Purpose**: Manage book inventory and metadata

**Port**: 8081  
**Database**: MySQL 8.0  
**Base Package**: `com.bookstore.catalog`

#### Responsibilities
- Book CRUD operations (Create, Read, Update, Delete)
- Book search and filtering
- Category management
- Stock level tracking
- ISBN validation

#### Key Endpoints
```
GET    /api/v1/books                # List all books
GET    /api/v1/books/{id}           # Get book details
POST   /api/v1/books                # Create book (admin)
PUT    /api/v1/books/{id}           # Update book (admin)
DELETE /api/v1/books/{id}           # Delete book (admin)
GET    /api/v1/books/search         # Search books
```

#### Database Schema
```sql
CREATE TABLE books (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    isbn VARCHAR(13) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    category VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### Technology Stack
- Spring Boot 2.7.18
- Spring Data JPA
- MySQL Connector
- Hibernate
- Lombok
- Swagger/OpenAPI

---

### 2. Order Service

**Purpose**: Handle order creation and management

**Port**: 8082  
**Database**: MongoDB 6.0  
**Base Package**: `com.bookstore.order`

#### Responsibilities
- Order creation and validation
- Stock validation (via Catalog Service)
- Order status management
- Order history tracking
- Shopping cart management

#### Key Endpoints
```
POST   /api/v1/orders               # Create order
GET    /api/v1/orders               # List all orders
GET    /api/v1/orders/{id}          # Get order details
GET    /api/v1/orders/user/{userId} # Get user orders
PUT    /api/v1/orders/{id}/status   # Update status (admin)
DELETE /api/v1/orders/{id}          # Delete order
```

#### Document Schema
```javascript
{
  "_id": ObjectId,
  "userId": Long,
  "items": [
    {
      "bookId": Long,
      "quantity": Integer,
      "price": Decimal,
      "bookTitle": String,
      "bookIsbn": String
    }
  ],
  "totalAmount": Decimal,
  "status": String, // PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
  "createdAt": ISODate,
  "updatedAt": ISODate
}
```

#### Service Integration
- **Catalog Service**: Validates book existence and stock availability
- **User Service**: Validates JWT authentication

#### Technology Stack
- Spring Boot 2.7.18
- Spring Data MongoDB
- MongoDB Driver
- RestTemplate (for service calls)
- Lombok
- Swagger/OpenAPI

---

### 3. User Service

**Purpose**: User authentication and profile management

**Port**: 8083  
**Database**: PostgreSQL 15  
**Base Package**: `com.bookstore.user`

#### Responsibilities
- User registration
- User authentication (login)
- JWT token generation and validation
- User profile management
- Role-based access control

#### Key Endpoints
```
POST   /api/v1/auth/register        # Register new user
POST   /api/v1/auth/login           # Login (get JWT)
POST   /api/v1/auth/refresh         # Refresh token
GET    /api/v1/users/profile        # Get user profile
PUT    /api/v1/users/profile        # Update profile
```

#### Database Schema
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- BCrypt hashed
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
```

#### Security Features
- BCrypt password hashing
- JWT token generation (HS256 algorithm)
- Token expiration (1 hour access, 7 days refresh)
- Role-based authorization

#### Technology Stack
- Spring Boot 2.7.18
- Spring Data JPA
- Spring Security
- JWT (io.jsonwebtoken)
- PostgreSQL Driver
- BCrypt
- Lombok
- Swagger/OpenAPI

---

## Database Strategy

### Polyglot Persistence

The system uses different databases optimized for different data models:

#### MySQL (Catalog Service)
**Why MySQL?**
- Relational data model fits book catalog structure
- ACID transactions for inventory management
- Strong consistency for stock levels
- Mature indexing for search queries
- Foreign key constraints for data integrity

**Use Cases**:
- Book metadata storage
- Category relationships
- Stock level tracking
- ISBN uniqueness enforcement

---

#### MongoDB (Order Service)
**Why MongoDB?**
- Flexible schema for varying order structures
- Embedded documents for order items
- Fast writes for order creation
- Horizontal scalability for high order volume
- Natural fit for document-based orders

**Use Cases**:
- Order storage with embedded items
- Shopping cart (temporary data)
- Order history (time-series data)
- Flexible order metadata

---

#### PostgreSQL (User Service)
**Why PostgreSQL?**
- ACID compliance for user data
- Strong consistency for authentication
- Advanced indexing for email lookups
- JSON support for flexible user metadata
- Mature security features

**Use Cases**:
- User account storage
- Authentication credentials
- Role management
- User profile data

---

## Communication Patterns

### Synchronous HTTP/REST

All inter-service communication uses synchronous HTTP REST calls.

#### Order Service → Catalog Service

**Purpose**: Validate book availability and fetch current price

**Flow**:
```
1. Client sends order request to Order Service
2. Order Service extracts book IDs from order items
3. For each book ID:
   - Order Service calls GET /api/v1/books/{id} on Catalog Service
   - Catalog Service returns book details (price, stock, title)
   - Order Service validates stock >= requested quantity
4. If all validations pass, Order Service creates order
5. Order Service returns order details to client
```

**Implementation**:
```java
@Service
public class OrderServiceImpl {
    private final RestTemplate restTemplate;
    
    @Value("${catalog.service.url}")
    private String catalogServiceUrl;
    
    private BookDto fetchBookFromCatalog(Long bookId) {
        String url = catalogServiceUrl + "/api/v1/books/" + bookId;
        return restTemplate.getForObject(url, BookDto.class);
    }
}
```

**Error Handling**:
- 404 from Catalog → 400 to client ("Book not found")
- 503 from Catalog → 503 to client ("Service unavailable")
- Timeout → 503 to client

---

### JWT Authentication Flow

**Flow**:
```
1. User registers/logs in via User Service
2. User Service generates JWT token containing:
   - User ID (subject)
   - Email
   - Role (CUSTOMER/ADMIN)
   - Issued at timestamp
   - Expiration timestamp
3. Client stores JWT token
4. Client includes token in Authorization header for protected requests
5. Each service validates JWT signature and expiration
6. Service extracts user info from token claims
7. Service enforces role-based access control
```

**JWT Structure**:
```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user@example.com",
    "userId": 1,
    "role": "CUSTOMER",
    "iat": 1704272400,
    "exp": 1704276000
  },
  "signature": "..."
}
```

---

## Security Architecture

### Authentication

**Mechanism**: JWT (JSON Web Tokens)  
**Algorithm**: HS256 (HMAC with SHA-256)  
**Secret**: Shared across all services (from environment variable)

#### Token Types

1. **Access Token**
   - Expiration: 1 hour
   - Used for API requests
   - Contains user ID, email, role

2. **Refresh Token**
   - Expiration: 7 days
   - Used to obtain new access token
   - Stored securely by client

---

### Authorization

**Mechanism**: Role-Based Access Control (RBAC)

#### Roles

1. **CUSTOMER**
   - Browse books (no auth required)
   - Create orders (auth required)
   - View own orders only
   - Update own profile

2. **ADMIN**
   - All CUSTOMER permissions
   - Create/update/delete books
   - View all orders
   - Update order status
   - View all user profiles

#### Endpoint Security Matrix

| Endpoint | Anonymous | CUSTOMER | ADMIN |
|----------|-----------|----------|-------|
| GET /books | ✓ | ✓ | ✓ |
| POST /books | ✗ | ✗ | ✓ |
| PUT /books/{id} | ✗ | ✗ | ✓ |
| DELETE /books/{id} | ✗ | ✗ | ✓ |
| POST /orders | ✗ | ✓ | ✓ |
| GET /orders | ✗ | ✓ (own) | ✓ (all) |
| PUT /orders/{id}/status | ✗ | ✗ | ✓ |
| GET /users/profile | ✗ | ✓ (own) | ✓ (all) |

---

### Password Security

- **Hashing Algorithm**: BCrypt
- **Work Factor**: 10 rounds
- **Salt**: Automatically generated per password
- **Storage**: Only hash stored, never plain text

```java
@Service
public class UserService {
    private final BCryptPasswordEncoder passwordEncoder;
    
    public void registerUser(UserDto userDto) {
        String hashedPassword = passwordEncoder.encode(userDto.getPassword());
        user.setPassword(hashedPassword);
        // ...
    }
}
```

---

## Data Flow

### Order Creation Flow (Complete)

```
┌──────┐                                                    ┌──────────┐
│Client│                                                    │  User    │
│      │                                                    │ Service  │
└───┬──┘                                                    └────┬─────┘
    │                                                            │
    │ 1. POST /auth/login                                        │
    │───────────────────────────────────────────────────────────>│
    │                                                            │
    │ 2. JWT Token                                               │
    │<───────────────────────────────────────────────────────────│
    │                                                            │
┌───┴──┐                                                    ┌────┴─────┐
│Client│                                                    │ Catalog  │
│      │                                                    │ Service  │
└───┬──┘                                                    └────┬─────┘
    │                                                            │
    │ 3. GET /books                                              │
    │───────────────────────────────────────────────────────────>│
    │                                                            │
    │ 4. Book List                                               │
    │<───────────────────────────────────────────────────────────│
    │                                                            │
┌───┴──┐                                                    ┌────┴─────┐
│Client│                                                    │  Order   │
│      │                                                    │ Service  │
└───┬──┘                                                    └────┬─────┘
    │                                                            │
    │ 5. POST /orders (with JWT)                                 │
    │───────────────────────────────────────────────────────────>│
    │                                                            │
    │                                                       ┌────┴─────┐
    │                                                       │ Validate │
    │                                                       │   JWT    │
    │                                                       └────┬─────┘
    │                                                            │
    │                                                            │ 6. GET /books/{id}
    │                                                            │──────────┐
    │                                                            │          │
    │                                                            │<─────────┘
    │                                                            │ 7. Book Details
    │                                                            │
    │                                                       ┌────┴─────┐
    │                                                       │ Validate │
    │                                                       │  Stock   │
    │                                                       └────┬─────┘
    │                                                            │
    │                                                       ┌────┴─────┐
    │                                                       │Calculate │
    │                                                       │  Total   │
    │                                                       └────┬─────┘
    │                                                            │
    │                                                       ┌────┴─────┐
    │                                                       │  Save    │
    │                                                       │  Order   │
    │                                                       └────┬─────┘
    │                                                            │
    │ 8. Order Created (201)                                     │
    │<───────────────────────────────────────────────────────────│
    │                                                            │
```

---

## Design Decisions

### 1. Why Multi-Module Maven Project?

**Decision**: Use single repository with multiple modules

**Rationale**:
- ✓ Simplified dependency management
- ✓ Shared common code (Dtos, exceptions)
- ✓ Easier local development
- ✓ Consistent versioning
- ✓ Single build command

**Trade-offs**:
- ✗ Tighter coupling than separate repos
- ✗ All services must use same Spring Boot version
- ✓ Acceptable for portfolio/learning project

---

### 2. Why Synchronous HTTP Communication?

**Decision**: Use REST/HTTP instead of message queues

**Rationale**:
- ✓ Simpler to implement and understand
- ✓ Immediate feedback (request-response)
- ✓ Easier debugging and testing
- ✓ No message broker infrastructure needed
- ✓ Sufficient for current scale

**Trade-offs**:
- ✗ Tight coupling between services
- ✗ Cascading failures possible
- ✗ No built-in retry mechanism
- ✓ Can add message queue later if needed

---

### 3. Why Shared Dto Module?

**Decision**: Create `shared-common` module for Dtos

**Rationale**:
- ✓ Avoid code duplication
- ✓ Consistent data contracts
- ✓ Easier refactoring
- ✓ Single source of truth

**Trade-offs**:
- ✗ Services share dependency
- ✗ Changes affect multiple services
- ✓ Acceptable for tightly integrated services

---

### 4. Why JWT for Authentication?

**Decision**: Use JWT instead of session-based auth

**Rationale**:
- ✓ Stateless (no session storage needed)
- ✓ Scalable (no session replication)
- ✓ Works across services
- ✓ Self-contained (includes user info)
- ✓ Industry standard

**Trade-offs**:
- ✗ Cannot revoke tokens before expiration
- ✗ Larger than session IDs
- ✓ Refresh token pattern mitigates revocation issue

---

### 5. Why Docker Compose?

**Decision**: Use Docker Compose for orchestration

**Rationale**:
- ✓ Simple local development setup
- ✓ Consistent environments
- ✓ Easy to start/stop all services
- ✓ Built-in networking
- ✓ Volume management for databases

**Trade-offs**:
- ✗ Not suitable for production
- ✗ No auto-scaling
- ✓ Kubernetes would be used for production

---

## Scalability Considerations

### Current Limitations

1. **Single Instance**: Each service runs as single container
2. **No Load Balancing**: Direct service-to-service calls
3. **No Service Discovery**: Hardcoded service URLs
4. **No Circuit Breaker**: Cascading failures possible
5. **Synchronous Communication**: Blocking calls

### Scaling Strategies

#### Horizontal Scaling
```yaml
# docker-compose.yml (future)
catalog-service:
  deploy:
    replicas: 3
  
order-service:
  deploy:
    replicas: 5  # More instances for high order volume
```

#### Database Scaling

**MySQL (Catalog)**:
- Read replicas for search queries
- Master-slave replication
- Connection pooling

**MongoDB (Order)**:
- Sharding by user ID
- Replica sets for high availability
- Indexes on frequently queried fields

**PostgreSQL (User)**:
- Read replicas for profile lookups
- Connection pooling
- Caching layer (Redis)

---

## Future Enhancements

### Phase 2: Advanced Microservices Patterns

#### 1. API Gateway
```
┌─────────────────┐
│   API Gateway   │
│   Port 8080     │
│                 │
│  - Routing      │
│  - Auth         │
│  - Rate Limit   │
└────────┬────────┘
         │
    ┌────┼────┐
    ▼    ▼    ▼
  Services
```

**Benefits**:
- Single entry point
- Centralized authentication
- Request routing
- Load balancing
- Rate limiting

**Technology**: Spring Cloud Gateway

---

#### 2. Service Discovery
```
┌──────────────┐
│    Eureka    │
│    Server    │
└──────┬───────┘
       │
   ┌───┴───┐
   ▼       ▼
Services register
and discover each other
```

**Benefits**:
- Dynamic service registration
- Client-side load balancing
- Health checking
- No hardcoded URLs

**Technology**: Netflix Eureka

---

#### 3. Circuit Breaker
```java
@CircuitBreaker(name = "catalogService", fallbackMethod = "fallbackMethod")
public BookDto fetchBook(Long id) {
    return restTemplate.getForObject(url, BookDto.class);
}

public BookDto fallbackMethod(Long id, Exception e) {
    return BookDto.builder()
        .id(id)
        .title("Unavailable")
        .build();
}
```

**Benefits**:
- Prevent cascading failures
- Graceful degradation
- Automatic recovery
- Fallback responses

**Technology**: Resilience4j

---

#### 4. Distributed Tracing
```
Request ID: abc123
├─ User Service (50ms)
├─ Order Service (200ms)
│  └─ Catalog Service (150ms)
└─ Total: 250ms
```

**Benefits**:
- Track requests across services
- Performance monitoring
- Bottleneck identification
- Debugging distributed systems

**Technology**: Spring Cloud Sleuth + Zipkin

---

#### 5. Asynchronous Communication
```
Order Service ──(OrderCreated)──> Message Queue
                                       │
                                       ├──> Notification Service
                                       ├──> Inventory Service
                                       └──> Analytics Service
```

**Benefits**:
- Loose coupling
- Better fault tolerance
- Event-driven architecture
- Scalable processing

**Technology**: RabbitMQ or Apache Kafka

---

#### 6. Caching Layer
```
Client ──> API Gateway ──> Redis Cache ──> Services
                              │
                              └──> Cache Hit (fast)
                              └──> Cache Miss (fetch from service)
```

**Benefits**:
- Reduced database load
- Faster response times
- Lower latency
- Cost savings

**Technology**: Redis

---

### Phase 3: UI Development

#### Admin Panel (Thymeleaf + Tailwind CSS)
- Book management interface
- Order dashboard
- User management
- Analytics

#### Customer Frontend (Angular SPA)
- Book browsing
- Shopping cart
- Order history
- User profile

---

## Technology Stack Summary

### Backend
- **Java**: 11
- **Spring Boot**: 2.7.18
- **Spring Data JPA**: For MySQL, PostgreSQL
- **Spring Data MongoDB**: For MongoDB
- **Spring Security**: Authentication & Authorization
- **JWT**: io.jsonwebtoken
- **Lombok**: Reduce boilerplate
- **Swagger/OpenAPI**: API documentation

### Databases
- **MySQL**: 8.0 (Catalog)
- **MongoDB**: 6.0 (Order)
- **PostgreSQL**: 15 (User)

### Build & Deploy
- **Maven**: 3.8+
- **Docker**: 20.10+
- **Docker Compose**: 3.8

### Testing
- **JUnit**: 5
- **Mockito**: Mocking framework
- **TestRestTemplate**: Integration tests
- **curl**: Manual API testing

---

## Performance Metrics

### Target Response Times
- Book list: < 200ms
- Book search: < 300ms
- Order creation: < 500ms (includes Catalog call)
- User login: < 100ms
- JWT validation: < 10ms

### Database Query Performance
- Book by ID: < 10ms (indexed)
- User by email: < 5ms (indexed)
- Order by ID: < 20ms (MongoDB)

---

## Monitoring & Observability

### Health Checks
Each service exposes:
- `/actuator/health` - Overall health status
- `/actuator/info` - Service information

### Logging
- **Format**: JSON structured logs
- **Levels**: DEBUG (dev), INFO (prod)
- **Correlation**: Request ID for tracing

### Metrics (Future)
- Request count
- Response time
- Error rate
- Database connection pool

**Technology**: Spring Boot Actuator + Prometheus + Grafana

---

## Conclusion

This architecture demonstrates modern microservices patterns suitable for a portfolio project while maintaining simplicity and clarity. The design prioritizes:

1. **Learning**: Clear separation of concerns
2. **Scalability**: Horizontal scaling ready
3. **Maintainability**: Clean code, good documentation
4. **Extensibility**: Easy to add new features
5. **Industry Relevance**: Uses current best practices

The system is production-ready for small to medium scale and has a clear path for enterprise-grade enhancements.

---

**Last Updated**: January 3, 2026  
**Version**: 1.0.0  
**Author**: Portfolio Project
