# Phase 10.5 Validation - Quick Start Guide

## Run Automated Tests

### Windows (PowerShell)
```powershell
cd c:\opt\git\bookstore-microservices
.\test-data\validation\run-validation-tests.ps1
```

### Linux/Mac (Bash)
```bash
cd /path/to/bookstore-microservices
chmod +x test-data/validation/run-validation-tests.sh
./test-data/validation/run-validation-tests.sh
```

**Expected Result**: 18/20 tests pass (90%)

---

## Run Manual Tests

### 1. Start Services
```bash
# Build all services
c:\opt\apache-maven\bin\mvn clean package -DskipTests

# Build Admin UI frontend
cd admin-ui
npm install
npm run build:css
cd ..

# Start all services
docker-compose up -d

# Wait 60 seconds for Eureka registration
```

### 2. Access Admin UI
- URL: http://localhost:8090/admin
- Username: `admin`
- Password: `admin123`

### 3. Test E2E Data Flow
1. Navigate to Books section
2. Click "Add Book"
3. Fill form with test data:
   - ISBN: `TEST-PHASE-10-5`
   - Title: `Phase 10.5 Validation Test Book`
   - Author: `Validation Team`
   - Price: `29.99`
   - Stock: `100`
4. Click "Save"
5. Verify success message
6. Verify book in list

### 4. Test i18n
1. Click "ES" button (top right)
2. Verify Spanish labels
3. Click "EN" to switch back

### 5. Test Responsive Design
1. Open DevTools (F12)
2. Enable device toolbar (Ctrl+Shift+M)
3. Test viewports:
   - Desktop: 1920x1080
   - Tablet: 768x1024
   - Mobile: 375x667

### 6. Verify Database
```bash
docker exec -it bookstore-postgres psql -U postgres -d catalog_db
SELECT * FROM books WHERE isbn = 'TEST-PHASE-10-5';
\q
```

### 7. Check Logs
```bash
# Gateway logs
docker logs api-gateway | grep "catalog-service"

# Catalog Service logs
docker logs catalog-service | grep "POST /api/books"
```

---

## Test Results Location

- **Automated Results**: Console output from validation script
- **Detailed Documentation**: `test-data/validation/phase-10.5-validation-tests.md`
- **Manual Procedures**: `test-data/validation/manual-test-procedures.md`
- **Summary Report**: `test-data/validation/VALIDATION_SUMMARY.md`

---

## Troubleshooting

### Services Won't Start
```bash
docker-compose down
docker-compose up -d
docker-compose logs
```

### Admin UI Not Accessible
```bash
docker logs admin-ui
docker port admin-ui
```

### Build Failures
```bash
c:\opt\apache-maven\bin\mvn clean install -DskipTests
```

---

## Success Criteria

- ✅ 18/20 automated tests pass
- ✅ Admin UI accessible and responsive
- ✅ Language switching works (EN/ES)
- ✅ Book CRUD operations work
- ✅ Data persists in database
- ✅ Gateway routes requests correctly
- ✅ No 401/403 errors in logs

---

## Next Steps After Validation

1. Document manual test results in `VALIDATION_SUMMARY.md`
2. Create atomic commits:
   ```bash
   git add test-data/validation/
   git commit -m "test: verify responsive design, i18n support, and a11y hooks"
   git commit -m "test: verify shared design token application and E2E book CRUD"
   git commit -m "test: verify admin-ui RBAC and JWT propagation"
   ```
3. Update `.windsurf/plan/phase-10.5-validation-plan.md` with results
4. Merge to develop branch
