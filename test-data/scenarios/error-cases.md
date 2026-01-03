# Error Cases Test Scenarios

This document describes error handling and validation scenarios across all microservices.

---

## User Service Error Cases

### 1. Registration Errors

#### 1.1 Invalid Email Format
**Endpoint**: `POST /api/v1/auth/register`

**Request**:
```json
{
  "email": "invalid-email",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Expected Response** (400 Bad Request):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid email format",
  "path": "/api/v1/auth/register"
}
```

**Validation**:
- ✓ HTTP 400 status code
- ✓ Clear error message
- ✓ Email validation enforced

---

#### 1.2 Weak Password
**Endpoint**: `POST /api/v1/auth/register`

**Request**:
```json
{
  "email": "weak@example.com",
  "password": "123",
  "firstName": "Weak",
  "lastName": "Password"
}
```

**Expected Response** (400 Bad Request):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Password must be at least 8 characters long",
  "path": "/api/v1/auth/register"
}
```

**Validation**:
- ✓ Password strength validation
- ✓ Minimum length enforced

---

#### 1.3 Missing Required Fields
**Endpoint**: `POST /api/v1/auth/register`

**Request**:
```json
{
  "email": "missing@example.com",
  "password": "SecurePass123!"
}
```

**Expected Response** (400 Bad Request):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "firstName is required; lastName is required",
  "path": "/api/v1/auth/register"
}
```

**Validation**:
- ✓ All required fields validated
- ✓ Multiple validation errors reported

---

#### 1.4 Duplicate Email
**Endpoint**: `POST /api/v1/auth/register`

**Request**:
```json
{
  "email": "existing@example.com",
  "password": "SecurePass123!",
  "firstName": "Duplicate",
  "lastName": "User"
}
```

**Expected Response** (409 Conflict):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Email already exists",
  "path": "/api/v1/auth/register"
}
```

**Validation**:
- ✓ HTTP 409 status code
- ✓ Unique email constraint enforced

---

### 2. Login Errors

#### 2.1 Invalid Credentials
**Endpoint**: `POST /api/v1/auth/login`

**Request**:
```json
{
  "email": "john.doe@example.com",
  "password": "WrongPassword"
}
```

**Expected Response** (401 Unauthorized):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/api/v1/auth/login"
}
```

**Validation**:
- ✓ HTTP 401 status code
- ✓ Generic error message (security best practice)
- ✓ No indication whether email or password is wrong

---

#### 2.2 Non-Existent User
**Endpoint**: `POST /api/v1/auth/login`

**Request**:
```json
{
  "email": "nonexistent@example.com",
  "password": "SomePassword123"
}
```

**Expected Response** (401 Unauthorized):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/api/v1/auth/login"
}
```

**Validation**:
- ✓ Same response as invalid credentials
- ✓ Prevents user enumeration

---

### 3. Authentication Errors

#### 3.1 Missing JWT Token
**Endpoint**: `GET /api/v1/users/profile`

**Headers**: None

**Expected Response** (401 Unauthorized):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Missing or invalid authentication token",
  "path": "/api/v1/users/profile"
}
```

**Validation**:
- ✓ Protected endpoints require authentication
- ✓ Clear error message

---

#### 3.2 Invalid/Expired JWT Token
**Endpoint**: `GET /api/v1/users/profile`

**Headers**:
```
Authorization: Bearer invalid.jwt.token
```

**Expected Response** (401 Unauthorized):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired token",
  "path": "/api/v1/users/profile"
}
```

**Validation**:
- ✓ Token validation enforced
- ✓ Expired tokens rejected

---

## Catalog Service Error Cases

### 1. Book Creation Errors

#### 1.1 Missing Required Fields
**Endpoint**: `POST /api/v1/books`

**Headers**:
```
Authorization: Bearer {ADMIN_JWT_TOKEN}
Content-Type: application/json
```

**Request**:
```json
{
  "isbn": "",
  "title": "",
  "author": "Test Author",
  "price": 45.99
}
```

**Expected Response** (400 Bad Request):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "isbn is required; title is required",
  "path": "/api/v1/books"
}
```

**Validation**:
- ✓ Field validation enforced
- ✓ Multiple errors reported

---

#### 1.2 Invalid Price/Stock
**Endpoint**: `POST /api/v1/books`

**Request**:
```json
{
  "isbn": "9780134685991",
  "title": "Test Book",
  "author": "Test Author",
  "price": -10.00,
  "stock": -5,
  "category": "Test"
}
```

**Expected Response** (400 Bad Request):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "price must be positive; stock must be non-negative",
  "path": "/api/v1/books"
}
```

**Validation**:
- ✓ Business rule validation
- ✓ Negative values rejected

---

#### 1.3 Duplicate ISBN
**Endpoint**: `POST /api/v1/books`

**Request**:
```json
{
  "isbn": "9780134685991",
  "title": "Duplicate ISBN Book",
  "author": "Test Author",
  "price": 45.99,
  "stock": 100,
  "category": "Test"
}
```

**Expected Response** (409 Conflict):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Book with ISBN 9780134685991 already exists",
  "path": "/api/v1/books"
}
```

**Validation**:
- ✓ Unique ISBN constraint
- ✓ HTTP 409 status code

---

#### 1.4 Unauthorized Book Creation (Customer Role)
**Endpoint**: `POST /api/v1/books`

**Headers**:
```
Authorization: Bearer {CUSTOMER_JWT_TOKEN}
Content-Type: application/json
```

**Request**:
```json
{
  "isbn": "9780134685991",
  "title": "Unauthorized Book",
  "author": "Test Author",
  "price": 45.99,
  "stock": 100,
  "category": "Test"
}
```

**Expected Response** (403 Forbidden):
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
- ✓ Role-based access control enforced
- ✓ HTTP 403 status code

---

### 2. Book Retrieval Errors

#### 2.1 Non-Existent Book ID
**Endpoint**: `GET /api/v1/books/99999`

**Expected Response** (404 Not Found):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Book not found with id: 99999",
  "path": "/api/v1/books/99999"
}
```

**Validation**:
- ✓ HTTP 404 status code
- ✓ Clear error message with ID

---

#### 2.2 Invalid Book ID Format
**Endpoint**: `GET /api/v1/books/invalid-id`

**Expected Response** (400 Bad Request):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid book ID format",
  "path": "/api/v1/books/invalid-id"
}
```

**Validation**:
- ✓ Input validation
- ✓ Type checking

---

### 3. Search Errors

#### 3.1 Invalid Price Range
**Endpoint**: `GET /api/v1/books/search?minPrice=100&maxPrice=50`

**Expected Response** (400 Bad Request):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "minPrice cannot be greater than maxPrice",
  "path": "/api/v1/books/search"
}
```

**Validation**:
- ✓ Business logic validation
- ✓ Parameter validation

---

## Order Service Error Cases

### 1. Order Creation Errors

#### 1.1 Unauthenticated Order Creation
**Endpoint**: `POST /api/v1/orders`

**Headers**: None

**Request**:
```json
{
  "userId": 1,
  "items": [
    {
      "bookId": 1,
      "quantity": 1
    }
  ]
}
```

**Expected Response** (401 Unauthorized):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required",
  "path": "/api/v1/orders"
}
```

**Validation**:
- ✓ Authentication enforced
- ✓ HTTP 401 status code

---

#### 1.2 Invalid Book ID in Order
**Endpoint**: `POST /api/v1/orders`

**Headers**:
```
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

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

**Expected Response** (404 Not Found):
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
- ✓ Service-to-service validation
- ✓ Order Service validates with Catalog Service
- ✓ Invalid book IDs rejected

---

#### 1.3 Insufficient Stock
**Endpoint**: `POST /api/v1/orders`

**Request**:
```json
{
  "userId": 1,
  "items": [
    {
      "bookId": 1,
      "quantity": 10000
    }
  ]
}
```

**Expected Response** (400 Bad Request):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient stock for book: Effective Java. Available: 100, Requested: 10000",
  "path": "/api/v1/orders"
}
```

**Validation**:
- ✓ Stock validation via Catalog Service
- ✓ Business rule enforced
- ✓ Clear error message with details

---

#### 1.4 Invalid Quantity
**Endpoint**: `POST /api/v1/orders`

**Request**:
```json
{
  "userId": 1,
  "items": [
    {
      "bookId": 1,
      "quantity": -5
    }
  ]
}
```

**Expected Response** (400 Bad Request):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "quantity must be positive",
  "path": "/api/v1/orders"
}
```

**Validation**:
- ✓ Input validation
- ✓ Negative quantities rejected

---

#### 1.5 Empty Order Items
**Endpoint**: `POST /api/v1/orders`

**Request**:
```json
{
  "userId": 1,
  "items": []
}
```

**Expected Response** (400 Bad Request):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Order must contain at least one item",
  "path": "/api/v1/orders"
}
```

**Validation**:
- ✓ Business rule validation
- ✓ Empty orders rejected

---

#### 1.6 Missing User ID
**Endpoint**: `POST /api/v1/orders`

**Request**:
```json
{
  "items": [
    {
      "bookId": 1,
      "quantity": 1
    }
  ]
}
```

**Expected Response** (400 Bad Request):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "userId is required",
  "path": "/api/v1/orders"
}
```

**Validation**:
- ✓ Required field validation

---

### 2. Order Retrieval Errors

#### 2.1 Non-Existent Order ID
**Endpoint**: `GET /api/v1/orders/99999999999999999999`

**Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Expected Response** (404 Not Found):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Order not found with id: 99999999999999999999",
  "path": "/api/v1/orders/99999999999999999999"
}
```

**Validation**:
- ✓ HTTP 404 status code
- ✓ Clear error message

---

#### 2.2 Unauthorized Order Access
**Endpoint**: `GET /api/v1/orders/{otherUserOrderId}`

**Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Expected Response** (403 Forbidden):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied. You can only view your own orders.",
  "path": "/api/v1/orders/{otherUserOrderId}"
}
```

**Validation**:
- ✓ User can only access their own orders
- ✓ HTTP 403 status code

---

### 3. Order Status Update Errors

#### 3.1 Invalid Order Status
**Endpoint**: `PUT /api/v1/orders/{orderId}/status`

**Headers**:
```
Authorization: Bearer {ADMIN_JWT_TOKEN}
Content-Type: application/json
```

**Request**:
```json
{
  "status": "INVALID_STATUS"
}
```

**Expected Response** (400 Bad Request):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid order status. Valid values: PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED",
  "path": "/api/v1/orders/{orderId}/status"
}
```

**Validation**:
- ✓ Enum validation
- ✓ Only valid statuses accepted

---

#### 3.2 Unauthorized Status Update (Customer)
**Endpoint**: `PUT /api/v1/orders/{orderId}/status`

**Headers**:
```
Authorization: Bearer {CUSTOMER_JWT_TOKEN}
Content-Type: application/json
```

**Request**:
```json
{
  "status": "SHIPPED"
}
```

**Expected Response** (403 Forbidden):
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied. Admin role required.",
  "path": "/api/v1/orders/{orderId}/status"
}
```

**Validation**:
- ✓ Only admins can update order status
- ✓ Role-based access control

---

## Service Availability Errors

### 1. Catalog Service Down (Order Creation)
**Scenario**: Order Service attempts to validate book, but Catalog Service is unavailable

**Expected Behavior**:
- ✓ Order Service returns 503 Service Unavailable
- ✓ Error message indicates external service failure
- ✓ No partial order created

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

---

## Error Response Standards

All error responses should follow this format:

```json
{
  "timestamp": "ISO-8601 timestamp",
  "status": "HTTP status code",
  "error": "HTTP status text",
  "message": "Human-readable error description",
  "path": "Request path that caused the error"
}
```

### HTTP Status Code Guidelines

- **400 Bad Request**: Invalid input, validation errors
- **401 Unauthorized**: Missing or invalid authentication
- **403 Forbidden**: Authenticated but insufficient permissions
- **404 Not Found**: Resource does not exist
- **409 Conflict**: Duplicate resource (e.g., email, ISBN)
- **500 Internal Server Error**: Unexpected server error
- **503 Service Unavailable**: External service dependency failure

---

## Testing Error Cases

Use the provided test scripts:
```bash
# Test all error cases
./scripts/test-catalog.sh  # Includes validation errors
./scripts/test-user.sh     # Includes auth errors
./scripts/test-order.sh    # Includes business logic errors
```

Each script includes both happy path and error case tests.
