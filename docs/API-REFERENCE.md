# API Reference

All client requests are routed through the **API Gateway** at `http://localhost:8080`. Each business service also exposes its own port for direct access during development.

---

## Base URLs

| Access Mode | URL | Use Case |
|-------------|-----|----------|
| **Via Gateway (recommended)** | `http://localhost:8080/api/v1` | Production-like routing, JWT validation, rate limiting |
| Catalog direct | `http://localhost:8081/api/v1` | Bypass gateway for debugging |
| Orders direct | `http://localhost:8082/api/v1` | Bypass gateway for debugging |
| Users direct | `http://localhost:8083/api/v1` | Bypass gateway for debugging |

---

## Authentication

### Login

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "john.doe@bookstore.com",
  "password": "customer123"
}
```

**Response (200):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 2,
    "email": "john.doe@bookstore.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "CUSTOMER"
  }
}
```

### Register

```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "email": "new.user@example.com",
  "password": "securePass123",
  "firstName": "New",
  "lastName": "User"
}
```

### Refresh Token

```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Using the Token

Include the JWT in the `Authorization` header for all protected endpoints:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## Catalog Service (Books)

### Public Endpoints (no auth required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/books` | List all books (paginated) |
| GET | `/api/v1/books/{id}` | Get book by ID |
| GET | `/api/v1/books/search?keyword=` | Search books by keyword |

### Admin Endpoints (ADMIN role required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/books` | Create a new book |
| PUT | `/api/v1/books/{id}` | Update an existing book |
| DELETE | `/api/v1/books/{id}` | Delete a book |

### Example: List Books

```bash
curl http://localhost:8080/api/v1/books
```

### Example: Create Book (Admin)

```bash
curl -X POST http://localhost:8080/api/v1/books \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -d '{
    "isbn": "9780134685991",
    "title": "Effective Java",
    "author": "Joshua Bloch",
    "description": "Best practices for Java",
    "price": 45.99,
    "stock": 50,
    "category": "Programming"
  }'
```

---

## Order Service

### Orders (auth required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/orders` | List current user's orders |
| GET | `/api/v1/orders/{id}` | Get order details |
| POST | `/api/v1/orders` | Create a new order |
| PUT | `/api/v1/orders/{id}/status` | Update order status (ADMIN) |

### Cart (auth required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/cart` | View shopping cart |
| POST | `/api/v1/cart/items` | Add item to cart |

### Example: Get Orders

```bash
curl http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer <TOKEN>"
```

---

## User Service

### Public Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/login` | User login |
| POST | `/api/v1/auth/register` | User registration |
| POST | `/api/v1/auth/refresh` | Refresh JWT token |

### Protected Endpoints (auth required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/users/profile` | Get current user's profile |
| PUT | `/api/v1/users/profile` | Update current user's profile |

---

## Error Responses

All errors follow a consistent structure:

```json
{
  "timestamp": "2026-03-09T17:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: email is required",
  "path": "/api/v1/auth/register"
}
```

| HTTP Status | Meaning | Suggested Client Action |
|-------------|---------|------------------------|
| 400 | Validation error | Display field-level errors |
| 401 | Unauthorized | Redirect to login or refresh token |
| 403 | Forbidden | Show access denied message |
| 404 | Not found | Show not found page |
| 429 | Rate limited | Retry after backoff |
| 500 | Internal server error | Show generic error message |

---

## Swagger / OpenAPI

Interactive API documentation is available for each service:

| Service | Swagger UI | OpenAPI JSON |
|---------|-----------|--------------|
| Catalog | http://localhost:8081/swagger-ui.html | http://localhost:8081/v3/api-docs |
| Orders | http://localhost:8082/swagger-ui.html | http://localhost:8082/v3/api-docs |
| Users | http://localhost:8083/swagger-ui.html | http://localhost:8083/v3/api-docs |

A consolidated OpenAPI specification is also available at [`docs/spa-frontend-spec/openapi.yaml`](spa-frontend-spec/openapi.yaml).

---

## CORS

The API Gateway handles CORS. Allowed origins are configured via environment variables:

```env
CORS_ALLOWED_ORIGIN=http://localhost:4200
CORS_ADMIN_ORIGIN=http://localhost:8084
```

| Setting | Value |
|---------|-------|
| Allowed Methods | GET, POST, PUT, DELETE, PATCH, OPTIONS |
| Allowed Headers | Authorization, Content-Type, X-User-Id |
| Credentials | true |
| Max Age | 3600s |

---

[← Back to Documentation Hub](README.md) · [Development Guide →](DEVELOPMENT.md)
