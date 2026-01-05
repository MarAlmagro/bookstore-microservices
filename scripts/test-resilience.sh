#!/bin/bash

# Resilience Testing Script for Order Service
# Tests Circuit Breaker, Retry, and Bulkhead patterns

set -e

GATEWAY_URL="${GATEWAY_URL:-http://localhost:8080}"
ORDER_SERVICE_URL="${ORDER_SERVICE_URL:-http://localhost:8082}"
CATALOG_SERVICE_NAME="catalog-service"

echo "=========================================="
echo "Resilience4j Testing Script"
echo "=========================================="
echo ""

# Function to check circuit breaker state
check_circuit_breaker_state() {
    echo "Checking Circuit Breaker State..."
    curl -s "${ORDER_SERVICE_URL}/actuator/health" | grep -o '"circuitBreakers":{[^}]*}' || echo "Circuit breaker health not available"
    echo ""
}

# Function to test order creation
test_order_creation() {
    local user_id=$1
    local book_id=$2
    
    echo "Testing order creation (User: ${user_id}, Book: ${book_id})..."
    
    response=$(curl -s -w "\n%{http_code}" -X POST "${GATEWAY_URL}/api/v1/orders" \
        -H "Content-Type: application/json" \
        -d "{
            \"userId\": ${user_id},
            \"items\": [
                {
                    \"bookId\": ${book_id},
                    \"quantity\": 1
                }
            ]
        }" 2>&1)
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    echo "HTTP Status: ${http_code}"
    echo "Response: ${body}"
    echo ""
    
    return $([ "$http_code" -eq 200 ] || [ "$http_code" -eq 201 ])
}

# Step 1: Verify services are running
echo "Step 1: Verifying services are running..."
echo "-------------------------------------------"
if ! docker ps | grep -q "${CATALOG_SERVICE_NAME}"; then
    echo "ERROR: Catalog service is not running. Starting services..."
    docker-compose up -d
    sleep 10
fi
echo "Services are running."
echo ""

# Step 2: Test normal operation (Circuit Breaker CLOSED)
echo "Step 2: Testing normal operation..."
echo "-------------------------------------------"
check_circuit_breaker_state
test_order_creation 1 1 && echo "✓ Order creation successful (Circuit CLOSED)" || echo "✗ Order creation failed"
echo ""

# Step 3: Simulate catalog service failure
echo "Step 3: Simulating Catalog Service failure..."
echo "-------------------------------------------"
echo "Stopping catalog-service container..."
docker-compose stop ${CATALOG_SERVICE_NAME}
sleep 5
echo "Catalog service stopped."
echo ""

# Step 4: Generate failures to open circuit breaker
echo "Step 4: Generating failures to trigger Circuit Breaker..."
echo "-------------------------------------------"
echo "Sending 10 requests to trigger circuit breaker (threshold: 50% of 10 calls)..."
for i in {1..10}; do
    echo -n "Request ${i}/10: "
    test_order_creation 1 1 && echo "Success" || echo "Failed (expected)"
    sleep 1
done
echo ""

# Step 5: Verify circuit breaker is OPEN
echo "Step 5: Verifying Circuit Breaker is OPEN..."
echo "-------------------------------------------"
check_circuit_breaker_state
echo "Attempting order creation (should fail fast with fallback)..."
test_order_creation 1 1 && echo "✗ Unexpected success" || echo "✓ Failed as expected (Circuit OPEN)"
echo ""

# Step 6: Wait for circuit breaker to transition to HALF_OPEN
echo "Step 6: Waiting for Circuit Breaker to transition to HALF_OPEN..."
echo "-------------------------------------------"
echo "Waiting 10 seconds (waitDurationInOpenState)..."
sleep 10
check_circuit_breaker_state
echo ""

# Step 7: Restart catalog service
echo "Step 7: Restarting Catalog Service..."
echo "-------------------------------------------"
docker-compose start ${CATALOG_SERVICE_NAME}
echo "Waiting for catalog service to be ready..."
sleep 10
echo "Catalog service restarted."
echo ""

# Step 8: Test recovery (Circuit Breaker should close)
echo "Step 8: Testing recovery..."
echo "-------------------------------------------"
echo "Sending 3 successful requests (permittedNumberOfCallsInHalfOpenState)..."
for i in {1..3}; do
    echo -n "Request ${i}/3: "
    test_order_creation 1 1 && echo "Success" || echo "Failed"
    sleep 2
done
echo ""

# Step 9: Verify circuit breaker is CLOSED
echo "Step 9: Verifying Circuit Breaker is CLOSED..."
echo "-------------------------------------------"
check_circuit_breaker_state
test_order_creation 1 1 && echo "✓ Order creation successful (Circuit CLOSED)" || echo "✗ Order creation failed"
echo ""

# Step 10: Test Retry pattern
echo "Step 10: Testing Retry Pattern..."
echo "-------------------------------------------"
echo "Retry is configured with maxAttempts=3, waitDuration=2s, exponential backoff"
echo "Check logs for retry attempts: docker-compose logs order-service | grep -i retry"
echo ""

# Step 11: Test Bulkhead pattern
echo "Step 11: Testing Bulkhead Pattern..."
echo "-------------------------------------------"
echo "Bulkhead is configured with maxConcurrentCalls=10"
echo "To test: Send 15+ concurrent requests and verify only 10 are processed simultaneously"
echo "Check metrics: curl ${ORDER_SERVICE_URL}/actuator/prometheus | grep bulkhead"
echo ""

echo "=========================================="
echo "Resilience Testing Complete!"
echo "=========================================="
echo ""
echo "Summary:"
echo "- Circuit Breaker: Tested CLOSED -> OPEN -> HALF_OPEN -> CLOSED transitions"
echo "- Retry: Configured with 3 attempts and exponential backoff"
echo "- Bulkhead: Configured with 10 concurrent call limit"
echo "- Fallback: Tested with catalog service unavailable"
echo ""
echo "View metrics at: ${ORDER_SERVICE_URL}/actuator/prometheus"
echo "View health at: ${ORDER_SERVICE_URL}/actuator/health"
echo ""
