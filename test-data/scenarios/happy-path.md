# Happy Path Test Scenarios

This document describes the successful user journeys through the bookstore microservices system.

---

## Scenario 1: New Customer Registration and First Purchase

### Overview
A new customer discovers the bookstore, registers an account, browses books, and makes their first purchase.

### Prerequisites
- All services running (Catalog, Order, User)
- Sample books loaded in catalog database

### Test Steps

#### 1. User Registration
**Endpoint**: `POST /api/v1/auth/register`  
**Service**: User Service (Port 8083)

**Request**:
```json
{
  "email": "john.doe@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Expected Response** (201 Created):
```json
{
  "id": 1,
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "CUSTOMER",
  "createdAt": "2026-01-03T10:00:00"
}
```

**Validation**:
- ✓ User ID is generated
- ✓ Password is hashed (not returned in response)
- ✓ Default role is CUSTOMER
- ✓ Email is unique and valid format

---

#### 2. User Login
**Endpoint**: `POST /api/v1/auth/login`  
**Service**: User Service (Port 8083)

**Request**:
```json
{
  "email": "john.doe@example.com",
  "password": "SecurePass123!"
}
```

**Expected Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": 1,
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "CUSTOMER"
  }
}
```

**Validation**:
- ✓ JWT token is returned
- ✓ Token contains user information
- ✓ Token expiry is set (1 hour)
- ✓ User details are included

**Save for Next Steps**: Store JWT token for authenticated requests

---

#### 3. Browse All Books
**Endpoint**: `GET /api/v1/books`  
**Service**: Catalog Service (Port 8081)

**Request**: No body (GET request)

**Expected Response** (200 OK):
```json
[
  {
    "id": 1,
    "isbn": "9780134685991",
    "title": "Effective Java",
    "author": "Joshua Bloch",
    "description": "Best practices for the Java programming language",
    "price": 45.99,
    "stock": 100,
    "category": "Programming"
  },
  {
    "id": 2,
    "isbn": "9780596009205",
    "title": "Head First Design Patterns",
    "author": "Eric Freeman",
    "description": "A brain-friendly guide to design patterns",
    "price": 39.99,
    "stock": 75,
    "category": "Programming"
  }
]
```

**Validation**:
- ✓ Array of books is returned
- ✓ Each book has all required fields
- ✓ Stock levels are visible
- ✓ No authentication required for browsing

---

#### 4. Search Books by Category
**Endpoint**: `GET /api/v1/books/search?category=Programming`  
**Service**: Catalog Service (Port 8081)

**Expected Response** (200 OK):
```json
[
  {
    "id": 1,
    "isbn": "9780134685991",
    "title": "Effective Java",
    "author": "Joshua Bloch",
    "price": 45.99,
    "stock": 100,
    "category": "Programming"
  }
]
```

**Validation**:
- ✓ Only books matching category are returned
- ✓ Search is case-insensitive
- ✓ Empty array if no matches

---

#### 5. Get Book Details
**Endpoint**: `GET /api/v1/books/1`  
**Service**: Catalog Service (Port 8081)

**Expected Response** (200 OK):
```json
{
  "id": 1,
  "isbn": "9780134685991",
  "title": "Effective Java",
  "author": "Joshua Bloch",
  "description": "Best practices for the Java programming language",
  "price": 45.99,
  "stock": 100,
  "category": "Programming"
}
```

**Validation**:
- ✓ Complete book details returned
- ✓ Stock availability shown
- ✓ Price is accurate

---

#### 6. Create Order
**Endpoint**: `POST /api/v1/orders`  
**Service**: Order Service (Port 8082)

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
      "bookId": 1,
      "quantity": 2
    }
  ]
}
```

**Expected Response** (201 Created):
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
- ✓ Order ID is generated (MongoDB ObjectId)
- ✓ Book details are fetched from Catalog Service
- ✓ Total amount is calculated correctly
- ✓ Initial status is PENDING
- ✓ Stock is validated before order creation

**Service-to-Service Communication**:
- Order Service → Catalog Service: Validate book exists and has sufficient stock

---

#### 7. View Order Details
**Endpoint**: `GET /api/v1/orders/{orderId}`  
**Service**: Order Service (Port 8082)

**Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Expected Response** (200 OK):
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
- ✓ Order details match creation
- ✓ User can only view their own orders
- ✓ All item details are present

---

#### 8. View Order History
**Endpoint**: `GET /api/v1/orders`  
**Service**: Order Service (Port 8082)

**Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Expected Response** (200 OK):
```json
[
  {
    "id": "65a1b2c3d4e5f6g7h8i9j0k1",
    "userId": 1,
    "totalAmount": 91.98,
    "status": "PENDING",
    "createdAt": "2026-01-03T10:15:00"
  }
]
```

**Validation**:
- ✓ All user's orders are returned
- ✓ Orders are sorted by creation date (newest first)
- ✓ Only authenticated user's orders are shown

---

## Scenario 2: Admin Book Management

### Prerequisites
- Admin user exists in database
- Admin credentials: `admin@bookstore.com` / `admin123`

### Test Steps

#### 1. Admin Login
**Endpoint**: `POST /api/v1/auth/login`  
**Service**: User Service (Port 8083)

**Request**:
```json
{
  "email": "admin@bookstore.com",
  "password": "admin123"
}
```

**Expected Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "user": {
    "id": 1,
    "email": "admin@bookstore.com",
    "role": "ADMIN"
  }
}
```

**Validation**:
- ✓ JWT token contains ADMIN role
- ✓ Token can be used for admin operations

---

#### 2. Create New Book
**Endpoint**: `POST /api/v1/books`  
**Service**: Catalog Service (Port 8081)

**Headers**:
```
Authorization: Bearer {ADMIN_JWT_TOKEN}
Content-Type: application/json
```

**Request**:
```json
{
  "isbn": "9780134685991",
  "title": "Effective Java",
  "author": "Joshua Bloch",
  "description": "Best practices for the Java programming language",
  "price": 45.99,
  "stock": 100,
  "category": "Programming"
}
```

**Expected Response** (201 Created):
```json
{
  "id": 1,
  "isbn": "9780134685991",
  "title": "Effective Java",
  "author": "Joshua Bloch",
  "description": "Best practices for the Java programming language",
  "price": 45.99,
  "stock": 100,
  "category": "Programming",
  "createdAt": "2026-01-03T10:00:00"
}
```

**Validation**:
- ✓ Book ID is generated
- ✓ ISBN is unique
- ✓ All fields are saved correctly

---

#### 3. Update Book
**Endpoint**: `PUT /api/v1/books/{id}`  
**Service**: Catalog Service (Port 8081)

**Headers**:
```
Authorization: Bearer {ADMIN_JWT_TOKEN}
Content-Type: application/json
```

**Request**:
```json
{
  "isbn": "9780134685991",
  "title": "Effective Java - Third Edition",
  "author": "Joshua Bloch",
  "description": "Updated best practices for Java 7, 8, and 9",
  "price": 49.99,
  "stock": 150,
  "category": "Programming"
}
```

**Expected Response** (200 OK):
```json
{
  "id": 1,
  "isbn": "9780134685991",
  "title": "Effective Java - Third Edition",
  "author": "Joshua Bloch",
  "description": "Updated best practices for Java 7, 8, and 9",
  "price": 49.99,
  "stock": 150,
  "category": "Programming",
  "updatedAt": "2026-01-03T11:00:00"
}
```

**Validation**:
- ✓ Book is updated
- ✓ Updated timestamp is set
- ✓ Only admin can update

---

#### 4. Delete Book
**Endpoint**: `DELETE /api/v1/books/{id}`  
**Service**: Catalog Service (Port 8081)

**Headers**:
```
Authorization: Bearer {ADMIN_JWT_TOKEN}
```

**Expected Response** (204 No Content)

**Validation**:
- ✓ Book is deleted
- ✓ Subsequent GET returns 404
- ✓ Only admin can delete

---

## Scenario 3: Order Status Update (Admin)

### Test Steps

#### 1. Admin Views All Orders
**Endpoint**: `GET /api/v1/orders`  
**Service**: Order Service (Port 8082)

**Headers**:
```
Authorization: Bearer {ADMIN_JWT_TOKEN}
```

**Expected Response** (200 OK):
```json
[
  {
    "id": "65a1b2c3d4e5f6g7h8i9j0k1",
    "userId": 1,
    "totalAmount": 91.98,
    "status": "PENDING",
    "createdAt": "2026-01-03T10:15:00"
  }
]
```

**Validation**:
- ✓ Admin can see all orders (not just their own)

---

#### 2. Update Order Status
**Endpoint**: `PUT /api/v1/orders/{orderId}/status`  
**Service**: Order Service (Port 8082)

**Headers**:
```
Authorization: Bearer {ADMIN_JWT_TOKEN}
Content-Type: application/json
```

**Request**:
```json
{
  "status": "SHIPPED"
}
```

**Expected Response** (200 OK):
```json
{
  "id": "65a1b2c3d4e5f6g7h8i9j0k1",
  "status": "SHIPPED",
  "updatedAt": "2026-01-03T12:00:00"
}
```

**Validation**:
- ✓ Status is updated
- ✓ Valid status transitions (PENDING → PROCESSING → SHIPPED → DELIVERED)

---

## Success Criteria

All happy path scenarios should:
- ✓ Return appropriate HTTP status codes (200, 201, 204)
- ✓ Return valid JSON responses
- ✓ Maintain data consistency across services
- ✓ Enforce authentication where required
- ✓ Enforce role-based access control
- ✓ Complete within acceptable response times (<500ms for most operations)
