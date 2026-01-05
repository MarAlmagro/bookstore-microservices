# Phase 10.5: Manual Test Procedures

This document provides step-by-step instructions for manual validation tests that require running services.

---

## Prerequisites

Before running manual tests, ensure:

1. **Docker and Docker Compose** are installed and running
2. **All microservices** are built successfully
3. **PostgreSQL and MongoDB** databases are accessible
4. **Maven** is installed at `c:\opt\apache-maven\bin`

---

## Test Environment Setup

### Step 1: Build All Services

```bash
# Navigate to project root
cd c:\opt\git\bookstore-microservices

# Build with Maven
c:\opt\apache-maven\bin\mvn clean package -DskipTests

# Build Admin UI frontend assets
cd admin-ui
npm install
npm run build:css
cd ..
```

### Step 2: Start All Services

```bash
# Start all services with Docker Compose
docker-compose up -d

# Wait for services to start (approximately 60 seconds)
# Check service health
docker-compose ps
```

### Step 3: Verify Service Registration

```bash
# Check Eureka Dashboard
# Open browser: http://localhost:8761

# Verify registered services:
# - API-GATEWAY
# - CATALOG-SERVICE
# - ORDER-SERVICE
# - USER-SERVICE
# - ADMIN-UI
```

---

## Manual Test 1: E2E Data Flow (10.5.2.2)

**Objective**: Verify complete data flow from Admin UI → API Gateway → Catalog Service → Database

### Test Steps

#### 1. Access Admin UI

1. Open browser and navigate to: `http://localhost:8090/admin`
2. You should see the login page
3. **Expected**: Login form with username and password fields

#### 2. Login

1. Enter credentials:
   - Username: `admin`
   - Password: `admin123`
2. Click "Sign In" button
3. **Expected**: Redirect to dashboard at `/admin/dashboard`

#### 3. Navigate to Books Management

1. Click "Books" in the sidebar navigation
2. **Expected**: Books list page displays at `/admin/books`
3. **Expected**: Table shows existing books (if any)

#### 4. Create New Book

1. Click "Add Book" button (top right)
2. **Expected**: Book form page displays
3. Fill in the form:
   - **ISBN**: `TEST-PHASE-10-5`
   - **Title**: `Phase 10.5 Validation Test Book`
   - **Author**: `Validation Team`
   - **Description**: `This book validates the E2E data flow from Admin UI through API Gateway to Catalog Service and PostgreSQL database.`
   - **Price**: `29.99`
   - **Stock Quantity**: `100`
   - **Category**: `Testing`
4. Click "Save" button
5. **Expected**: 
   - Redirect to books list
   - Success message: "Book created successfully"
   - New book appears in the table

#### 5. Verify Book in List

1. Search for the book using ISBN: `TEST-PHASE-10-5`
2. **Expected**: Book appears in search results with correct data

#### 6. Verify Database Persistence

```bash
# Connect to PostgreSQL container
docker exec -it bookstore-postgres psql -U postgres -d catalog_db

# Query for the test book
SELECT * FROM books WHERE isbn = 'TEST-PHASE-10-5';

# Expected output: One row with matching data
# Exit psql
\q
```

#### 7. Verify Gateway Routing

```bash
# Check API Gateway logs for routing
docker logs api-gateway | grep "catalog-service" | tail -20

# Expected: Log entries showing requests routed to catalog-service
# Look for: POST /api/books
```

#### 8. Verify Catalog Service Processing

```bash
# Check Catalog Service logs
docker logs catalog-service | grep "POST /api/books" | tail -10

# Expected: Log entries showing book creation
# Should see: 201 Created response
```

### Test Results

- [ ] Admin UI accessible at port 8090
- [ ] Login successful with admin credentials
- [ ] Books list page loads correctly
- [ ] Book creation form displays
- [ ] Book saves successfully
- [ ] Success message appears
- [ ] Book appears in list with correct data
- [ ] Book persists in PostgreSQL database
- [ ] API Gateway routes request correctly
- [ ] Catalog Service processes request successfully
- [ ] HTTP 201/200 response received

### Cleanup

```bash
# Delete test book via UI or database
docker exec -it bookstore-postgres psql -U postgres -d catalog_db -c "DELETE FROM books WHERE isbn = 'TEST-PHASE-10-5';"
```

---

## Manual Test 2: i18n Language Switching (10.5.1.2)

**Objective**: Verify internationalization works correctly with Spanish translations

### Test Steps

#### 1. Access Dashboard in English

1. Navigate to: `http://localhost:8090/admin/dashboard`
2. Login if needed
3. **Expected**: All labels in English
   - "Dashboard"
   - "Total Books"
   - "Total Orders"
   - "Total Users"
   - "Revenue"

#### 2. Switch to Spanish

1. Click "ES" button in the top right corner
2. **Expected**: URL changes to `/admin/dashboard?lang=es`
3. **Expected**: All labels change to Spanish:
   - "Panel"
   - "Total de Libros"
   - "Total de Pedidos"
   - "Total de Usuarios"
   - "Ingresos"

#### 3. Navigate to Books in Spanish

1. Click "Libros" in sidebar (was "Books")
2. **Expected**: Books page in Spanish
   - "Gestión de Libros" (Book Management)
   - "Agregar Libro" (Add Book)
   - "Buscar libros" (Search books)
   - Table headers in Spanish

#### 4. Switch Back to English

1. Click "EN" button in top right
2. **Expected**: All labels return to English

### Test Results

- [ ] Dashboard displays in English by default
- [ ] Language switcher visible and accessible
- [ ] Clicking "ES" switches to Spanish
- [ ] All labels translate correctly
- [ ] Navigation maintains language preference
- [ ] Clicking "EN" switches back to English
- [ ] No missing translations (no keys displayed)

---

## Manual Test 3: Responsive Design (10.5.1.1)

**Objective**: Verify responsive design works on mobile and desktop viewports

### Test Steps

#### 1. Desktop View (1920x1080)

1. Open browser at full screen (1920x1080 or larger)
2. Navigate to: `http://localhost:8090/admin/dashboard`
3. **Expected**:
   - Sidebar visible on left
   - Stats grid shows 4 columns
   - Charts display side by side (2 columns)
   - All content fits without horizontal scroll

#### 2. Tablet View (768x1024)

1. Open browser DevTools (F12)
2. Enable device toolbar (Ctrl+Shift+M)
3. Select iPad or similar (768px width)
4. **Expected**:
   - Sidebar visible
   - Stats grid shows 2 columns
   - Charts stack vertically (1 column)
   - No horizontal scroll

#### 3. Mobile View (375x667)

1. In DevTools, select iPhone or similar (375px width)
2. **Expected**:
   - Sidebar hidden (only main content visible)
   - Stats grid shows 1 column (stacked)
   - Charts stack vertically
   - Table scrolls horizontally
   - Language switcher remains accessible

#### 4. Books Table on Mobile

1. Navigate to Books page on mobile view
2. **Expected**:
   - Table has horizontal scroll
   - Search and "Add Book" button stack vertically
   - All functionality accessible

### Test Results

- [ ] Desktop view displays correctly (4-column grid)
- [ ] Tablet view displays correctly (2-column grid)
- [ ] Mobile view hides sidebar
- [ ] Mobile view shows 1-column grid
- [ ] Tables scroll horizontally on mobile
- [ ] All interactive elements accessible on all sizes
- [ ] No layout breaks at any viewport size

---

## Manual Test 4: Accessibility (10.5.1.3)

**Objective**: Verify accessibility features and test hooks

### Test Steps

#### 1. Keyboard Navigation

1. Navigate to: `http://localhost:8090/admin/dashboard`
2. Press Tab key repeatedly
3. **Expected**:
   - Focus moves through all interactive elements
   - Focus indicator visible on all elements
   - Tab order is logical (top to bottom, left to right)
   - Can navigate entire page without mouse

#### 2. Screen Reader Compatibility

1. Open browser DevTools → Elements
2. Inspect sidebar navigation
3. **Expected**:
   - `<nav>` element has `role="navigation"`
   - `<nav>` has `aria-label="Main navigation"`
   - All links have descriptive text

#### 3. Form Accessibility

1. Navigate to: `http://localhost:8090/admin/books/new`
2. Inspect form elements
3. **Expected**:
   - All inputs have associated `<label>` elements
   - Labels use `for` attribute matching input `id`
   - Required fields have `required` attribute
   - Form has `data-testid="book-form"`

#### 4. Test Automation Hooks

1. Open browser DevTools → Elements
2. Search for `data-testid` attributes
3. **Expected**: Find test IDs for:
   - `sidebar`
   - `topbar`
   - `nav-dashboard`, `nav-books`, `nav-logout`
   - `lang-en`, `lang-es`
   - `books-table`, `books-add-button`
   - `book-save`, `book-cancel`
   - All form inputs

### Test Results

- [ ] All interactive elements keyboard accessible
- [ ] Focus indicators visible
- [ ] Semantic HTML elements used
- [ ] ARIA roles present
- [ ] Form labels properly associated
- [ ] Sufficient data-testid attributes (30+)
- [ ] Test hooks on all critical actions

---

## Manual Test 5: RBAC - Unauthorized Access (10.5.3.1)

**Objective**: Verify role-based access control prevents unauthorized access

### Test Steps

#### 1. Attempt Unauthenticated Access

1. Open browser in incognito/private mode
2. Navigate to: `http://localhost:8090/admin/dashboard`
3. **Expected**: Redirect to login page

#### 2. Test Invalid Credentials

1. On login page, enter:
   - Username: `invalid`
   - Password: `invalid`
2. Click "Sign In"
3. **Expected**: Error message "Invalid username or password"

#### 3. Test Session Timeout

1. Login successfully
2. Wait for session timeout (30 minutes) or clear cookies
3. Try to access: `http://localhost:8090/admin/books`
4. **Expected**: Redirect to login page

### Test Results

- [ ] Unauthenticated users redirected to login
- [ ] Invalid credentials rejected
- [ ] Session timeout enforced
- [ ] Protected routes require authentication

**Note**: Full RBAC testing with `ROLE_CUSTOMER` requires User Service integration and JWT authentication, which may not be fully implemented yet.

---

## Manual Test 6: JWT Propagation (10.5.3.2)

**Objective**: Verify JWT tokens propagate through API Gateway to backend services

### Test Steps

#### 1. Monitor Gateway Logs

```bash
# In one terminal, tail Gateway logs
docker logs -f api-gateway | grep -i "authorization"
```

#### 2. Monitor Catalog Service Logs

```bash
# In another terminal, tail Catalog Service logs
docker logs -f catalog-service | grep -i "jwt\|authorization"
```

#### 3. Perform Book Operation

1. In browser, login to Admin UI
2. Create or update a book
3. Watch the logs in both terminals

#### 4. Verify JWT in Logs

**Expected in Gateway logs**:
- Request headers include `Authorization: Bearer <token>`
- Token forwarded to downstream service

**Expected in Catalog Service logs**:
- Received request with JWT token
- Token validated successfully
- No 401 Unauthorized errors

### Test Results

- [ ] Admin UI includes JWT in requests
- [ ] API Gateway receives JWT
- [ ] API Gateway forwards JWT to Catalog Service
- [ ] Catalog Service validates JWT
- [ ] No 401 errors in logs
- [ ] Request completes successfully

---

## Test Completion Checklist

### Automated Tests
- [x] Responsive design classes verified
- [x] i18n configuration verified
- [x] Accessibility hooks verified
- [x] Design tokens verified
- [x] CSS token usage verified

### Manual Tests
- [ ] E2E data flow tested
- [ ] i18n language switching tested
- [ ] Responsive design tested
- [ ] Accessibility tested
- [ ] RBAC tested
- [ ] JWT propagation tested

### Documentation
- [ ] Test results documented
- [ ] Screenshots captured (optional)
- [ ] Issues logged (if any)
- [ ] Validation report completed

---

## Troubleshooting

### Services Won't Start

```bash
# Check Docker status
docker ps -a

# Check logs for errors
docker-compose logs

# Restart specific service
docker-compose restart <service-name>
```

### Admin UI Not Accessible

```bash
# Check if service is running
docker ps | grep admin-ui

# Check logs
docker logs admin-ui

# Verify port mapping
docker port admin-ui
```

### Database Connection Issues

```bash
# Check PostgreSQL
docker exec -it bookstore-postgres psql -U postgres -l

# Check MongoDB
docker exec -it bookstore-mongo mongosh --eval "db.adminCommand('ping')"
```

### Gateway Not Routing

```bash
# Check Eureka registration
curl http://localhost:8761/eureka/apps

# Check Gateway routes
curl http://localhost:8080/actuator/routes
```

---

## Next Steps After Testing

1. Update `phase-10.5-validation-tests.md` with manual test results
2. Capture screenshots of successful tests
3. Document any issues or failures
4. Create atomic commits for each validation step
5. Merge validation branch to develop
