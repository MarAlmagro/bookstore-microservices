# E2E Tests

End-to-end tests for the Bookstore Microservices application using Playwright.

## Setup

```bash
npm install
npx playwright install
```

## Running Tests

```bash
# Run all tests
npm test

# Run with UI mode
npm run test:ui

# Run in headed mode (see browser)
npm run test:headed

# Debug tests
npm run test:debug

# View report
npm run report
```

## Prerequisites

Ensure all services are running:
```bash
docker-compose up -d
```

Wait for services to be ready (~90 seconds).

## Test Structure

- `tests/auth.api.spec.ts` - Authentication API tests
- `tests/catalog.api.spec.ts` - Catalog API tests
- `tests/admin.ui.spec.ts` - Admin UI tests
