#!/bin/bash

# Catalog Service Test Script
# Tests all CRUD operations for the Catalog Service
# Base URL: http://localhost:8081

BASE_URL="http://localhost:8081/api/v1"
CONTENT_TYPE="Content-Type: application/json"

echo "========================================="
echo "Catalog Service Test Suite"
echo "========================================="
echo ""

# Test 1: Health Check
echo "Test 1: Health Check"
echo "---------------------"
curl -s -X GET "${BASE_URL}/../actuator/health" | jq '.'
echo ""
echo ""

# Test 2: Get All Books
echo "Test 2: Get All Books (Initial State)"
echo "--------------------------------------"
curl -s -X GET "${BASE_URL}/books" | jq '.'
echo ""
echo ""

# Test 3: Create a New Book
echo "Test 3: Create a New Book"
echo "-------------------------"
BOOK_RESPONSE=$(curl -s -X POST "${BASE_URL}/books" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "isbn": "9780134685991",
    "title": "Effective Java",
    "author": "Joshua Bloch",
    "description": "Best practices for the Java programming language",
    "price": 45.99,
    "stock": 100,
    "category": "Programming"
  }')
echo "$BOOK_RESPONSE" | jq '.'
BOOK_ID=$(echo "$BOOK_RESPONSE" | jq -r '.id // empty')
echo ""
echo "Created Book ID: $BOOK_ID"
echo ""

# Test 4: Get Book by ID
if [ -n "$BOOK_ID" ]; then
  echo "Test 4: Get Book by ID ($BOOK_ID)"
  echo "----------------------------------"
  curl -s -X GET "${BASE_URL}/books/${BOOK_ID}" | jq '.'
  echo ""
  echo ""
fi

# Test 5: Update Book
if [ -n "$BOOK_ID" ]; then
  echo "Test 5: Update Book ($BOOK_ID)"
  echo "-------------------------------"
  curl -s -X PUT "${BASE_URL}/books/${BOOK_ID}" \
    -H "${CONTENT_TYPE}" \
    -d '{
      "isbn": "9780134685991",
      "title": "Effective Java - Third Edition",
      "author": "Joshua Bloch",
      "description": "Updated best practices for Java 7, 8, and 9",
      "price": 49.99,
      "stock": 150,
      "category": "Programming"
    }' | jq '.'
  echo ""
  echo ""
fi

# Test 6: Search Books by Category
echo "Test 6: Search Books by Category"
echo "---------------------------------"
curl -s -X GET "${BASE_URL}/books/search?category=Programming" | jq '.'
echo ""
echo ""

# Test 7: Search Books by Author
echo "Test 7: Search Books by Author"
echo "-------------------------------"
curl -s -X GET "${BASE_URL}/books/search?author=Joshua%20Bloch" | jq '.'
echo ""
echo ""

# Test 8: Search Books by Price Range
echo "Test 8: Search Books by Price Range"
echo "------------------------------------"
curl -s -X GET "${BASE_URL}/books/search?minPrice=20&maxPrice=60" | jq '.'
echo ""
echo ""

# Test 9: Create Book with Invalid Data (Validation Test)
echo "Test 9: Create Book with Invalid Data"
echo "--------------------------------------"
curl -s -X POST "${BASE_URL}/books" \
  -H "${CONTENT_TYPE}" \
  -d '{
    "isbn": "",
    "title": "",
    "author": "Test Author",
    "description": "Missing required fields",
    "price": -10.00,
    "stock": -5,
    "category": "Test"
  }' | jq '.'
echo ""
echo ""

# Test 10: Get Non-Existent Book (Error Handling)
echo "Test 10: Get Non-Existent Book"
echo "-------------------------------"
curl -s -X GET "${BASE_URL}/books/99999" | jq '.'
echo ""
echo ""

# Test 11: Delete Book
if [ -n "$BOOK_ID" ]; then
  echo "Test 11: Delete Book ($BOOK_ID)"
  echo "--------------------------------"
  curl -s -X DELETE "${BASE_URL}/books/${BOOK_ID}" -w "\nHTTP Status: %{http_code}\n"
  echo ""
  echo ""
fi

# Test 12: Verify Deletion
if [ -n "$BOOK_ID" ]; then
  echo "Test 12: Verify Book Deleted"
  echo "-----------------------------"
  curl -s -X GET "${BASE_URL}/books/${BOOK_ID}" | jq '.'
  echo ""
  echo ""
fi

echo "========================================="
echo "Catalog Service Tests Completed"
echo "========================================="
