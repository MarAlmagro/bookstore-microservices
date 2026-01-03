#!/bin/bash
# Verification script for database initialization
# Run this after starting Docker containers to verify sample data loaded correctly

echo "=========================================="
echo "Database Initialization Verification"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to check if container is running
check_container() {
    if docker ps --format '{{.Names}}' | grep -q "^$1$"; then
        echo -e "${GREEN}✓${NC} Container $1 is running"
        return 0
    else
        echo -e "${RED}✗${NC} Container $1 is not running"
        return 1
    fi
}

# Function to run SQL query on MySQL
mysql_query() {
    docker exec bookstore-mysql mysql -u root -ppassword -D bookstore_catalog -e "$1" 2>/dev/null
}

# Function to run SQL query on PostgreSQL
postgres_query() {
    docker exec bookstore-postgres psql -U postgres -d bookstore_users -t -c "$1" 2>/dev/null
}

# Function to run command on MongoDB
mongo_query() {
    docker exec bookstore-mongodb mongosh bookstore_orders --quiet --eval "$1" 2>/dev/null
}

echo "1. Checking Docker Containers..."
echo "-------------------------------------------"
check_container "bookstore-mysql"
MYSQL_RUNNING=$?
check_container "bookstore-postgres"
POSTGRES_RUNNING=$?
check_container "bookstore-mongodb"
MONGO_RUNNING=$?
echo ""

if [ $MYSQL_RUNNING -ne 0 ] || [ $POSTGRES_RUNNING -ne 0 ] || [ $MONGO_RUNNING -ne 0 ]; then
    echo -e "${RED}Error: Not all containers are running${NC}"
    echo "Please start containers with: docker-compose up -d"
    exit 1
fi

echo "2. Verifying MySQL Catalog Data..."
echo "-------------------------------------------"
if [ $MYSQL_RUNNING -eq 0 ]; then
    BOOK_COUNT=$(mysql_query "SELECT COUNT(*) FROM books;" | tail -n 1)
    if [ "$BOOK_COUNT" = "20" ]; then
        echo -e "${GREEN}✓${NC} Catalog database initialized successfully"
        echo "  - Books loaded: $BOOK_COUNT"
        echo ""
        echo "  Sample books:"
        mysql_query "SELECT id, title, author, price FROM books LIMIT 5;" | column -t
    else
        echo -e "${RED}✗${NC} Expected 20 books, found: $BOOK_COUNT"
    fi
fi
echo ""

echo "3. Verifying PostgreSQL User Data..."
echo "-------------------------------------------"
if [ $POSTGRES_RUNNING -eq 0 ]; then
    USER_COUNT=$(postgres_query "SELECT COUNT(*) FROM users;" | xargs)
    if [ "$USER_COUNT" = "6" ]; then
        echo -e "${GREEN}✓${NC} User database initialized successfully"
        echo "  - Users loaded: $USER_COUNT"
        echo ""
        echo "  User accounts:"
        postgres_query "SELECT email, first_name, last_name, role FROM users ORDER BY role DESC;" | column -t
    else
        echo -e "${RED}✗${NC} Expected 6 users, found: $USER_COUNT"
    fi
fi
echo ""

echo "4. Verifying MongoDB Order Data..."
echo "-------------------------------------------"
if [ $MONGO_RUNNING -eq 0 ]; then
    ORDER_COUNT=$(mongo_query "db.orders.countDocuments()")
    if [ "$ORDER_COUNT" = "10" ]; then
        echo -e "${GREEN}✓${NC} Order database initialized successfully"
        echo "  - Orders loaded: $ORDER_COUNT"
        echo ""
        echo "  Orders by status:"
        mongo_query "db.orders.aggregate([{\\$group: {_id: '\\$status', count: {\\$sum: 1}}}, {\\$sort: {_id: 1}}]).forEach(doc => print('  ' + doc._id + ': ' + doc.count))"
    else
        echo -e "${RED}✗${NC} Expected 10 orders, found: $ORDER_COUNT"
    fi
fi
echo ""

echo "5. Testing API Endpoints..."
echo "-------------------------------------------"

# Check if services are responding
echo "Testing Catalog Service (port 8081)..."
if curl -s http://localhost:8081/api/v1/books > /dev/null 2>&1; then
    CATALOG_COUNT=$(curl -s http://localhost:8081/api/v1/books | grep -o '"id"' | wc -l)
    echo -e "${GREEN}✓${NC} Catalog service is responding (found $CATALOG_COUNT books)"
else
    echo -e "${YELLOW}⚠${NC} Catalog service not responding (may still be starting up)"
fi

echo "Testing User Service (port 8083)..."
if curl -s http://localhost:8083/api/v1/auth/login > /dev/null 2>&1; then
    echo -e "${GREEN}✓${NC} User service is responding"
else
    echo -e "${YELLOW}⚠${NC} User service not responding (may still be starting up)"
fi

echo "Testing Order Service (port 8082)..."
if curl -s http://localhost:8082/api/v1/orders > /dev/null 2>&1; then
    echo -e "${GREEN}✓${NC} Order service is responding"
else
    echo -e "${YELLOW}⚠${NC} Order service not responding (may still be starting up)"
fi
echo ""

echo "6. Test Credentials Available..."
echo "-------------------------------------------"
echo "Admin Account:"
echo "  Email: admin@bookstore.com"
echo "  Password: admin123"
echo ""
echo "Customer Accounts (password: customer123):"
echo "  - john.doe@bookstore.com"
echo "  - jane.smith@bookstore.com"
echo "  - mike.wilson@bookstore.com"
echo "  - sarah.jones@bookstore.com"
echo "  - alex.brown@bookstore.com"
echo ""

echo "=========================================="
echo "Verification Complete!"
echo "=========================================="
echo ""
echo "Next Steps:"
echo "1. Access Swagger UI:"
echo "   - Catalog: http://localhost:8081/swagger-ui.html"
echo "   - Order: http://localhost:8082/swagger-ui.html"
echo "   - User: http://localhost:8083/swagger-ui.html"
echo ""
echo "2. Test authentication:"
echo "   curl -X POST http://localhost:8083/api/v1/auth/login \\"
echo "     -H 'Content-Type: application/json' \\"
echo "     -d '{\"email\":\"admin@bookstore.com\",\"password\":\"admin123\"}'"
echo ""
echo "3. Browse books:"
echo "   curl http://localhost:8081/api/v1/books"
echo ""
