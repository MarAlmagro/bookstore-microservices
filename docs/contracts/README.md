# Bookstore Microservices - Service Contract Documentation

## 📋 Overview

This directory contains the complete API contract documentation for Phase 12 (Angular SPA Integration). All files were generated through deep analysis of the microservices codebase, including REST controllers, DTOs, validation annotations, and gateway routing configuration.

## 📁 Files in This Directory

### 1. `openapi.json` - OpenAPI 3.0 Specification
**Purpose**: The single source of truth for all API endpoints, request/response schemas, and validation rules.

**Contents**:
- Complete API specification in OpenAPI 3.0.3 format
- All 30+ REST endpoints across 3 microservices
- Detailed schema definitions for 8 DTOs with validation constraints
- Security scheme definition (JWT Bearer Authentication)
- Request/response examples with proper type mappings
- Error response schemas

**Usage**:
- Import into Swagger UI, Postman, or Insomnia for API testing
- Generate API client libraries using OpenAPI Generator
- Validate API requests/responses in CI/CD pipelines
- Share with frontend developers as API reference

**Server URL**: `http://localhost:8080` (API Gateway)

### 2. `GATEWAY_ROUTING.md` - Gateway Routing Documentation
**Purpose**: Developer-friendly guide to understanding the API Gateway routing architecture.

**Contents**:
- Complete routing table for all 3 microservices
- HTTP method, path, destination, and auth requirements for each endpoint
- Service discovery configuration details
- CORS settings and security considerations
- Authentication flow diagrams
- Troubleshooting guide for common gateway issues
- Performance and production deployment recommendations

**Key Insights**:
- Gateway runs on port 8080
- Uses Eureka service discovery with load balancing
- Permissive CORS enabled for development
- JWT authentication required for user profile endpoints

### 3. `proxy.conf.json` - Angular Development Proxy
**Purpose**: Eliminate CORS issues during Angular development by proxying API calls through the dev server.

**Contents**:
- Proxy configuration for Angular CLI (`ng serve`)
- Routes all `/api/*` requests to `http://localhost:8080`
- Debug logging enabled for troubleshooting

**Usage in Angular Project**:
```bash
# Add to angular.json
"serve": {
  "options": {
    "proxyConfig": "proxy.conf.json"
  }
}

# Or use directly
ng serve --proxy-config proxy.conf.json
```

### 4. `frontend-models.ts` - TypeScript Interface Definitions
**Purpose**: Type-safe TypeScript interfaces matching all Java DTOs for Angular development.

**Contents**:
- 12 TypeScript interfaces with complete JSDoc documentation
- 2 enums (OrderStatus, UserRole) with all valid values
- Type guards for runtime type checking
- Utility types for pagination and API responses
- Proper Java-to-TypeScript type mappings

**Type Mapping Rules Applied**:
- `Long`/`Integer` → `number`
- `BigDecimal` → `number`
- `LocalDateTime`/`Date` → `string` (ISO 8601)
- `List<T>`/`Set<T>` → `T[]`
- `@NotNull` → Required field (no `?`)
- `@Min/@Max/@Size` → Documented in JSDoc

**Usage in Angular**:
```typescript
import { BookDTO, OrderDTO, AuthResponseDTO } from './models/frontend-models';

// Type-safe HTTP calls
this.http.get<BookDTO[]>('/api/v1/books').subscribe(books => {
  // TypeScript knows the exact shape of books
});
```

## 🔍 API Endpoint Summary

### Catalog Service (13 endpoints)
- **Books CRUD**: Create, Read, Update, Delete operations
- **Search & Filter**: By title, author, category, ISBN
- **Stock Management**: Check availability, low stock alerts, update quantities
- **Batch Operations**: CSV import for bulk catalog updates

### Order Service (8 endpoints)
- **Order Management**: Create, retrieve, update status, delete
- **Filtering**: By user, by status (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
- **Batch Operations**: Generate sales reports in mainframe-compatible format

### User Service (7 endpoints)
- **Authentication**: Register, login, refresh token (JWT-based)
- **Profile Management**: Get/update current user profile
- **Admin Operations**: View/delete users (requires ADMIN role)

**Total Endpoints Mapped**: 28 REST endpoints + 2 batch endpoints = **30 endpoints**

## 📊 DTO Coverage

All 8 Data Transfer Objects have been fully documented:

| DTO | Fields | Validation Rules | Used By |
|:----|:------:|:----------------:|:--------|
| `BookDTO` | 8 | @NotBlank, @Size, @DecimalMin, @Min | Catalog Service |
| `OrderDTO` | 6 | @NotNull, @NotEmpty, @Valid | Order Service |
| `OrderItemDTO` | 5 | @NotNull, @Min | Order Service |
| `UserDTO` | 5 | @NotBlank, @Email, @Size | User Service |
| `AuthRequestDTO` | 2 | @NotBlank, @Email, @Size | User Service |
| `AuthResponseDTO` | 3 | None (response only) | User Service |
| `BookImportDTO` | 7 | None (batch only) | Catalog Batch |
| `OrderReportDTO` | 4 | None (batch only) | Order Batch |
| `ErrorResponse` | 5 | None (error only) | All Services |

## 🔐 Security & Authentication

### Public Endpoints (No Auth Required)
- All book browsing endpoints (GET `/api/v1/books/**`)
- User registration (`POST /api/v1/auth/register`)
- User login (`POST /api/v1/auth/login`)
- Token refresh (`POST /api/v1/auth/refresh`)

### Protected Endpoints (JWT Required)
- User profile operations (`/api/v1/users/profile`)
- Admin user management (`/api/v1/users/{id}`)

### Recommended for Production
The following endpoints should be protected but are currently open:
- Book creation/modification (should require ADMIN role)
- Order operations (should validate user ownership)
- Stock updates (should require ADMIN role)
- Batch operations (should require ADMIN role)

## 🚀 Quick Start for Frontend Developers

### 1. Setup Angular Proxy
Copy `proxy.conf.json` to your Angular project root and configure:
```json
// angular.json
"serve": {
  "options": {
    "proxyConfig": "proxy.conf.json"
  }
}
```

### 2. Import TypeScript Models
Copy `frontend-models.ts` to your Angular project:
```bash
cp frontend-models.ts src/app/models/
```

### 3. Create HTTP Service
```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BookDTO, OrderDTO, AuthResponseDTO } from './models/frontend-models';

@Injectable({ providedIn: 'root' })
export class BookstoreApiService {
  private baseUrl = '/api/v1';

  constructor(private http: HttpClient) {}

  // Books
  getAllBooks(): Observable<BookDTO[]> {
    return this.http.get<BookDTO[]>(`${this.baseUrl}/books`);
  }

  getBookById(id: number): Observable<BookDTO> {
    return this.http.get<BookDTO>(`${this.baseUrl}/books/${id}`);
  }

  // Orders
  createOrder(order: OrderDTO): Observable<OrderDTO> {
    return this.http.post<OrderDTO>(`${this.baseUrl}/orders`, order);
  }

  // Auth
  login(email: string, password: string): Observable<AuthResponseDTO> {
    return this.http.post<AuthResponseDTO>(`${this.baseUrl}/auth/login`, {
      email,
      password
    });
  }
}
```

### 4. Add JWT Interceptor
```typescript
import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler } from '@angular/common/http';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler) {
    const token = localStorage.getItem('access_token');
    if (token) {
      req = req.clone({
        setHeaders: { Authorization: `Bearer ${token}` }
      });
    }
    return next.handle(req);
  }
}
```

## 🧪 Testing the APIs

### Using cURL
```bash
# Get all books
curl http://localhost:8080/api/v1/books

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'

# Create order (with JWT)
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{"userId":1,"items":[{"bookId":1,"quantity":2}]}'
```

### Using Postman
1. Import `openapi.json` into Postman
2. Set base URL to `http://localhost:8080`
3. For protected endpoints, add Authorization header: `Bearer {token}`

## 📈 Observability

### Health Checks
- **Gateway**: `http://localhost:8080/actuator/health`
- **Catalog**: `http://localhost:8081/actuator/health`
- **Order**: `http://localhost:8082/actuator/health`
- **User**: `http://localhost:8083/actuator/health`

### Service Discovery
- **Eureka Dashboard**: `http://localhost:8761`

### Monitoring
- **Prometheus**: `http://localhost:9090`
- **Grafana**: `http://localhost:3000`
- **Zipkin**: `http://localhost:9411`

## 🐛 Known Limitations & Gaps

### Missing Features
1. **Pagination**: No pagination support for list endpoints (will return all records)
2. **Sorting**: No sort parameters for collection endpoints
3. **Filtering**: Limited filter options (only basic search/category/author)
4. **Rate Limiting**: Not implemented (could cause performance issues)
5. **API Versioning**: Only v1 exists, no deprecation strategy

### Security Gaps
1. Most mutating operations lack authentication/authorization
2. No role-based access control on catalog/order endpoints
3. CORS is fully permissive (not production-ready)
4. No request size limits
5. No API key validation

### Documentation Gaps
1. Actuator endpoints not included in OpenAPI spec
2. WebSocket endpoints (if any) not documented
3. Batch job status endpoints not exposed via REST
4. No examples for complex error scenarios

## 🎯 Recommendations for Phase 12

### High Priority
1. ✅ Use the provided TypeScript models for type safety
2. ✅ Configure Angular proxy to avoid CORS issues
3. ✅ Implement JWT token storage and refresh logic
4. ⚠️ Add error handling for all API calls (use ErrorResponse type)
5. ⚠️ Implement loading states and retry logic

### Medium Priority
1. Add pagination support in frontend (prepare for backend pagination)
2. Implement optimistic UI updates for better UX
3. Add request caching for frequently accessed data
4. Create reusable HTTP service with interceptors
5. Add comprehensive error messages for validation failures

### Low Priority
1. Add API response logging for debugging
2. Implement request/response transformation if needed
3. Create mock services for offline development
4. Add API performance monitoring

## 📚 Additional Resources

- **Architecture Docs**: `../ARCHITECTURE.md`
- **API Gateway Details**: `../GATEWAY.md`
- **Observability Setup**: `../OBSERVABILITY.md`
- **Test Data**: `../../test-data/requests/`

## 🤝 Contributing

When adding new endpoints:
1. Update the OpenAPI specification
2. Add TypeScript interfaces for new DTOs
3. Update the routing documentation
4. Document any new authentication requirements
5. Add examples to this README

## 📝 Version History

- **v1.0.0** (2026-01-05): Initial contract generation for Phase 12
  - 30 endpoints documented
  - 8 DTOs with full validation rules
  - Complete TypeScript type definitions
  - Angular proxy configuration
  - Gateway routing documentation

---

**Generated**: January 5, 2026  
**Target Phase**: Phase 12 - Angular SPA Development  
**Services Analyzed**: catalog-service, order-service, user-service, api-gateway  
**Total Endpoints**: 30  
**Total DTOs**: 9 (including ErrorResponse)
