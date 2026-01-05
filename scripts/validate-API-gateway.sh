#!/bin/bash

# Phase 7.9 Validation Script
# This script validates that all Phase 7 components are properly configured

echo "=========================================="
echo "Phase 7.9 Validation Script"
echo "=========================================="
echo ""

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Validation results
PASSED=0
FAILED=0

# Function to check result
check_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ PASSED${NC}: $2"
        ((PASSED++))
    else
        echo -e "${RED}✗ FAILED${NC}: $2"
        ((FAILED++))
    fi
}

echo "1. Checking Maven Build..."
echo "-------------------------------------------"
# Check if JAR files exist (indicating successful build)
if [ -f "eureka-server/target/eureka-server-1.0.0-SNAPSHOT.jar" ] && \
   [ -f "api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar" ] && \
   [ -f "catalog-service/target/catalog-service-1.0.0-SNAPSHOT.jar" ] && \
   [ -f "order-service/target/order-service-1.0.0-SNAPSHOT.jar" ] && \
   [ -f "user-service/target/user-service-1.0.0-SNAPSHOT.jar" ]; then
    check_result 0 "All services build successfully"
else
    check_result 1 "All services build successfully"
fi
echo ""

echo "2. Checking Project Structure..."
echo "-------------------------------------------"
[ -d "eureka-server" ] && check_result 0 "Eureka Server module exists" || check_result 1 "Eureka Server module exists"
[ -d "api-gateway" ] && check_result 0 "API Gateway module exists" || check_result 1 "API Gateway module exists"
[ -d "catalog-service" ] && check_result 0 "Catalog Service module exists" || check_result 1 "Catalog Service module exists"
[ -d "order-service" ] && check_result 0 "Order Service module exists" || check_result 1 "Order Service module exists"
[ -d "user-service" ] && check_result 0 "User Service module exists" || check_result 1 "User Service module exists"
[ -f "eureka-server/src/main/java/com/bookstore/eureka/EurekaServerApplication.java" ] && check_result 0 "Eureka Server application class exists" || check_result 1 "Eureka Server application class exists"
[ -f "api-gateway/src/main/java/com/bookstore/gateway/ApiGatewayApplication.java" ] && check_result 0 "API Gateway application class exists" || check_result 1 "API Gateway application class exists"
echo ""

echo "3. Checking Configuration Files..."
echo "-------------------------------------------"
[ -f "eureka-server/src/main/resources/application.yml" ] && check_result 0 "Eureka Server configuration exists" || check_result 1 "Eureka Server configuration exists"
[ -f "api-gateway/src/main/resources/application.yml" ] && check_result 0 "API Gateway configuration exists" || check_result 1 "API Gateway configuration exists"

# Check if services have Eureka client configuration
grep -q "eureka:" catalog-service/src/main/resources/application.yml && check_result 0 "Catalog Service has Eureka config" || check_result 1 "Catalog Service has Eureka config"
grep -q "eureka:" order-service/src/main/resources/application.yml && check_result 0 "Order Service has Eureka config" || check_result 1 "Order Service has Eureka config"
grep -q "eureka:" user-service/src/main/resources/application.yml && check_result 0 "User Service has Eureka config" || check_result 1 "User Service has Eureka config"
echo ""

echo "4. Checking Dependencies..."
echo "-------------------------------------------"
# Check if Spring Cloud is in parent POM
grep -q "spring-cloud-dependencies" pom.xml && check_result 0 "Spring Cloud BOM in parent POM" || check_result 1 "Spring Cloud BOM in parent POM"

# Check if services have Eureka client dependency
grep -q "spring-cloud-starter-netflix-eureka-client" catalog-service/pom.xml && check_result 0 "Catalog Service has Eureka client dependency" || check_result 1 "Catalog Service has Eureka client dependency"
grep -q "spring-cloud-starter-netflix-eureka-client" order-service/pom.xml && check_result 0 "Order Service has Eureka client dependency" || check_result 1 "Order Service has Eureka client dependency"
grep -q "spring-cloud-starter-netflix-eureka-client" user-service/pom.xml && check_result 0 "User Service has Eureka client dependency" || check_result 1 "User Service has Eureka client dependency"
echo ""

echo "5. Checking Docker Configuration..."
echo "-------------------------------------------"
[ -f "eureka-server/Dockerfile" ] && check_result 0 "Eureka Server Dockerfile exists" || check_result 1 "Eureka Server Dockerfile exists"
[ -f "api-gateway/Dockerfile" ] && check_result 0 "API Gateway Dockerfile exists" || check_result 1 "API Gateway Dockerfile exists"
grep -q "eureka-server:" docker-compose.yml && check_result 0 "Eureka Server in docker-compose.yml" || check_result 1 "Eureka Server in docker-compose.yml"
grep -q "api-gateway:" docker-compose.yml && check_result 0 "API Gateway in docker-compose.yml" || check_result 1 "API Gateway in docker-compose.yml"
echo ""

echo "6. Checking Gateway Routes Configuration..."
echo "-------------------------------------------"
grep -q "catalog-service" api-gateway/src/main/resources/application.yml && check_result 0 "Catalog Service route configured" || check_result 1 "Catalog Service route configured"
grep -q "order-service" api-gateway/src/main/resources/application.yml && check_result 0 "Order Service route configured" || check_result 1 "Order Service route configured"
grep -q "user-service" api-gateway/src/main/resources/application.yml && check_result 0 "User Service route configured" || check_result 1 "User Service route configured"
echo ""

echo "7. Checking Service Discovery Configuration..."
echo "-------------------------------------------"
# Check if Order Service uses @LoadBalanced RestTemplate
grep -q "@LoadBalanced" order-service/src/main/java/com/bookstore/order/config/RestTemplateConfig.java && check_result 0 "Order Service uses @LoadBalanced RestTemplate" || check_result 1 "Order Service uses @LoadBalanced RestTemplate"
echo ""

echo "=========================================="
echo "Validation Summary"
echo "=========================================="
echo -e "${GREEN}Passed: $PASSED${NC}"
echo -e "${RED}Failed: $FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✓ All Phase 7 validations passed!${NC}"
    exit 0
else
    echo -e "${YELLOW}⚠ Some validations failed. Please review the results above.${NC}"
    exit 1
fi
