# API Gateway Routing Configuration

## Overview
The API Gateway serves as the single entry point for all client requests to the Bookstore microservices platform. It runs on **port 8080** and uses Spring Cloud Gateway with Eureka service discovery for dynamic routing.

## Gateway Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     API Gateway (Port 8080)                  │
│                  http://localhost:8080                       │
└─────────────────────────────────────────────────────────────┘
                              │
                ┌─────────────┼─────────────┐
                │             │             │
                ▼             ▼             ▼
        ┌───────────┐  ┌───────────┐  ┌───────────┐
        │ Catalog   │  │  Order    │  │   User    │
        │ Service   │  │ Service   │  │ Service   │
        │ Port 8081 │  │ Port 8082 │  │ Port 8083 │
        └───────────┘  └───────────┘  └───────────┘
```

## Route Mappings

### Catalog Service Routes

| HTTP Method | Gateway Path | Destination | Description | Auth Required |
|:------------|:-------------|:------------|:------------|:--------------|
| GET | `/api/v1/books` | `catalog-service:8081` | Get all books | No |
| GET | `/api/v1/books/{id}` | `catalog-service:8081` | Get book by ID | No |
| GET | `/api/v1/books/isbn/{isbn}` | `catalog-service:8081` | Get book by ISBN | No |
| GET | `/api/v1/books/search` | `catalog-service:8081` | Search books by title/author | No |
| GET | `/api/v1/books/category/{category}` | `catalog-service:8081` | Get books by category | No |
| GET | `/api/v1/books/author/{author}` | `catalog-service:8081` | Get books by author | No |
| GET | `/api/v1/books/available` | `catalog-service:8081` | Get available books (in stock) | No |
| GET | `/api/v1/books/low-stock` | `catalog-service:8081` | Get low stock books | No |
| POST | `/api/v1/books` | `catalog-service:8081` | Create new book | No* |
| PUT | `/api/v1/books/{id}` | `catalog-service:8081` | Update book | No* |
| DELETE | `/api/v1/books/{id}` | `catalog-service:8081` | Delete book | No* |
| PATCH | `/api/v1/books/{id}/stock` | `catalog-service:8081` | Update book stock | No* |
| POST | `/api/v1/batch/import-catalog` | `catalog-service:8081` | Import catalog from CSV | No* |

**Note:** Routes marked with `*` should ideally require authentication/authorization in production. Currently implemented without security constraints.

### Order Service Routes

| HTTP Method | Gateway Path | Destination | Description | Auth Required |
|:------------|:-------------|:------------|:------------|:--------------|
| GET | `/api/v1/orders` | `order-service:8082` | Get all orders | No* |
| GET | `/api/v1/orders/{id}` | `order-service:8082` | Get order by ID | No* |
| GET | `/api/v1/orders/user/{userId}` | `order-service:8082` | Get orders for specific user | No* |
| GET | `/api/v1/orders/status/{status}` | `order-service:8082` | Get orders by status | No* |
| POST | `/api/v1/orders` | `order-service:8082` | Create new order | No* |
| PUT | `/api/v1/orders/{id}/status` | `order-service:8082` | Update order status | No* |
| DELETE | `/api/v1/orders/{id}` | `order-service:8082` | Delete order | No* |
| POST | `/api/v1/batch/generate-sales-report` | `order-service:8082` | Generate sales report | No* |

**Note:** Routes marked with `*` should require authentication. Order operations should validate user ownership or admin role.

### User Service Routes

| HTTP Method | Gateway Path | Destination | Description | Auth Required |
|:------------|:-------------|:------------|:------------|:--------------|
| POST | `/api/v1/auth/register` | `user-service:8083` | Register new user | No |
| POST | `/api/v1/auth/login` | `user-service:8083` | User login | No |
| POST | `/api/v1/auth/refresh` | `user-service:8083` | Refresh JWT token | No |
| GET | `/api/v1/users/profile` | `user-service:8083` | Get current user profile | **Yes** |
| PUT | `/api/v1/users/profile` | `user-service:8083` | Update current user profile | **Yes** |
| GET | `/api/v1/users/{id}` | `user-service:8083` | Get user by ID (Admin) | **Yes (Admin)** |
| DELETE | `/api/v1/users/{id}` | `user-service:8083` | Delete user (Admin) | **Yes (Admin)** |

## Service Discovery Configuration

The API Gateway uses **Eureka Service Discovery** to dynamically locate service instances:

- **Eureka Server**: `http://localhost:8761/eureka/`
- **Load Balancing**: Enabled via `lb://` URI scheme
- **Service Registration**: All microservices register with Eureka on startup

### Route Configuration Pattern

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: catalog-service
          uri: lb://catalog-service
          predicates:
            - Path=/api/v1/books/**
```

## CORS Configuration

The Gateway is configured with permissive CORS settings for development:

- **Allowed Origins**: `*` (all origins)
- **Allowed Methods**: GET, POST, PUT, DELETE, OPTIONS
- **Allowed Headers**: `*` (all headers)
- **Credentials**: Not allowed (set to false)

**⚠️ Production Warning**: Tighten CORS settings before deploying to production by specifying exact allowed origins.

## Authentication Flow

### JWT Token Usage

1. **Obtain Token**: Call `/api/v1/auth/login` or `/api/v1/auth/register`
2. **Include Token**: Add `Authorization: Bearer {token}` header to protected endpoints
3. **Refresh Token**: Use `/api/v1/auth/refresh` with refresh token when access token expires

### Protected Endpoints

Endpoints requiring authentication should include the JWT token in the Authorization header:

```http
GET /api/v1/users/profile HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Observability Endpoints

The Gateway exposes actuator endpoints for monitoring:

| Endpoint | Description |
|:---------|:------------|
| `/actuator/health` | Health check status |
| `/actuator/info` | Application information |
| `/actuator/gateway/routes` | View all configured routes |
| `/actuator/prometheus` | Prometheus metrics |
| `/actuator/metrics` | Application metrics |

## Request Flow Example

### Creating an Order

```mermaid
sequenceDiagram
    participant Client
    participant Gateway
    participant UserService
    participant OrderService
    participant CatalogService
    
    Client->>Gateway: POST /api/v1/orders
    Gateway->>UserService: Validate JWT Token
    UserService-->>Gateway: Token Valid
    Gateway->>OrderService: Forward Request
    OrderService->>CatalogService: Check Book Availability
    CatalogService-->>OrderService: Stock Available
    OrderService->>CatalogService: Update Stock
    OrderService-->>Gateway: Order Created
    Gateway-->>Client: 201 Created
```

## Error Handling

All services return standardized error responses:

```json
{
  "timestamp": "2026-01-05T21:13:00",
  "status": 404,
  "error": "Not Found",
  "message": "Book with ID 999 not found",
  "path": "/api/v1/books/999"
}
```

## Performance Considerations

- **Connection Pooling**: Gateway maintains connection pools to backend services
- **Circuit Breaker**: Resilience4j circuit breakers protect against cascading failures
- **Rate Limiting**: Can be configured per route (not currently enabled)
- **Caching**: Response caching can be added for read-heavy endpoints

## Development vs Production

### Development (Current)
- Permissive CORS
- All services on localhost
- Detailed logging enabled
- No rate limiting

### Production Recommendations
1. Enable authentication on all mutating operations
2. Restrict CORS to specific domains
3. Enable rate limiting per client
4. Use HTTPS/TLS for all communications
5. Implement API key validation
6. Add request/response size limits
7. Enable distributed tracing (Zipkin/Jaeger)

## Troubleshooting

### Common Issues

**Gateway returns 503 Service Unavailable**
- Check if target service is registered with Eureka
- Verify service health: `http://localhost:8761`

**CORS errors in browser**
- Ensure preflight OPTIONS requests are allowed
- Check browser console for specific CORS error

**404 Not Found for valid endpoints**
- Verify path predicates in gateway configuration
- Check service registration name matches URI

## Additional Resources

- **OpenAPI Specification**: See `openapi.json` for complete API documentation
- **Frontend Proxy Config**: See `proxy.conf.json` for Angular development setup
- **TypeScript Models**: See `frontend-models.ts` for type definitions
