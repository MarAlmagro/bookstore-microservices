#!/bin/bash

# User Service Test Script
# Tests authentication and user management
# Base URL: http://localhost:8083

BASE_URL="http://localhost:8083/api/v1"
CONTENT_TYPE="Content-Type: application/json"

echo "========================================="
echo "User Service Test Suite"
echo "========================================="
echo ""

# Test 1: Health Check
echo "Test 1: Health Check"
echo "---------------------"
curl -s -X GET "${BASE_URL}/../actuator/health" | jq '.'
echo ""
echo ""

# Test 2: Register New User
echo "Test 2: Register New User"
echo "-------------------------"
TIMESTAMP=$(date +%s)
TEST_EMAIL="testuser${TIMESTAMP}@example.com"
TEST_PASSWORD="SecurePass123!"

REGISTER_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/register" \
  -H "${CONTENT_TYPE}" \
  -d "{
    \"email\": \"${TEST_EMAIL}\",
    \"password\": \"${TEST_PASSWORD}\",
    \"firstName\": \"Test\",
    \"lastName\": \"User\"
  }")
echo "$REGISTER_RESPONSE" | jq '.'
echo ""
echo ""

# Test 3: Register with Invalid Email
echo "Test 3: Register with Invalid Email"
echo "------------------------------------"
curl -s -X POST "${BASE_URL}/auth/register" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "email": "invalid-email",
    "password": "SecurePass123!",
    "firstName": "Invalid",
    "lastName": "Email"
  }' | jq '.'
echo ""
echo ""

# Test 4: Register with Weak Password
echo "Test 4: Register with Weak Password"
echo "------------------------------------"
curl -s -X POST "${BASE_URL}/auth/register" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "email": "weak@example.com",
    "password": "123",
    "firstName": "Weak",
    "lastName": "Password"
  }' | jq '.'
echo ""
echo ""

# Test 5: Login with Valid Credentials
echo "Test 5: Login with Valid Credentials"
echo "-------------------------------------"
LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/login" \
  -H "${CONTENT_TYPE}" \
  -d "{
    \"email\": \"${TEST_EMAIL}\",
    \"password\": \"${TEST_PASSWORD}\"
  }")
echo "$LOGIN_RESPONSE" | jq '.'
JWT_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token // .accessToken // empty')
echo ""
echo "JWT Token: ${JWT_TOKEN:0:50}..."
echo ""

# Test 6: Login with Invalid Credentials
echo "Test 6: Login with Invalid Credentials"
echo "---------------------------------------"
curl -s -X POST "${BASE_URL}/auth/login" \
  -H "${CONTENT_TYPE}" \
  -d "{
    \"email\": \"${TEST_EMAIL}\",
    \"password\": \"WrongPassword\"
  }" | jq '.'
echo ""
echo ""

# Test 7: Login with Non-Existent User
echo "Test 7: Login with Non-Existent User"
echo "-------------------------------------"
curl -s -X POST "${BASE_URL}/auth/login" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "email": "nonexistent@example.com",
    "password": "SomePassword123"
  }' | jq '.'
echo ""
echo ""

# Test 8: Get User Profile (Authenticated)
if [ -n "$JWT_TOKEN" ]; then
  echo "Test 8: Get User Profile (Authenticated)"
  echo "-----------------------------------------"
  curl -s -X GET "${BASE_URL}/users/profile" \
    -H "Authorization: Bearer ${JWT_TOKEN}" | jq '.'
  echo ""
  echo ""
fi

# Test 9: Get User Profile (Unauthenticated)
echo "Test 9: Get User Profile (Unauthenticated)"
echo "-------------------------------------------"
curl -s -X GET "${BASE_URL}/users/profile" | jq '.'
echo ""
echo ""

# Test 10: Update User Profile (Authenticated)
if [ -n "$JWT_TOKEN" ]; then
  echo "Test 10: Update User Profile (Authenticated)"
  echo "---------------------------------------------"
  curl -s -X PUT "${BASE_URL}/users/profile" \
    -H "Authorization: Bearer ${JWT_TOKEN}" \
    -H "${CONTENT_TYPE}" \
    -d '{
      "firstName": "Updated",
      "lastName": "Name"
    }' | jq '.'
  echo ""
  echo ""
fi

# Test 11: Update User Profile (Unauthenticated)
echo "Test 11: Update User Profile (Unauthenticated)"
echo "-----------------------------------------------"
curl -s -X PUT "${BASE_URL}/users/profile" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "firstName": "Should",
    "lastName": "Fail"
  }' | jq '.'
echo ""
echo ""

# Test 12: Login as Admin (if exists)
echo "Test 12: Login as Admin"
echo "-----------------------"
ADMIN_LOGIN=$(curl -s -X POST "${BASE_URL}/auth/login" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "email": "admin@bookstore.com",
    "password": "admin123"
  }')
echo "$ADMIN_LOGIN" | jq '.'
ADMIN_TOKEN=$(echo "$ADMIN_LOGIN" | jq -r '.token // .accessToken // empty')
echo ""
if [ -n "$ADMIN_TOKEN" ]; then
  echo "Admin JWT Token: ${ADMIN_TOKEN:0:50}..."
fi
echo ""

echo "========================================="
echo "User Service Tests Completed"
echo "========================================="
echo ""
echo "Test Credentials Created:"
echo "Email: ${TEST_EMAIL}"
echo "Password: ${TEST_PASSWORD}"
if [ -n "$JWT_TOKEN" ]; then
  echo "JWT Token: ${JWT_TOKEN}"
fi
