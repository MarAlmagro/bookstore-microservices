# Testing Guide - Bookstore Microservices

This guide provides comprehensive instructions for testing the Bookstore Microservices application.

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Quick Start](#quick-start)
3. [Test Credentials](#test-credentials)
4. [Running Test Scripts](#running-test-scripts)
5. [Manual Testing](#manual-testing)
6. [Expected Responses](#expected-responses)
7. [Troubleshooting](#troubleshooting)
8. [Resetting Test Data](#resetting-test-data)

---

## Prerequisites

### Required Software

- **Docker Desktop**: Version 20.10 or higher
  - Download: https://www.docker.com/products/docker-desktop
  - Verify: `docker --version` and `docker-compose --version`

- **Maven**: Version 3.8 or higher (for local builds)
  - Windows: Install to `C:\opt\apache-maven\`
  - Linux/Mac: Install via package manager
  - Verify: `mvn --version`

- **Java**: JDK 11
  - Download: https://adoptium.net/
  - Verify: `java -version`

- **Git**: Latest version
  - Verify: `git --version`

- **curl**: For API testing
  - Windows: Included in Windows 10+
  - Linux/Mac: Pre-installed
  - Verify: `curl --version`

- **jq** (Optional): For JSON formatting
  - Windows: Download from https://stedolan.github.io/jq/
  - Linux: `sudo apt-get install jq`
  - Mac: `brew install jq`

### System Requirements

- **RAM**: Minimum 8GB (16GB recommended)
- **Disk Space**: 10GB free space
- **Ports Available**: 3306, 5432, 8081, 8082, 8083, 27017

---

## Quick Start

### 1. Start All Services

```bash
# Clone the repository
git clone <repository-url>
cd bookstore-microservices

# Start all services with Docker Compose
docker-compose up -d

# Wait for services to be healthy (30-60 seconds)
docker-compose ps
```

### 2. Verify Services are Running

```bash
# Check service health
curl http://localhost:8081/actuator/health  # Catalog Service
curl http://localhost:8082/actuator/health  # Order Service
curl http://localhost:8083/actuator/health  # User Service
```

Expected response: `{"status":"UP"}`

### 3. Access Swagger UI

Open in your browser:
- Catalog Service: http://localhost:8081/swagger-ui.html
- Order Service: http://localhost:8082/swagger-ui.html
- User Service: http://localhost:8083/swagger-ui.html

### 4. Run Integration Tests

```bash
# Make scripts executable (Linux/Mac)
chmod +x scripts/*.sh

# Run full E2E integration test
./scripts/test-integration.sh

# Or run individual service tests
./scripts/test-catalog.sh
./scripts/test-user.sh
./scripts/test-order.sh
```

**Windows Users**: Use Git Bash or PowerShell to run the scripts.

---

## Test Credentials

### Admin Account
```
Email: admin@bookstore.com
Password: admin123
Role: ADMIN
```

**Permissions**:
- Create, update, delete books
- View all orders
- Update order status

### Customer Accounts

**Account 1**:
```
Email: john.doe@example.com
Password: password123
Role: CUSTOMER
```

**Account 2**:
```
Email: jane.smith@example.com
Password: password123
Role: CUSTOMER
```

**Permissions**:
- Browse books
- Create orders
- View own orders only

### Test Account (Created by Scripts)
```
Email: test@test.com
Password: test123
Role: CUSTOMER
```

---

## Running Test Scripts

### Test Script Overview

| Script | Purpose | Duration | Prerequisites |
|--------|---------|----------|---------------|
| `test-catalog.sh` | Test Catalog Service CRUD operations | ~30s | Catalog Service running |
| `test-user.sh` | Test User Service authentication | ~20s | User Service running |
| `test-order.sh` | Test Order Service operations | ~40s | All services running |
| `test-integration.sh` | Full E2E workflow test | ~60s | All services running |

### Running Individual Tests

#### Catalog Service Tests
```bash
./scripts/test-catalog.sh
```

**Tests Performed**:
1. Health check
2. Get all books
3. Create new book (admin required)
4. Get book by ID
5. Update book
6. Search books (by category, author, price)
7. Validation tests (invalid data)
8. Delete book

**Expected Output**: 12 test cases with JSON responses

---

#### User Service Tests
```bash
./scripts/test-user.sh
```

**Tests Performed**:
1. Health check
2. Register new user
3. Registration validation (invalid email, weak password)
4. Login with valid credentials
5. Login with invalid credentials
6. Get user profile (authenticated)
7. Get user profile (unauthenticated - should fail)
8. Update user profile
9. Admin login

**Expected Output**: 12 test cases, JWT token saved for subsequent tests

---

#### Order Service Tests
```bash
./scripts/test-order.sh
```

**Tests Performed**:
1. Health check
2. Login to get JWT token
3. Create order (authenticated)
4. Create order (unauthenticated - should fail)
5. Create order with invalid book ID
6. Create order with insufficient stock
7. Get all orders
8. Get order by ID
9. Update order status
10. Validation tests

**Expected Output**: 11 test cases with service-to-service communication

---

#### Integration Test (E2E)
```bash
./scripts/test-integration.sh
```

**Complete Workflow**:
1. ✓ Verify all services running
2. ✓ Register new user
3. ✓ Login and get JWT token
4. ✓ Browse books
5. ✓ Search books by category
6. ✓ Get book details
7. ✓ Create order (validates with Catalog Service)
8. ✓ Verify order details
9. ✓ Update order status
10. ✓ Get user profile
11. ✓ Get order history

**Expected Duration**: 60 seconds  
**Expected Result**: All steps pass with ✓ checkmarks

---

## Manual Testing

### Using curl

#### 1. Register a New User
```bash
curl -X POST http://localhost:8083/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com",
    "password": "SecurePass123!",
    "firstName": "New",
    "lastName": "User"
  }'
```

**Expected Response** (201 Created):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 8,
    "email": "newuser@example.com",
    "firstName": "New",
    "lastName": "User",
    "role": "CUSTOMER"
  }
}
```

---

#### 2. Login
```bash
curl -X POST http://localhost:8083/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@bookstore.com",
    "password": "admin123"
  }'
```

**Save the JWT token** from the response for authenticated requests.

---

#### 3. Browse Books
```bash
curl http://localhost:8081/api/v1/books
```

**Expected Response** (200 OK): Array of books

---

#### 4. Create an Order
```bash
# Replace YOUR_JWT_TOKEN with actual token from login
curl -X POST http://localhost:8082/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "userId": 1,
    "items": [
      {
        "bookId": 1,
        "quantity": 2
      }
    ]
  }'
```

**Expected Response** (201 Created):
```json
{
  "id": "65a1b2c3d4e5f6g7h8i9j0k1",
  "userId": 1,
  "items": [
    {
      "bookId": 1,
      "quantity": 2,
      "price": 45.99,
      "bookTitle": "Effective Java",
      "bookIsbn": "9780134685991"
    }
  ],
  "totalAmount": 91.98,
  "status": "PENDING",
  "createdAt": "2026-01-03T10:15:00"
}
```

---

### Using Swagger UI

1. **Open Swagger UI** in browser:
   - http://localhost:8081/swagger-ui.html (Catalog)
   - http://localhost:8082/swagger-ui.html (Order)
   - http://localhost:8083/swagger-ui.html (User)

2. **Authenticate** (for protected endpoints):
   - Click "Authorize" button (top right)
   - Enter: `Bearer YOUR_JWT_TOKEN`
   - Click "Authorize"

3. **Test Endpoints**:
   - Expand endpoint section
   - Click "Try it out"
   - Fill in parameters
   - Click "Execute"
   - View response

---

## Expected Responses

### Success Responses

#### User Registration (201 Created)
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "role": "CUSTOMER"
  }
}
```

#### Book List (200 OK)
```json
[
  {
    "id": 1,
    "title": "Effective Java",
    "author": "Joshua Bloch",
    "price": 45.99,
    "stock": 100
  }
]
```

#### Order Created (201 Created)
```json
{
  "id": "65a1b2c3d4e5f6g7h8i9j0k1",
  "totalAmount": 91.98,
  "status": "PENDING"
}
```

---

### Error Responses

#### Invalid Credentials (401 Unauthorized)
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password"
}
```

#### Book Not Found (404 Not Found)
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Book not found with id: 99999"
}
```

#### Insufficient Stock (400 Bad Request)
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient stock for book: Effective Java. Available: 5, Requested: 10"
}
```

#### Access Denied (403 Forbidden)
```json
{
  "timestamp": "2026-01-03T10:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied. Admin role required."
}
```

---

## Troubleshooting

### Services Not Starting

**Issue**: Docker containers fail to start

**Solutions**:
```bash
# Check Docker is running
docker ps

# Check logs for specific service
docker-compose logs catalog-service
docker-compose logs order-service
docker-compose logs user-service

# Restart services
docker-compose restart

# Full restart with rebuild
docker-compose down
docker-compose up --build -d
```

---

### Port Conflicts

**Issue**: Port already in use (e.g., 8081, 8082, 8083)

**Solutions**:

**Windows**:
```powershell
# Find process using port
netstat -ano | findstr :8081

# Kill process (replace PID)
taskkill /PID <PID> /F
```

**Linux/Mac**:
```bash
# Find process using port
lsof -i :8081

# Kill process
kill -9 <PID>
```

**Alternative**: Change ports in `docker-compose.yml`

---

### Database Connection Errors

**Issue**: Service cannot connect to database

**Solutions**:
```bash
# Check database containers are running
docker-compose ps mysql-catalog
docker-compose ps mongodb-order
docker-compose ps postgres-user

# Restart database containers
docker-compose restart mysql-catalog mongodb-order postgres-user

# Check database logs
docker-compose logs mysql-catalog
docker-compose logs mongodb-order
docker-compose logs postgres-user
```

---

### JWT Token Expired

**Issue**: 401 Unauthorized after some time

**Solution**:
```bash
# Login again to get new token
curl -X POST http://localhost:8083/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@bookstore.com",
    "password": "admin123"
  }'
```

**Note**: Access tokens expire after 1 hour. Use refresh token for long-running sessions.

---

### No Books Available

**Issue**: GET /api/v1/books returns empty array

**Solution**:
```bash
# Verify initialization scripts ran
docker-compose logs catalog-service | grep "init-catalog.sql"

# If not initialized, restart with fresh volumes
docker-compose down -v
docker-compose up -d

# Wait 30 seconds for initialization
sleep 30

# Verify books loaded
curl http://localhost:8081/api/v1/books
```

---

### Test Scripts Not Executable

**Issue**: Permission denied when running scripts

**Solution** (Linux/Mac):
```bash
chmod +x scripts/*.sh
```

**Windows**: Use Git Bash or PowerShell:
```powershell
bash scripts/test-integration.sh
```

---

### Service Health Check Fails

**Issue**: Service shows as unhealthy in `docker-compose ps`

**Solutions**:
```bash
# Check service logs
docker-compose logs <service-name>

# Common issues:
# 1. Database not ready - wait longer
# 2. Port conflict - change port
# 3. Build error - rebuild with --no-cache

# Rebuild specific service
docker-compose build --no-cache <service-name>
docker-compose up -d <service-name>
```

---

## Resetting Test Data

### Full Reset (All Data)

```bash
# Stop all services and remove volumes
docker-compose down -v

# Start services (will reinitialize databases)
docker-compose up -d

# Wait for initialization (30-60 seconds)
sleep 30

# Verify data loaded
curl http://localhost:8081/api/v1/books
```

**Warning**: This deletes ALL data including test orders and users.

---

### Reset Specific Database

#### Catalog Database (MySQL)
```bash
docker-compose restart mysql-catalog catalog-service
```

#### Order Database (MongoDB)
```bash
docker-compose restart mongodb-order order-service
```

#### User Database (PostgreSQL)
```bash
docker-compose restart postgres-user user-service
```

---

### Verify Data Initialization

```bash
# Run verification script
./scripts/verify-init.sh

# Or manually check each service
curl http://localhost:8081/api/v1/books | jq 'length'  # Should return 15+
curl http://localhost:8083/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@bookstore.com","password":"admin123"}' | jq '.token'
```

---

## Test Data Files

### JSON Request Payloads

Located in `test-data/requests/`:
- `catalog-requests.json` - Book CRUD operations
- `order-requests.json` - Order creation and updates
- `user-requests.json` - Registration and login

**Usage**:
```bash
# Use jq to extract specific payload
jq '.createBook' test-data/requests/catalog-requests.json

# Use in curl command
curl -X POST http://localhost:8081/api/v1/books \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "$(jq '.createBook' test-data/requests/catalog-requests.json)"
```

---

### Test Scenarios

Located in `test-data/scenarios/`:
- `happy-path.md` - Successful user journeys
- `error-cases.md` - Error handling scenarios
- `service-integration.md` - Service-to-service communication

---

## Performance Testing

### Load Testing with curl

```bash
# Create 10 concurrent orders
for i in {1..10}; do
  curl -X POST http://localhost:8082/api/v1/orders \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{
      "userId": 1,
      "items": [{"bookId": 1, "quantity": 1}]
    }' &
done
wait
```

### Response Time Testing

```bash
# Measure response time
time curl http://localhost:8081/api/v1/books

# Expected: < 500ms
```

---

## Continuous Integration

### Running Tests in CI/CD

```bash
# Build all services
mvn clean install -DskipTests

# Start services
docker-compose up -d

# Wait for health
timeout 60 bash -c 'until docker-compose ps | grep healthy; do sleep 2; done'

# Run tests
./scripts/test-integration.sh

# Cleanup
docker-compose down -v
```

---

## Additional Resources

- **Test Results**: See `test-data/TEST_RESULTS.md` for Phase 6.4 validation results
- **API Documentation**: Access Swagger UI for each service
- **Architecture**: See `docs/ARCHITECTURE.md` for system design
- **Main README**: See root `README.md` for project overview

---

## Support

For issues or questions:
1. Check this troubleshooting guide
2. Review service logs: `docker-compose logs <service-name>`
3. Check GitHub issues
4. Consult test scenario documentation in `test-data/scenarios/`

---

**Last Updated**: January 3, 2026  
**Version**: 1.0.0
