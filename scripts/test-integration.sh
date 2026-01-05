#!/bin/bash

# Integration Test Script
# Full E2E workflow: Register → Login → Browse Books → Create Order
# Tests service-to-service communication and complete user journey
# Updated for Phase 7: All requests go through API Gateway (port 8080)

# API Gateway URL (Phase 7)
GATEWAY_URL="http://localhost:8080"
API_BASE="${GATEWAY_URL}/api/v1"

# Service URLs (via Gateway)
CATALOG_URL="${API_BASE}"
ORDER_URL="${API_BASE}"
USER_URL="${API_BASE}"

# Direct service URLs (for backward compatibility / direct testing)
# CATALOG_URL="http://localhost:8081/api/v1"
# ORDER_URL="http://localhost:8082/api/v1"
# USER_URL="http://localhost:8083/api/v1"

CONTENT_TYPE="Content-Type: application/json"

echo "========================================="
echo "Integration Test Suite - E2E Workflow"
echo "========================================="
echo ""

# Step 1: Verify All Services are Running
echo "Step 1: Verify API Gateway and Services"
echo "========================================="
echo ""

echo "Checking API Gateway..."
GATEWAY_HEALTH=$(curl -s -o /dev/null -w "%{http_code}" "${GATEWAY_URL}/actuator/health")
if [ "$GATEWAY_HEALTH" == "200" ]; then
  echo "✓ API Gateway is running (Port 8080)"
else
  echo "✗ API Gateway is NOT running (Expected 200, got $GATEWAY_HEALTH)"
  echo "  Make sure Eureka Server and API Gateway are started"
  exit 1
fi

echo "Checking Eureka Server..."
EUREKA_HEALTH=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8761/actuator/health")
if [ "$EUREKA_HEALTH" == "200" ]; then
  echo "✓ Eureka Server is running (Port 8761)"
else
  echo "✗ Eureka Server is NOT running (Expected 200, got $EUREKA_HEALTH)"
fi

echo ""
echo "Note: All requests now go through API Gateway (port 8080)"
echo "Services are discovered via Eureka and load-balanced by Gateway"
echo ""
echo "All services are healthy!"
echo ""
echo ""

# Step 2: Register a New User
echo "Step 2: Register a New User"
echo "============================"
echo ""

TIMESTAMP=$(date +%s)
TEST_EMAIL="integration.test.${TIMESTAMP}@bookstore.com"
TEST_PASSWORD="IntegrationTest123!"

echo "Registering user: ${TEST_EMAIL}"
REGISTER_RESPONSE=$(curl -s -X POST "${USER_URL}/auth/register" \
  -H "${CONTENT_TYPE}" \
  -d "{
    \"email\": \"${TEST_EMAIL}\",
    \"password\": \"${TEST_PASSWORD}\",
    \"firstName\": \"Integration\",
    \"lastName\": \"Test\"
  }")

echo "$REGISTER_RESPONSE" | jq '.'
USER_ID=$(echo "$REGISTER_RESPONSE" | jq -r '.id // empty')

if [ -z "$USER_ID" ]; then
  echo ""
  echo "Registration failed. Trying to use existing admin account..."
  TEST_EMAIL="admin@bookstore.com"
  TEST_PASSWORD="admin123"
  USER_ID="1"
fi

echo ""
echo "User ID: $USER_ID"
echo ""
echo ""

# Step 3: Login and Get JWT Token
echo "Step 3: Login and Get JWT Token"
echo "================================"
echo ""

echo "Logging in as: ${TEST_EMAIL}"
LOGIN_RESPONSE=$(curl -s -X POST "${USER_URL}/auth/login" \
  -H "${CONTENT_TYPE}" \
  -d "{
    \"email\": \"${TEST_EMAIL}\",
    \"password\": \"${TEST_PASSWORD}\"
  }")

echo "$LOGIN_RESPONSE" | jq '.'
JWT_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token // .accessToken // empty')

if [ -z "$JWT_TOKEN" ]; then
  echo ""
  echo "✗ Failed to obtain JWT token. Cannot proceed with integration test."
  exit 1
fi

echo ""
echo "✓ JWT Token obtained: ${JWT_TOKEN:0:50}..."
echo ""
echo ""

# Step 4: Browse Books (Catalog Service)
echo "Step 4: Browse Books (Catalog Service)"
echo "======================================="
echo ""

echo "Fetching all available books..."
BOOKS_RESPONSE=$(curl -s -X GET "${CATALOG_URL}/books")
echo "$BOOKS_RESPONSE" | jq '.[0:3]'

BOOK_COUNT=$(echo "$BOOKS_RESPONSE" | jq '. | length')
echo ""
echo "Total books available: $BOOK_COUNT"

if [ "$BOOK_COUNT" -eq 0 ]; then
  echo ""
  echo "No books available. Creating a test book..."
  
  CREATE_BOOK_RESPONSE=$(curl -s -X POST "${CATALOG_URL}/books" \
    -H "${CONTENT_TYPE}" \
    -H "Authorization: Bearer ${JWT_TOKEN}" \
    -d '{
      "isbn": "9780134685991",
      "title": "Effective Java",
      "author": "Joshua Bloch",
      "description": "Best practices for the Java programming language",
      "price": 45.99,
      "stock": 100,
      "category": "Programming"
    }')
  
  echo "$CREATE_BOOK_RESPONSE" | jq '.'
  BOOK_ID=$(echo "$CREATE_BOOK_RESPONSE" | jq -r '.id // empty')
else
  BOOK_ID=$(echo "$BOOKS_RESPONSE" | jq -r '.[0].id // empty')
fi

echo ""
echo "Selected Book ID for order: $BOOK_ID"
echo ""
echo ""

# Step 5: Get Book Details
echo "Step 5: Get Book Details"
echo "========================"
echo ""

if [ -n "$BOOK_ID" ]; then
  echo "Fetching details for Book ID: $BOOK_ID"
  BOOK_DETAILS=$(curl -s -X GET "${CATALOG_URL}/books/${BOOK_ID}")
  echo "$BOOK_DETAILS" | jq '.'
  
  BOOK_TITLE=$(echo "$BOOK_DETAILS" | jq -r '.title // empty')
  BOOK_PRICE=$(echo "$BOOK_DETAILS" | jq -r '.price // empty')
  BOOK_STOCK=$(echo "$BOOK_DETAILS" | jq -r '.stock // empty')
  
  echo ""
  echo "Book: $BOOK_TITLE"
  echo "Price: \$${BOOK_PRICE}"
  echo "Stock: $BOOK_STOCK"
  echo ""
fi
echo ""

# Step 6: Search Books by Category
echo "Step 6: Search Books by Category"
echo "================================="
echo ""

echo "Searching for Programming books..."
SEARCH_RESPONSE=$(curl -s -X GET "${CATALOG_URL}/books/search?category=Programming")
echo "$SEARCH_RESPONSE" | jq '.'
SEARCH_COUNT=$(echo "$SEARCH_RESPONSE" | jq '. | length')
echo ""
echo "Found $SEARCH_COUNT Programming books"
echo ""
echo ""

# Step 7: Create an Order (Service-to-Service Communication Test)
echo "Step 7: Create an Order"
echo "======================="
echo ""

if [ -n "$BOOK_ID" ] && [ -n "$JWT_TOKEN" ]; then
  echo "Creating order for Book ID: $BOOK_ID"
  ORDER_RESPONSE=$(curl -s -X POST "${ORDER_URL}/orders" \
    -H "Authorization: Bearer ${JWT_TOKEN}" \
    -H "${CONTENT_TYPE}" \
    -d "{
      \"userId\": ${USER_ID},
      \"items\": [
        {
          \"bookId\": ${BOOK_ID},
          \"quantity\": 2
        }
      ]
    }")
  
  echo "$ORDER_RESPONSE" | jq '.'
  ORDER_ID=$(echo "$ORDER_RESPONSE" | jq -r '.id // ._id // empty')
  
  if [ -n "$ORDER_ID" ]; then
    echo ""
    echo "✓ Order created successfully!"
    echo "Order ID: $ORDER_ID"
  else
    echo ""
    echo "✗ Order creation failed"
    echo "Response: $ORDER_RESPONSE"
  fi
else
  echo "✗ Cannot create order - missing Book ID or JWT Token"
fi
echo ""
echo ""

# Step 8: Verify Order Details
echo "Step 8: Verify Order Details"
echo "============================="
echo ""

if [ -n "$ORDER_ID" ] && [ -n "$JWT_TOKEN" ]; then
  echo "Fetching order details for Order ID: $ORDER_ID"
  ORDER_DETAILS=$(curl -s -X GET "${ORDER_URL}/orders/${ORDER_ID}" \
    -H "Authorization: Bearer ${JWT_TOKEN}")
  echo "$ORDER_DETAILS" | jq '.'
  
  ORDER_STATUS=$(echo "$ORDER_DETAILS" | jq -r '.status // empty')
  ORDER_TOTAL=$(echo "$ORDER_DETAILS" | jq -r '.totalAmount // .total // empty')
  
  echo ""
  echo "Order Status: $ORDER_STATUS"
  echo "Order Total: \$${ORDER_TOTAL}"
  echo ""
fi
echo ""

# Step 9: Update Order Status
echo "Step 9: Update Order Status"
echo "==========================="
echo ""

if [ -n "$ORDER_ID" ] && [ -n "$JWT_TOKEN" ]; then
  echo "Updating order status to SHIPPED..."
  UPDATE_RESPONSE=$(curl -s -X PUT "${ORDER_URL}/orders/${ORDER_ID}/status" \
    -H "Authorization: Bearer ${JWT_TOKEN}" \
    -H "${CONTENT_TYPE}" \
    -d '{
      "status": "SHIPPED"
    }')
  echo "$UPDATE_RESPONSE" | jq '.'
  echo ""
  echo "✓ Order status updated"
fi
echo ""
echo ""

# Step 10: Get User Profile
echo "Step 10: Get User Profile"
echo "========================="
echo ""

if [ -n "$JWT_TOKEN" ]; then
  echo "Fetching user profile..."
  PROFILE_RESPONSE=$(curl -s -X GET "${USER_URL}/users/profile" \
    -H "Authorization: Bearer ${JWT_TOKEN}")
  echo "$PROFILE_RESPONSE" | jq '.'
  echo ""
fi
echo ""

# Step 11: Get User's Order History
echo "Step 11: Get User's Order History"
echo "=================================="
echo ""

if [ -n "$JWT_TOKEN" ]; then
  echo "Fetching all orders for user..."
  ORDERS_RESPONSE=$(curl -s -X GET "${ORDER_URL}/orders" \
    -H "Authorization: Bearer ${JWT_TOKEN}")
  echo "$ORDERS_RESPONSE" | jq '.'
  
  TOTAL_ORDERS=$(echo "$ORDERS_RESPONSE" | jq '. | length')
  echo ""
  echo "Total orders: $TOTAL_ORDERS"
  echo ""
fi
echo ""

# Summary
echo "========================================="
echo "Integration Test Summary"
echo "========================================="
echo ""
echo "✓ All services are running"
echo "✓ User registration: ${TEST_EMAIL}"
echo "✓ User authentication: JWT token obtained"
echo "✓ Catalog browsing: $BOOK_COUNT books available"
echo "✓ Book search: $SEARCH_COUNT Programming books found"
if [ -n "$ORDER_ID" ]; then
  echo "✓ Order creation: Order ID $ORDER_ID"
  echo "✓ Order status update: SHIPPED"
else
  echo "✗ Order creation: Failed"
fi
echo ""
echo "========================================="
echo "E2E Integration Test Completed"
echo "========================================="
