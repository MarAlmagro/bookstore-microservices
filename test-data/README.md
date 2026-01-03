# Test Data & Artifacts

This directory contains comprehensive test artifacts for the Bookstore Microservices project.

---

## Directory Structure

```
test-data/
├── README.md                    # This file
├── requests/                    # JSON request payloads
│   ├── catalog-requests.json   # Catalog service test payloads
│   ├── order-requests.json     # Order service test payloads
│   └── user-requests.json      # User service test payloads
└── scenarios/                   # Test scenario documentation
    ├── happy-path.md           # Successful user journeys
    ├── error-cases.md          # Error handling scenarios
    └── service-integration.md  # Service-to-service communication tests
```

---

## Quick Start

### 1. Start All Services

```bash
# From project root
docker-compose up -d

# Wait for services to be healthy
docker-compose ps
```

### 2. Run Test Scripts

```bash
# Test individual services
./scripts/test-catalog.sh
./scripts/test-user.sh
./scripts/test-order.sh

# Run full E2E integration test
./scripts/test-integration.sh
```

---

## Test Request Payloads

### Catalog Requests (`requests/catalog-requests.json`)

Contains JSON payloads for:
- **createBook**: Valid book creation request
- **createBookInvalid**: Invalid data for validation testing
- **updateBook**: Book update request
- **updateBookPartial**: Partial update (price/stock only)
- **searchParams**: Various search criteria (category, author, price range)

**Usage Example**:
```bash
# Create a book using the test payload
curl -X POST http://localhost:8081/api/v1/books \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -d @test-data/requests/catalog-requests.json | jq '.createBook'
```

---

### Order Requests (`requests/order-requests.json`)

Contains JSON payloads for:
- **createOrder**: Valid order with multiple items
- **createOrderSingleItem**: Simple single-item order
- **createOrderInvalidBook**: Order with non-existent book ID
- **createOrderInsufficientStock**: Order exceeding available stock
- **createOrderInvalidQuantity**: Order with negative quantity
- **createOrderMissingUserId**: Order missing required user ID
- **updateOrderStatus**: Valid status update
- **updateOrderStatusInvalid**: Invalid status value

**Usage Example**:
```bash
# Create an order
curl -X POST http://localhost:8082/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -d "$(jq '.createOrder' test-data/requests/order-requests.json)"
```

---

### User Requests (`requests/user-requests.json`)

Contains JSON payloads for:
- **register**: Valid user registration
- **registerAdmin**: Admin user registration
- **registerInvalidEmail**: Invalid email format
- **registerWeakPassword**: Password too short
- **registerMissingFields**: Missing required fields
- **login**: Valid login credentials
- **loginAdmin**: Admin login
- **loginInvalidCredentials**: Wrong password
- **loginNonExistentUser**: Non-existent email
- **updateProfile**: Profile update request
- **updateProfilePartial**: Partial profile update

**Usage Example**:
```bash
# Register a new user
curl -X POST http://localhost:8083/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d "$(jq '.register' test-data/requests/user-requests.json)"
```

---

## Test Scenarios Documentation

### Happy Path (`scenarios/happy-path.md`)

Documents successful user journeys:
- **Scenario 1**: New customer registration and first purchase
- **Scenario 2**: Admin book management (create, update, delete)
- **Scenario 3**: Order status updates

Each scenario includes:
- Prerequisites
- Step-by-step test instructions
- Expected requests and responses
- Validation criteria

---

### Error Cases (`scenarios/error-cases.md`)

Documents error handling across all services:
- **User Service Errors**: Registration, login, authentication failures
- **Catalog Service Errors**: Validation, authorization, resource not found
- **Order Service Errors**: Stock validation, business logic, unauthorized access
- **Service Availability Errors**: Handling external service failures

Each error case includes:
- Request that triggers the error
- Expected HTTP status code
- Expected error response format
- Validation criteria

---

### Service Integration (`scenarios/service-integration.md`)

Documents service-to-service communication:
- **Order ↔ Catalog**: Stock validation, price fetching
- **All Services ↔ User**: JWT authentication
- **Role-Based Access Control**: Permission enforcement
- **Error Propagation**: Handling service failures

Includes:
- Communication flow diagrams
- Integration test scenarios
- Troubleshooting guide
- Performance considerations

---

## Test Scripts

All test scripts are located in the `scripts/` directory:

### `test-catalog.sh`
Tests Catalog Service CRUD operations:
- Health check
- Get all books
- Create book
- Get book by ID
- Update book
- Search books (by category, author, price range)
- Delete book
- Error cases (invalid data, non-existent book)

**Run**:
```bash
./scripts/test-catalog.sh
```

---

### `test-user.sh`
Tests User Service authentication:
- Health check
- User registration (valid and invalid)
- User login (valid and invalid)
- Get user profile (authenticated and unauthenticated)
- Update user profile
- Admin login

**Run**:
```bash
./scripts/test-user.sh
```

**Output**: Creates a test user and returns JWT token

---

### `test-order.sh`
Tests Order Service operations:
- Health check
- Create order (with stock validation)
- Get all orders
- Get order by ID
- Update order status
- Error cases (invalid book, insufficient stock, unauthorized access)

**Prerequisites**: User Service and Catalog Service must be running

**Run**:
```bash
./scripts/test-order.sh
```

---

### `test-integration.sh`
End-to-end integration test:
1. Verify all services are running
2. Register new user
3. Login and get JWT token
4. Browse books
5. Search books by category
6. Get book details
7. Create order (validates stock with Catalog Service)
8. Verify order details
9. Update order status (admin operation)
10. Get user's order history

**Run**:
```bash
./scripts/test-integration.sh
```

**Expected Duration**: ~30 seconds

---

## Test Credentials

### Default Admin Account
```
Email: admin@bookstore.com
Password: admin123
Role: ADMIN
```

### Test Customer Accounts
Created by initialization scripts:
```
Email: john.doe@example.com
Password: password123
Role: CUSTOMER

Email: jane.smith@example.com
Password: password123
Role: CUSTOMER
```

---

## Using Test Data with Postman

### Import Collection

1. Create a new Postman collection
2. Import requests from `requests/*.json` files
3. Set environment variables:
   ```
   CATALOG_URL=http://localhost:8081/api/v1
   ORDER_URL=http://localhost:8082/api/v1
   USER_URL=http://localhost:8083/api/v1
   JWT_TOKEN={{jwt_token}}
   ```

### Workflow

1. **Login**: Use `user-requests.json` → `login` or `loginAdmin`
2. **Save Token**: Extract `token` from response and set as `{{jwt_token}}`
3. **Test Endpoints**: Use saved token in Authorization header

---

## Validation Checklist

Use this checklist when running tests:

### Catalog Service
- [ ] Can browse books without authentication
- [ ] Can search books by category, author, price range
- [ ] Admin can create books
- [ ] Admin can update books
- [ ] Admin can delete books
- [ ] Customer cannot create/update/delete books (403 Forbidden)
- [ ] Invalid data returns 400 Bad Request
- [ ] Non-existent book returns 404 Not Found

### User Service
- [ ] Can register with valid data
- [ ] Cannot register with invalid email (400)
- [ ] Cannot register with weak password (400)
- [ ] Cannot register duplicate email (409)
- [ ] Can login with valid credentials
- [ ] Cannot login with invalid credentials (401)
- [ ] Can get profile when authenticated
- [ ] Cannot get profile when unauthenticated (401)
- [ ] Can update profile when authenticated

### Order Service
- [ ] Cannot create order without authentication (401)
- [ ] Can create order with valid data and JWT
- [ ] Order validates book exists with Catalog Service
- [ ] Order validates sufficient stock
- [ ] Order rejects invalid book ID (404)
- [ ] Order rejects insufficient stock (400)
- [ ] Order rejects invalid quantity (400)
- [ ] User can only view their own orders
- [ ] Admin can view all orders
- [ ] Only admin can update order status

### Integration
- [ ] All services are healthy
- [ ] JWT authentication works across services
- [ ] Order Service fetches book details from Catalog Service
- [ ] Order Service validates stock with Catalog Service
- [ ] Role-based access control enforced
- [ ] Error responses are consistent
- [ ] Service failures handled gracefully

---

## Troubleshooting

### Services Not Running

```bash
# Check service status
docker-compose ps

# View logs
docker-compose logs catalog-service
docker-compose logs order-service
docker-compose logs user-service

# Restart services
docker-compose restart
```

### JWT Token Expired

JWT tokens expire after 1 hour. If you get 401 errors:
```bash
# Login again to get new token
./scripts/test-user.sh
```

### Database Not Initialized

If no books are available:
```bash
# Restart with fresh data
docker-compose down -v
docker-compose up -d

# Wait for initialization
sleep 30

# Verify data loaded
./scripts/verify-init.sh
```

### Port Conflicts

If services fail to start due to port conflicts:
```bash
# Check what's using the ports
netstat -ano | findstr :8081
netstat -ano | findstr :8082
netstat -ano | findstr :8083

# Stop conflicting processes or change ports in docker-compose.yml
```

---

## Adding New Test Cases

### 1. Add JSON Payload

Edit the appropriate file in `requests/`:
```json
{
  "newTestCase": {
    "field1": "value1",
    "field2": "value2"
  }
}
```

### 2. Add to Test Script

Edit the appropriate script in `../scripts/`:
```bash
echo "Test N: New Test Case"
echo "---------------------"
curl -s -X POST "${BASE_URL}/endpoint" \
  -H "${CONTENT_TYPE}" \
  -d "$(jq '.newTestCase' test-data/requests/file.json)" | jq '.'
```

### 3. Document the Scenario

Add to the appropriate file in `scenarios/`:
- Happy path → `happy-path.md`
- Error case → `error-cases.md`
- Integration → `service-integration.md`

---

## Best Practices

1. **Always use jq**: Format JSON responses for readability
2. **Save JWT tokens**: Store in variables for reuse across requests
3. **Check service health**: Before running tests
4. **Clean state**: Use `docker-compose down -v` for fresh start
5. **Read logs**: When tests fail, check service logs
6. **Validate responses**: Don't just check status codes, verify response data
7. **Test error cases**: Ensure proper error handling
8. **Test integration**: Verify service-to-service communication

---

## Contributing

When adding new test artifacts:
1. Follow existing JSON structure in `requests/`
2. Add comprehensive documentation in `scenarios/`
3. Update test scripts in `../scripts/`
4. Update this README with new test cases
5. Verify all tests pass before committing

---

## Related Documentation

- [Build Plan Phase 6](../BUILD_PLAN_PHASE_6.md) - Implementation plan
- [Project Specification](../PROJECT_SPECIFICATION.md) - Technical details
- [Main README](../README.md) - Project overview
- [Scripts README](../scripts/README.md) - Database initialization

---

**Last Updated**: January 3, 2026  
**Phase**: 6.3 - Test Artifacts & Collections
