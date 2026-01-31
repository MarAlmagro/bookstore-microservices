# Bookstore SPA Frontend Architecture Specification

## System Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                         SPA Frontend                                 │
│                    (React/Vue/Angular/etc.)                         │
└─────────────────────────────────────────────────────────────────────┘
                                │
                                │ HTTP/HTTPS (port 8080)
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        API Gateway                                   │
│  • JWT Validation    • Rate Limiting    • CORS    • Load Balancing  │
└─────────────────────────────────────────────────────────────────────┘
          │                     │                     │
          ▼                     ▼                     ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│  user-service   │  │ catalog-service │  │  order-service  │
│   (PostgreSQL)  │  │     (MySQL)     │  │    (MongoDB)    │
│    Port 8083    │  │    Port 8081    │  │    Port 8082    │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

## API Gateway Configuration

| Route Pattern | Target Service | Auth Required |
|--------------|----------------|---------------|
| `/api/v1/auth/**` | user-service | No (public) |
| `/api/v1/users/**` | user-service | Yes |
| `/api/v1/books/**` | catalog-service | Partial* |
| `/api/v1/orders/**` | order-service | Yes |

*Books: GET endpoints are public, POST/PUT/DELETE require ADMIN role.

## Authentication Flow

```
1. Login/Register
   POST /api/v1/auth/login  →  { token, refreshToken, user }
   POST /api/v1/auth/register  →  { token, refreshToken, user }

2. Authenticated Requests
   Authorization: Bearer <token>

3. Token Refresh (when access token expires)
   POST /api/v1/auth/refresh { refreshToken }  →  { token, refreshToken, user }

4. Token Storage (SPA)
   - accessToken: Memory (XSS-safe)
   - refreshToken: HttpOnly cookie or secure storage
```

## Role-Based Access Control

| Feature | CUSTOMER | ADMIN |
|---------|----------|-------|
| Browse books | ✓ | ✓ |
| Search books | ✓ | ✓ |
| View own profile | ✓ | ✓ |
| Update own profile | ✓ | ✓ |
| Create orders | ✓ | ✓ |
| View own orders | ✓ | ✓ |
| Manage catalog | ✗ | ✓ |
| View all orders | ✗ | ✓ |
| Manage users | ✗ | ✓ |
| Update order status | ✗ | ✓ |

## Error Handling Contract

All errors follow this structure:
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: email is required",
  "path": "/api/v1/auth/register"
}
```

| Status | Meaning | SPA Action |
|--------|---------|------------|
| 400 | Validation error | Show field errors |
| 401 | Unauthorized | Redirect to login |
| 403 | Forbidden | Show access denied |
| 404 | Not found | Show not found page |
| 429 | Rate limited | Show retry message |
| 500 | Server error | Show generic error |

## Rate Limiting

| Endpoint Group | Requests/sec | Burst |
|---------------|--------------|-------|
| Auth endpoints | 5 | 10 |
| Books endpoints | 10 | 20 |
| Orders endpoints | 10 | 20 |

## CORS Configuration

```yaml
Allowed Origins:
  - http://localhost:3000  (dev)
  - ${CORS_ALLOWED_ORIGIN}  (configurable)

Allowed Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS
Allowed Headers: Authorization, Content-Type, X-User-Id
Credentials: true
Max Age: 3600s
```

## Recommended SPA Tech Stack

| Layer | Recommended | Alternatives |
|-------|-------------|--------------|
| Framework | React 18+ | Vue 3, Angular 17+ |
| State | Zustand/Jotai | Redux Toolkit, Pinia |
| HTTP Client | Axios | Fetch, ky |
| Forms | React Hook Form | Formik |
| Validation | Zod | Yup |
| UI Components | shadcn/ui | MUI, Ant Design |
| Styling | Tailwind CSS | CSS Modules |
| Icons | Lucide | Heroicons |
| Router | React Router v6 | TanStack Router |
| Data Fetching | TanStack Query | SWR |

## Environment Variables

```env
# Required
VITE_API_BASE_URL=http://localhost:8080

# Optional
VITE_APP_NAME=Bookstore
VITE_TOKEN_REFRESH_THRESHOLD_MS=300000
```
