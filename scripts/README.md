# Database Initialization Scripts

> Part of [Bookstore Microservices](../README.md) · [Documentation Hub](../docs/README.md)

This directory contains initialization scripts that automatically populate the databases with sample data when Docker containers are started.

## Scripts Overview

### 1. `init-catalog.sql` (MySQL - Catalog Service)
- **Database**: bookstore_catalog
- **Table**: books
- **Sample Data**: 20 books across various genres
  - 5 Programming & Technology books
  - 4 Fiction books
  - 3 Science Fiction & Fantasy books
  - 3 Business & Self-Help books
  - 2 Mystery & Thriller books
  - 2 Non-Fiction & Biography books
  - 1 Romance book

**Sample Books Include**:
- Effective Java by Joshua Bloch
- Clean Code by Robert C. Martin
- 1984 by George Orwell
- The Hobbit by J.R.R. Tolkien
- Atomic Habits by James Clear
- And 15 more...

### 2. `init-users.sql` (PostgreSQL - User Service)
- **Database**: bookstore_users
- **Table**: users
- **Sample Data**: 6 user accounts (1 admin + 5 customers)

**Admin Account**:
- Email: admin@bookstore.com
- Password: admin123
- Role: ADMIN

**Customer Accounts** (all use password: customer123):
- john.doe@bookstore.com - John Doe
- jane.smith@bookstore.com - Jane Smith
- mike.wilson@bookstore.com - Mike Wilson
- sarah.jones@bookstore.com - Sarah Jones
- alex.brown@bookstore.com - Alex Brown

**Note**: All passwords are BCrypt hashed with strength 10 for security.

### 3. `init-orders.js` (MongoDB - Order Service)
- **Database**: bookstore_orders
- **Collection**: orders
- **Sample Data**: 10 orders with various statuses

**Order Statuses**:
- PENDING: 1 order
- CONFIRMED: 2 orders
- SHIPPED: 2 orders
- DELIVERED: 4 orders
- CANCELLED: 1 order

**Features**:
- Orders linked to users (userId references PostgreSQL users)
- Orders linked to books (bookId references MySQL catalog)
- Realistic order dates and amounts
- Shipping addresses included
- Status-specific date fields (confirmedDate, shippedDate, deliveryDate, cancelledDate)

## How It Works

### Automatic Initialization
When you start the Docker containers using `docker-compose up`, these scripts are automatically executed:

1. **MySQL Container**: Runs `init-catalog.sql` on first startup
2. **PostgreSQL Container**: Runs `init-users.sql` on first startup
3. **MongoDB Container**: Runs `init-orders.js` on first startup

The scripts are mounted as volumes in `docker-compose.yml`:
```yaml
mysql-catalog:
  volumes:
    - ./scripts/init-catalog.sql:/docker-entrypoint-initdb.d/init-catalog.sql

postgres-user:
  volumes:
    - ./scripts/init-users.sql:/docker-entrypoint-initdb.d/init-users.sql

mongodb-order:
  volumes:
    - ./scripts/init-orders.js:/docker-entrypoint-initdb.d/init-orders.js
```

### When Scripts Execute
- **First Run**: Scripts execute when the database is created for the first time
- **Clean Start**: To re-run initialization, remove volumes: `docker-compose down -v`

## Verification Commands

### Verify Data Loaded Successfully

#### 1. Check MySQL Catalog Data
```bash
# Connect to MySQL container
docker exec -it bookstore-mysql mysql -u root -ppassword

# Inside MySQL shell
USE bookstore_catalog;
SELECT COUNT(*) FROM books;  # Should return 20
SELECT isbn, title, author, price FROM books LIMIT 5;
EXIT;
```

#### 2. Check PostgreSQL User Data
```bash
# Connect to PostgreSQL container
docker exec -it bookstore-postgres psql -U postgres -d bookstore_users

# Inside psql shell
SELECT COUNT(*) FROM users;  # Should return 6
SELECT email, first_name, last_name, role FROM users;
\q
```

#### 3. Check MongoDB Order Data
```bash
# Connect to MongoDB container
docker exec -it bookstore-mongodb mongosh bookstore_orders

# Inside mongosh shell
db.orders.countDocuments()  # Should return 10
db.orders.find({}, {userId: 1, totalAmount: 1, status: 1})
db.orders.aggregate([{$group: {_id: '$status', count: {$sum: 1}}}])
exit
```

## Testing with API Endpoints

**Phase 7 Update**: All requests now go through API Gateway on port 8080.

### Test Authentication (via API Gateway - Port 8080)
```bash
# Login as admin
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@bookstore.com","password":"admin123"}'

# Login as customer
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john.doe@bookstore.com","password":"customer123"}'
```

### Test Catalog Service (via API Gateway - Port 8080)
```bash
# Get all books
curl http://localhost:8080/api/v1/books

# Get book by ID
curl http://localhost:8080/api/v1/books/1

# Search books by category
curl "http://localhost:8080/api/v1/books/search?category=Programming"
```

### Test Order Service (via API Gateway - Port 8080)
```bash
# Get all orders (requires authentication)
curl http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Get order by ID
curl http://localhost:8080/api/v1/orders/ORDER_ID \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Direct Service Access (for testing without Gateway)
If you need to test services directly (bypassing Gateway):
- User Service: http://localhost:8083/api/v1
- Catalog Service: http://localhost:8081/api/v1
- Order Service: http://localhost:8082/api/v1

## Resetting Sample Data

To start fresh with the sample data:

```bash
# Stop and remove all containers and volumes
docker-compose down -v

# Rebuild and start containers (scripts will run again)
docker-compose up --build -d

# Wait for services to be healthy
docker-compose ps

# Verify data loaded
# (Use verification commands above)
```

## Customizing Sample Data

To modify the sample data:

1. Edit the appropriate script file in this directory
2. Remove existing volumes: `docker-compose down -v`
3. Restart containers: `docker-compose up -d`
4. Verify new data loaded

## Important Notes

- **First-time only**: Init scripts only run when the database is first created
- **Volume persistence**: Data persists in Docker volumes between restarts
- **Clean slate**: Use `docker-compose down -v` to remove volumes and reset data
- **BCrypt passwords**: User passwords are properly hashed for security
- **Foreign keys**: Order data references users (by userId) and books (by bookId)
- **Realistic data**: All sample data is designed for realistic testing scenarios

## Troubleshooting

### Scripts Not Running
- Ensure Docker volumes are removed: `docker-compose down -v`
- Check file permissions (scripts must be readable)
- View container logs: `docker-compose logs mysql-catalog`

### Data Not Appearing
- Check if services are healthy: `docker-compose ps`
- View service logs: `docker-compose logs [service-name]`
- Verify scripts are mounted: `docker inspect bookstore-mysql`

### Password Authentication Fails
- Verify BCrypt hashes are correct in `init-users.sql`
- Check that user service is encoding passwords with BCrypt
- Ensure password matches expected format (10-72 characters)

## See Also

- [Getting Started](../docs/GETTING-STARTED.md) — Full setup and run instructions
- [API Reference](../docs/API-REFERENCE.md) — Endpoint catalog and Swagger UI links
- [Deployment & Operations](../docs/DEPLOYMENT.md) — Docker volumes, data persistence
- [Documentation Hub](../docs/README.md) — Index of all project documentation
