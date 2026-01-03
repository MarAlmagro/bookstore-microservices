#!/bin/bash

# Order Service Test Script
# Tests order creation and management
# Base URL: http://localhost:8082
# Requires: Catalog service running, User service running

BASE_URL="http://localhost:8082/api/v1"
CATALOG_URL="http://localhost:8081/api/v1"
USER_URL="http://localhost:8083/api/v1"
CONTENT_TYPE="Content-Type: application/json"

echo "========================================="
echo "Order Service Test Suite"
echo "========================================="
echo ""

# Test 1: Health Check
echo "Test 1: Health Check"
echo "---------------------"
curl -s -X GET "${BASE_URL}/../actuator/health" | jq '.'
echo ""
echo ""

# Setup: Login to get JWT token
echo "Setup: Login to Get JWT Token"
echo "------------------------------"
LOGIN_RESPONSE=$(curl -s -X POST "${USER_URL}/auth/login" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "email": "admin@bookstore.com",
    "password": "admin123"
  }')
JWT_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token // .accessToken // empty')

if [ -z "$JWT_TOKEN" ]; then
  echo "Failed to obtain JWT token. Using test@test.com instead..."
  LOGIN_RESPONSE=$(curl -s -X POST "${USER_URL}/auth/login" \
    -H "${CONTENT_TYPE}" \
    -d '{
      "email": "test@test.com",
      "password": "test123"
    }')
  JWT_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token // .accessToken // empty')
fi

echo "JWT Token obtained: ${JWT_TOKEN:0:50}..."
echo ""
echo ""

# Setup: Get available books
echo "Setup: Get Available Books"
echo "--------------------------"
BOOKS_RESPONSE=$(curl -s -X GET "${CATALOG_URL}/books")
echo "$BOOKS_RESPONSE" | jq '.[0:3]'
BOOK_ID=$(echo "$BOOKS_RESPONSE" | jq -r '.[0].id // empty')
echo ""
echo "Using Book ID: $BOOK_ID"
echo ""

# Test 2: Create Order (Authenticated)
if [ -n "$JWT_TOKEN" ] && [ -n "$BOOK_ID" ]; then
  echo "Test 2: Create Order (Authenticated)"
  echo "------------------------------------"
  ORDER_RESPONSE=$(curl -s -X POST "${BASE_URL}/orders" \
    -H "Authorization: Bearer ${JWT_TOKEN}" \
    -H "${CONTENT_TYPE}" \
    -d "{
      \"userId\": 1,
      \"items\": [
        {
          \"bookId\": ${BOOK_ID},
          \"quantity\": 2
        }
      ]
    }")
  echo "$ORDER_RESPONSE" | jq '.'
  ORDER_ID=$(echo "$ORDER_RESPONSE" | jq -r '.id // ._id // empty')
  echo ""
  echo "Created Order ID: $ORDER_ID"
  echo ""
fi

# Test 3: Create Order (Unauthenticated)
echo "Test 3: Create Order (Unauthenticated)"
echo "---------------------------------------"
curl -s -X POST "${BASE_URL}/orders" \
  -H "${CONTENT_TYPE}" \
  -d "{
    \"userId\": 1,
    \"items\": [
      {
        \"bookId\": ${BOOK_ID},
        \"quantity\": 1
      }
    ]
  }" | jq '.'
echo ""
echo ""

# Test 4: Create Order with Invalid Book ID
if [ -n "$JWT_TOKEN" ]; then
  echo "Test 4: Create Order with Invalid Book ID"
  echo "------------------------------------------"
  curl -s -X POST "${BASE_URL}/orders" \
    -H "Authorization: Bearer ${JWT_TOKEN}" \
    -H "${CONTENT_TYPE}" \
    -d '{
      "userId": 1,
      "items": [
        {
          "bookId": 99999,
          "quantity": 1
        }
      ]
    }' | jq '.'
  echo ""
  echo ""
fi

# Test 5: Create Order with Insufficient Stock
if [ -n "$JWT_TOKEN" ] && [ -n "$BOOK_ID" ]; then
  echo "Test 5: Create Order with Insufficient Stock"
  echo "---------------------------------------------"
  curl -s -X POST "${BASE_URL}/orders" \
    -H "Authorization: Bearer ${JWT_TOKEN}" \
    -H "${CONTENT_TYPE}" \
    -d "{
      \"userId\": 1,
      \"items\": [
        {
          \"bookId\": ${BOOK_ID},
          \"quantity\": 10000
        }
      ]
    }" | jq '.'
  echo ""
  echo ""
fi

# Test 6: Create Order with Invalid Quantity
if [ -n "$JWT_TOKEN" ] && [ -n "$BOOK_ID" ]; then
  echo "Test 6: Create Order with Invalid Quantity"
  echo "-------------------------------------------"
  curl -s -X POST "${BASE_URL}/orders" \
    -H "Authorization: Bearer ${JWT_TOKEN}" \
    -H "${CONTENT_TYPE}" \
    -d "{
      \"userId\": 1,
      \"items\": [
        {
          \"bookId\": ${BOOK_ID},
          \"quantity\": -5
        }
      ]
    }" | jq '.'
  echo ""
  echo ""
fi

# Test 7: Get All Orders (Authenticated)
if [ -n "$JWT_TOKEN" ]; then
  echo "Test 7: Get All Orders (Authenticated)"
  echo "---------------------------------------"
  curl -s -X GET "${BASE_URL}/orders" \
    -H "Authorization: Bearer ${JWT_TOKEN}" | jq '.'
  echo ""
  echo ""
fi

# Test 8: Get Order by ID
if [ -n "$JWT_TOKEN" ] && [ -n "$ORDER_ID" ]; then
  echo "Test 8: Get Order by ID ($ORDER_ID)"
  echo "------------------------------------"
  curl -s -X GET "${BASE_URL}/orders/${ORDER_ID}" \
    -H "Authorization: Bearer ${JWT_TOKEN}" | jq '.'
  echo ""
  echo ""
fi

# Test 9: Update Order Status
if [ -n "$JWT_TOKEN" ] && [ -n "$ORDER_ID" ]; then
  echo "Test 9: Update Order Status"
  echo "----------------------------"
  curl -s -X PUT "${BASE_URL}/orders/${ORDER_ID}/status" \
    -H "Authorization: Bearer ${JWT_TOKEN}" \
    -H "${CONTENT_TYPE}" \
    -d '{
      "status": "SHIPPED"
    }' | jq '.'
  echo ""
  echo ""
fi

# Test 10: Update Order Status with Invalid Status
if [ -n "$JWT_TOKEN" ] && [ -n "$ORDER_ID" ]; then
  echo "Test 10: Update Order Status with Invalid Status"
  echo "-------------------------------------------------"
  curl -s -X PUT "${BASE_URL}/orders/${ORDER_ID}/status" \
    -H "Authorization: Bearer ${JWT_TOKEN}" \
    -H "${CONTENT_TYPE}" \
    -d '{
      "status": "INVALID_STATUS"
    }' | jq '.'
  echo ""
  echo ""
fi

# Test 11: Get Non-Existent Order
if [ -n "$JWT_TOKEN" ]; then
  echo "Test 11: Get Non-Existent Order"
  echo "--------------------------------"
  curl -s -X GET "${BASE_URL}/orders/99999999999999999999" \
    -H "Authorization: Bearer ${JWT_TOKEN}" | jq '.'
  echo ""
  echo ""
fi

echo "========================================="
echo "Order Service Tests Completed"
echo "========================================="
