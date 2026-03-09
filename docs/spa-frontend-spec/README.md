# Bookstore SPA Frontend Specification

> Part of [Bookstore Microservices](../../README.md) · [Documentation Hub](../README.md)

This directory contains all contracts and documentation needed to build a standalone SPA frontend for the Bookstore Microservices application.

## Files

| File | Purpose |
|------|---------|
| `openapi.yaml` | **Source of Truth** - Complete API contract (OpenAPI 3.0.3) |
| `ARCHITECTURE.md` | System overview, auth flow, CORS, rate limits |
| `AGENTIC-RULES.md` | Code patterns, project structure, generation commands |
| `UI-SPEC.md` | Pages, components, responsive design, accessibility |
| `test-data.json` | Sample data and endpoint reference for testing |

## Quick Start for Agentic Coding

### 1. Generate Types from OpenAPI
```bash
npx openapi-typescript ./spa-frontend-spec/openapi.yaml -o ./src/api/generated/types.ts
```

### 2. Bootstrap Project
```bash
npm create vite@latest bookstore-spa -- --template react-ts
cd bookstore-spa
npm install axios @tanstack/react-query zustand zod react-hook-form @hookform/resolvers react-router-dom
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p
npx shadcn-ui@latest init
```

### 3. Key Integration Points

**API Gateway**: `http://localhost:8080`

**Auth Header**: `Authorization: Bearer <token>`

**Public Endpoints** (no auth):
- `GET /api/v1/books/**` (read operations)
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/register`

**Protected Endpoints** (require JWT):
- All `/api/v1/orders/**`
- All `/api/v1/users/**`
- `POST/PUT/DELETE /api/v1/books/**`

## Running the Backend

```bash
# Start all services
docker-compose up -d

# Verify services are healthy
curl http://localhost:8080/actuator/health
```

## Test Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@bookstore.com | admin123 |
| Customer | john.doe@bookstore.com | customer123 |

## Contract Validation

Before implementing any API call, verify against `openapi.yaml`:
1. Correct HTTP method
2. Correct path and parameters
3. Request body schema
4. Response schema
5. Required auth (check `security` field)

## Error Handling

All API errors return:
```json
{
  "timestamp": "ISO-8601",
  "status": 400,
  "error": "Bad Request",
  "message": "Detailed error message",
  "path": "/api/v1/..."
}
```
