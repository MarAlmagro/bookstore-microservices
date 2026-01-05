# Service Integration Test Scenarios

This document describes how microservices communicate and integrate with each other.

---

## Overview

The bookstore microservices architecture consists of three independent services that communicate via HTTP REST APIs:

```
┌─────────────────┐
│  User Service   │
│   Port 8083     │
│  (PostgreSQL)   │
└────────┬────────┘
         │
         │ JWT Authentication
         │
         ▼
┌─────────────────┐      ┌─────────────────┐
│  Order Service  │◄────►│ Catalog Service │
│   Port 8082     │      │   Port 8081     │
│   (MongoDB)     │      │    (MySQL)      │
└─────────────────┘      └─────────────────┘
```

---

## Integration Point 1: Order Service ↔ Catalog Service

### Purpose
When creating an order, the Order Service must validate that:
1. The requested books exist
2. Sufficient stock is available
3. Current prices are accurate

### Communication Flow

```
Client → Order Service → Catalog Service
                      ← Book Details
       ← Order Created
```

### Test Scenario: Successful Order Creation with Stock Validation

#### Step 1: Check Available Books
**Service**: Catalog Service  
**Endpoint**: `GET /api/v1/books/1`

**Response**:
```json
{
  "id": 1,
  "isbn": "9780134685991",
  "title": "Effective Java",
  "author": "Joshua Bloch",
  "price": 45.99,
  "stock": 100,
  "category": "Programming"
}
```

**Validation**:
- ✓ Book exists
- ✓ Stock: 100 units available

---

#### Step 2: Create Order
**Service**: Order Service  
**Endpoint**: `POST /api/v1/orders`

**Request**:
```json
{
  "userId": 1,
  "items": [
    {
      "bookId": 1,
      "quantity": 2
    }
  ]
}
```

**Internal Process**:
1. Order Service receives request
2. Order Service calls Catalog Service: `GET /api/v1/books/1`
3. Catalog Service returns book details
4. Order Service validates:
   - Book exists ✓
   - Stock (100) >= Quantity (2) ✓
5. Order Service creates order with current price

**Response**:
```json
{
  "id": "65a1b2c3d4e5f6g7h8i9j0k1",
  "userId": 1,
  "items": [
    {
      "bookId": 1,
      "bookTitle": "Effective Java",
      "quantity": 2,
      "price": 45.99,
      "subtotal": 91.98
    }
  ],
  "totalAmount": 91.98,
  "status": "PENDING",
  "createdAt": "2026-01-03T10:15:00"
}
```

**Validation**:
- ✓ Order created successfully
- ✓ Book title fetched from Catalog Service
- ✓ Current price used (45.99)
- ✓ Total calculated correctly (45.99 × 2 = 91.98)

---

### Test Scenario: Order Rejected - Book Not Found

#### Step 1: Attempt Order with Invalid Book ID
**Service**: Order Service  
**Endpoint**: `POST /api/v1/orders`

**Request**:
```json
{
  "userId": 1,
  "items": [
    {
      "bookId": 99999,
      "quantity": 1
    }
  ]
}
```

**Internal Process**:
1. Order Service receives request
2. Order Service calls Catalog Service: `GET /api/v1/books/99999`
3. Catalog Service returns 404 Not Found
4. Order Service rejects order creation

**Response** (404 Not Found):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Book not found with id: 99999",
  "path": "/api/v1/orders"
}
```

**Validation**:
- ✓ Order Service validates with Catalog Service
- ✓ Invalid book IDs are rejected
- ✓ No partial order created
- ✓ Error propagated to client

---

### Test Scenario: Order Rejected - Insufficient Stock

#### Step 1: Check Current Stock
**Service**: Catalog Service  
**Endpoint**: `GET /api/v1/books/1`

**Response**:
```json
{
  "id": 1,
  "title": "Effective Java",
  "stock": 5
}
```

---

#### Step 2: Attempt Order Exceeding Stock
**Service**: Order Service  
**Endpoint**: `POST /api/v1/orders`

**Request**:
```json
{
  "userId": 1,
  "items": [
    {
      "bookId": 1,
      "quantity": 10
    }
  ]
}
```

**Internal Process**:
1. Order Service calls Catalog Service
2. Catalog Service returns stock: 5
3. Order Service compares: 5 < 10
4. Order Service rejects order

**Response** (400 Bad Request):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient stock for book: Effective Java. Available: 5, Requested: 10",
  "path": "/api/v1/orders"
}
```

**Validation**:
- ✓ Stock validation enforced
- ✓ Business rule prevents overselling
- ✓ Clear error message with details

---

### Test Scenario: Multiple Books in Single Order

**Request**:
```json
{
  "userId": 1,
  "items": [
    {
      "bookId": 1,
      "quantity": 2
    },
    {
      "bookId": 2,
      "quantity": 1
    },
    {
      "bookId": 3,
      "quantity": 3
    }
  ]
}
```

**Internal Process**:
1. Order Service validates each book:
   - Calls `GET /api/v1/books/1`
   - Calls `GET /api/v1/books/2`
   - Calls `GET /api/v1/books/3`
2. Validates stock for each item
3. Calculates total amount
4. Creates order

**Expected Behavior**:
- ✓ All books validated before order creation
- ✓ If any book fails validation, entire order rejected
- ✓ Atomic operation (all or nothing)

---

## Integration Point 2: All Services ↔ User Service (Authentication)

### Purpose
All protected endpoints require JWT authentication provided by the User Service.

### Communication Flow

```
Client → User Service (Login)
       ← JWT Token

Client → Catalog/Order Service (with JWT)
       → Validate JWT
       ← Protected Resource
```

### Test Scenario: Authenticated Order Creation

#### Step 1: Login
**Service**: User Service  
**Endpoint**: `POST /api/v1/auth/login`

**Request**:
```json
{
  "email": "john.doe@example.com",
  "password": "SecurePass123!"
}
```

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsInJvbGUiOiJDVVNUT01FUiIsImlhdCI6MTcwNDI3MjQwMCwiZXhwIjoxNzA0Mjc2MDAwfQ.signature",
  "type": "Bearer",
  "expiresIn": 3600
}
```

**JWT Payload** (decoded):
```json
{
  "sub": "1",
  "email": "john.doe@example.com",
  "role": "CUSTOMER",
  "iat": 1704272400,
  "exp": 1704276000
}
```

---

#### Step 2: Create Order with JWT
**Service**: Order Service  
**Endpoint**: `POST /api/v1/orders`

**Headers**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Request**:
```json
{
  "userId": 1,
  "items": [
    {
      "bookId": 1,
      "quantity": 2
    }
  ]
}
```

**Internal Process**:
1. Order Service extracts JWT from Authorization header
2. Order Service validates JWT signature
3. Order Service extracts user info from JWT
4. Order Service verifies userId matches JWT subject
5. Order Service processes order

**Validation**:
- ✓ JWT signature is valid
- ✓ JWT is not expired
- ✓ User ID matches authenticated user
- ✓ Order created successfully

---

### Test Scenario: Unauthorized Access

#### Attempt Order Without JWT
**Service**: Order Service  
**Endpoint**: `POST /api/v1/orders`

**Headers**: None

**Response** (401 Unauthorized):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Missing or invalid authentication token",
  "path": "/api/v1/orders"
}
```

**Validation**:
- ✓ Protected endpoints require authentication
- ✓ Requests without JWT are rejected

---

## Integration Point 3: Role-Based Access Control

### Purpose
Different user roles have different permissions across services.

### Role Matrix

| Endpoint | Anonymous | CUSTOMER | ADMIN |
|----------|-----------|----------|-------|
| GET /books | ✓ | ✓ | ✓ |
| POST /books | ✗ | ✗ | ✓ |
| PUT /books/{id} | ✗ | ✗ | ✓ |
| DELETE /books/{id} | ✗ | ✗ | ✓ |
| POST /orders | ✗ | ✓ | ✓ |
| GET /orders | ✗ | ✓ (own) | ✓ (all) |
| PUT /orders/{id}/status | ✗ | ✗ | ✓ |

### Test Scenario: Customer Attempts Admin Operation

#### Step 1: Login as Customer
**Service**: User Service

**Response**:
```json
{
  "token": "customer.jwt.token",
  "user": {
    "role": "CUSTOMER"
  }
}
```

---

#### Step 2: Attempt to Create Book
**Service**: Catalog Service  
**Endpoint**: `POST /api/v1/books`

**Headers**:
```
Authorization: Bearer customer.jwt.token
```

**Response** (403 Forbidden):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied. Admin role required.",
  "path": "/api/v1/books"
}
```

**Validation**:
- ✓ JWT is valid (authenticated)
- ✓ Role is CUSTOMER (insufficient permissions)
- ✓ Operation rejected with 403 Forbidden

---

### Test Scenario: Admin Access to All Orders

#### Step 1: Login as Admin
**Service**: User Service

**Response**:
```json
{
  "token": "admin.jwt.token",
  "user": {
    "role": "ADMIN"
  }
}
```

---

#### Step 2: Get All Orders
**Service**: Order Service  
**Endpoint**: `GET /api/v1/orders`

**Headers**:
```
Authorization: Bearer admin.jwt.token
```

**Response**:
```json
[
  {
    "id": "order1",
    "userId": 1,
    "totalAmount": 91.98,
    "status": "PENDING"
  },
  {
    "id": "order2",
    "userId": 2,
    "totalAmount": 45.99,
    "status": "SHIPPED"
  }
]
```

**Validation**:
- ✓ Admin can view all orders (not just their own)
- ✓ Orders from multiple users returned

---

## Integration Point 4: Error Propagation

### Test Scenario: Catalog Service Unavailable

#### Simulate Catalog Service Down
```bash
# Stop catalog service
docker-compose stop catalog-service
```

---

#### Attempt Order Creation
**Service**: Order Service  
**Endpoint**: `POST /api/v1/orders`

**Request**:
```json
{
  "userId": 1,
  "items": [
    {
      "bookId": 1,
      "quantity": 2
    }
  ]
}
```

**Expected Response** (503 Service Unavailable):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 503,
  "error": "Service Unavailable",
  "message": "Catalog service is currently unavailable. Please try again later.",
  "path": "/api/v1/orders"
}
```

**Validation**:
- ✓ Order Service detects Catalog Service failure
- ✓ Returns 503 status code
- ✓ No partial order created
- ✓ Clear error message for client

---

## End-to-End Integration Test

### Complete User Journey

This test validates all integration points in a single workflow.

#### 1. User Registration
**Service**: User Service  
**Result**: User created with ID and CUSTOMER role

---

#### 2. User Login
**Service**: User Service  
**Result**: JWT token obtained

---

#### 3. Browse Books
**Service**: Catalog Service  
**Result**: List of available books

---

#### 4. Search Books
**Service**: Catalog Service  
**Result**: Filtered book list

---

#### 5. Get Book Details
**Service**: Catalog Service  
**Result**: Detailed book information with stock

---

#### 6. Create Order
**Service**: Order Service  
**Integration**: 
- Validates JWT with User Service
- Validates books with Catalog Service
- Checks stock availability

**Result**: Order created with accurate pricing

---

#### 7. View Order
**Service**: Order Service  
**Integration**: JWT validation  
**Result**: Order details returned

---

#### 8. Admin Updates Order Status
**Service**: Order Service  
**Integration**: 
- Validates admin JWT
- Enforces role-based access

**Result**: Order status updated to SHIPPED

---

## Testing Service Integration

### Using the Integration Test Script

```bash
# Run complete E2E integration test
./scripts/test-integration.sh
```

This script tests:
- ✓ All services are running
- ✓ User registration and authentication
- ✓ Book browsing and search
- ✓ Order creation with stock validation
- ✓ Service-to-service communication
- ✓ JWT authentication across services
- ✓ Role-based access control

### Expected Output

```
=========================================
Integration Test Suite - E2E Workflow
=========================================

Step 1: Verify All Services are Running
✓ Catalog Service is running (Port 8081)
✓ Order Service is running (Port 8082)
✓ User Service is running (Port 8083)

Step 2: Register a New User
✓ User registered successfully

Step 3: Login and Get JWT Token
✓ JWT Token obtained

Step 4: Browse Books
Total books available: 15

Step 5: Get Book Details
Book: Effective Java
Price: $45.99
Stock: 100

Step 6: Search Books by Category
Found 8 Programming books

Step 7: Create an Order
✓ Order created successfully!
Order ID: 65a1b2c3d4e5f6g7h8i9j0k1

Step 8: Verify Order Details
Order Status: PENDING
Order Total: $91.98

Step 9: Update Order Status
✓ Order status updated

=========================================
E2E Integration Test Completed
=========================================
```

---

## Integration Testing Checklist

- [ ] Order Service validates books with Catalog Service
- [ ] Order Service checks stock availability
- [ ] Order Service uses current prices from Catalog Service
- [ ] All services validate JWT tokens
- [ ] Role-based access control enforced across services
- [ ] Error responses propagate correctly
- [ ] Service unavailability handled gracefully
- [ ] Multiple books in single order validated atomically
- [ ] User can only access their own orders
- [ ] Admin can access all orders
- [ ] JWT expiration enforced
- [ ] Invalid JWT tokens rejected

---

## Troubleshooting Integration Issues

### Issue: Order Service Cannot Reach Catalog Service

**Symptoms**:
- 503 Service Unavailable errors
- Connection refused errors

**Solutions**:
1. Verify Catalog Service is running: `docker-compose ps`
2. Check service URLs in Order Service configuration
3. Verify network connectivity: `docker network ls`
4. Check logs: `docker-compose logs catalog-service`

---

### Issue: JWT Validation Fails

**Symptoms**:
- 401 Unauthorized errors
- "Invalid token" messages

**Solutions**:
1. Verify JWT_SECRET is same across all services
2. Check token expiration time
3. Ensure Authorization header format: `Bearer {token}`
4. Verify token is not corrupted during transmission

---

### Issue: Stock Validation Not Working

**Symptoms**:
- Orders created despite insufficient stock
- Stock levels not checked

**Solutions**:
1. Verify Order Service is calling Catalog Service
2. Check RestTemplate configuration
3. Review Order Service logs for HTTP calls
4. Test Catalog Service endpoint directly

---

## Performance Considerations

### Service-to-Service Call Latency

Each order creation involves:
1. JWT validation (local)
2. N × Catalog Service calls (where N = number of unique books)
3. Database writes

**Optimization Strategies**:
- Batch book validation calls
- Cache book details (with TTL)
- Implement circuit breaker for resilience
- Use async communication for non-critical operations

---

## Future Enhancements

### Planned Integration Improvements

1. **Circuit Breaker Pattern** (Resilience4j)
   - Prevent cascading failures
   - Fallback mechanisms

2. **Service Discovery** (Eureka)
   - Dynamic service registration
   - Load balancing

3. **API Gateway** (Spring Cloud Gateway)
   - Single entry point
   - Centralized authentication

4. **Distributed Tracing** (Sleuth + Zipkin)
   - Track requests across services
   - Performance monitoring

5. **Message Queue** (RabbitMQ/Kafka)
   - Async communication
   - Event-driven architecture
