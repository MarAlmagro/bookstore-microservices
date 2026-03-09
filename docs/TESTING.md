# Testing Guide

This document covers the testing strategy, how to run tests, coverage tools, and test data management for the Bookstore Microservices project.

---

## Test Inventory

The project contains **261+ tests** across all services.

| Service | Unit Tests | Integration Tests | Total |
|---------|-----------|-------------------|-------|
| **catalog-service** | BookServiceTest (22), BookControllerTest (20), BookImportProcessorTest (7) | BookIntegrationTest (14), CatalogImportJobIntegrationTest (5) | ~68 |
| **order-service** | OrderServiceTest (12), OrderControllerTest (8), MainframeOrderLineAggregatorTest (9) | OrderIntegrationTest (6) | ~35 |
| **user-service** | UserServiceTest (6), AuthServiceTest (6), UserControllerTest (5), AuthControllerTest (3) | UserServiceIntegrationTest (2) | ~22 |
| **admin-ui** | BookControllerTest (5), DashboardControllerTest (2) | AdminUiApplicationTests (1) | ~8 |
| **shared-common** | Various DTO/exception/utility tests | — | 128+ |

---

## Running Tests

### All Tests

```bash
mvn clean test
```

### Single Module

```bash
mvn test -pl catalog-service
mvn test -pl order-service
mvn test -pl user-service
mvn test -pl shared-common
mvn test -pl admin-ui
```

### Single Test Class

```bash
mvn test -pl catalog-service -Dtest=BookServiceTest
```

### Skip Tests During Build

```bash
mvn clean install -DskipTests
```

---

## Test Profiles and Databases

Each service uses an in-memory or embedded database for testing — no Docker containers required for unit/integration tests.

| Service | Test Profile | Database | Config File |
|---------|-------------|----------|-------------|
| catalog-service | `test` | H2 in-memory | `application-test.yml` |
| order-service | `test` | Embedded MongoDB (Flapdoodle) | `application.yml` |
| user-service | `test` | H2 in-memory | `application-test.yml` |

---

## Code Coverage (JaCoCo)

### Generate Coverage Reports

```bash
mvn test -P jacoco
```

Reports are generated at `<module>/target/site/jacoco/index.html` for each module.

### View Reports

Open in a browser:

```
catalog-service/target/site/jacoco/index.html
order-service/target/site/jacoco/index.html
user-service/target/site/jacoco/index.html
shared-common/target/site/jacoco/index.html
```

### Coverage Targets

| Metric | Target |
|--------|--------|
| Line Coverage | ≥ 80% |
| Branch Coverage | ≥ 70% |

---

## Test Fixtures

Pre-built test data and request samples are available in `test-fixtures/`:

| Artifact | Location | Purpose |
|----------|----------|---------|
| `catalog-requests.json` | `test-fixtures/requests/` | Sample API request payloads for catalog |
| `order-requests.json` | `test-fixtures/requests/` | Sample API request payloads for orders |
| `user-requests.json` | `test-fixtures/requests/` | Sample API request payloads for users |
| `batch_catalog_import.csv` | `test-fixtures/scenarios/` | CSV file for batch import testing |
| `BookTestFixtures.java` | `catalog-service/src/test/` | Factory class for Book/BookDto test objects |

---

## Shell-Based API Tests

The `scripts/` directory contains bash scripts for end-to-end API testing against a running stack:

| Script | Purpose |
|--------|---------|
| `test-catalog.sh` | Test catalog CRUD via API Gateway |
| `test-order.sh` | Test order operations via API Gateway |
| `test-user.sh` | Test auth and user endpoints |
| `test-integration.sh` | Cross-service integration scenarios |
| `test-resilience.sh` | Resilience and error-handling tests |
| `validate-API-gateway.sh` | Gateway routing and rate-limit validation |
| `verify-init.sh` | Verify database initialization scripts |

Run them against a live stack:

```bash
# Ensure the stack is running
docker-compose up -d

# Run all API tests
bash scripts/test-integration.sh
```

---

## Test Data (Seeded Databases)

When Docker containers start for the first time, init scripts populate the databases. See [scripts/README.md](../scripts/README.md) for full details.

| Database | Script | Records |
|----------|--------|---------|
| MySQL | `init-catalog.sql` | 20 books |
| PostgreSQL | `init-users.sql` | 6 users (1 admin + 5 customers) |
| MongoDB | `init-orders.js` | 10 orders |

To reset test data:

```bash
docker-compose down -v
docker-compose up --build -d
```

---

## Writing New Tests

### Naming Convention

- Unit tests: `<Class>Test.java`
- Integration tests: `<Feature>IntegrationTest.java`
- Use descriptive method names: `shouldReturnBookWhenIdExists()`, `shouldReturn404WhenBookNotFound()`

### Pattern

Follow Arrange-Act-Assert (given/when/then):

```java
@Test
void shouldReturnBookWhenIdExists() {
    // Given
    Book book = BookTestFixtures.createBook();
    when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

    // When
    BookDto result = bookService.findById(1L);

    // Then
    assertThat(result.getTitle()).isEqualTo("Effective Java");
}
```

### UI Testing (admin-ui)

All interactive elements must include a `data-testid` attribute:

```html
<button data-testid="book-save-submit">Save</button>
```

---

## Known Gaps

| Area | Status | Notes |
|------|--------|-------|
| Testcontainers (real DBs) | Planned | Replace H2/Flapdoodle with Testcontainers for production parity |
| Contract Tests | Missing | Spring Cloud Contract for inter-service contracts |
| Performance Tests | Missing | JMeter or Gatling for load testing |
| Mutation Testing | Missing | PIT mutation testing for test quality |
| API Gateway tests | Missing | Route validation, JWT propagation |

---

[← Back to Documentation Hub](README.md) · [Deployment & Operations →](DEPLOYMENT.md)
