# Progress Log

## Latest Status
Testing improvements plan implemented (PLAN-03-TESTING.md completed) - E2E tests with Playwright, contract tests with Spring Cloud Contract, API Gateway tests enhanced, CI workflow created. All test infrastructure ready for execution (2026-03-10).

## Integration Test Run — 2026-03-09

| Test Class | Module | Tests | Result |
|------------|--------|-------|--------|
| BookIntegrationTest | catalog-service | 13 | ✅ PASS |
| CatalogImportJobIntegrationTest | catalog-service | 5 | ✅ PASS |
| OrderIntegrationTest | order-service | 7 | ✅ PASS |
| OrderCatalogIntegrationTest | order-service | 4 | ✅ PASS |
| UserServiceIntegrationTest | user-service | 2 | ✅ PASS |

### Fixes applied
- Upgraded Testcontainers 1.19.3→1.21.4 (Docker Engine 29.x API v1.44+ compat)
- Removed duplicate `@Bean jwtTokenProvider` in catalog/order JwtConfig (BeanDefinitionOverrideException)
- Fixed `fetchBookFromCatalog` catch-all wrapping all exceptions as InvalidRequestException (400)
- Fixed `CatalogClientFallback` to throw RuntimeException (500) instead of InvalidRequestException (400)
- Added `url` attribute to `CatalogClient` Feign client for test configurability
- Configured WireMock port routing + Feign timeouts in integration test profiles
- Set `userId` in `OrderCatalogIntegrationTest.createOrderDto` (missing @NotNull field)
- Updated `.testcontainers.properties` Docker host to `dockerDesktopLinuxEngine`

## Unit Test Run — 2026-03-09

| Service | Tests | Passed | Failed | Skipped |
|---------|-------|--------|--------|---------|
| shared-common | 56 | 56 | 0 | 0 |
| api-gateway | 29 | 29 | 0 | 0 |
| catalog-service | 101 | 99 | 0 | 2 |
| order-service | 114 | 112 | 0 | 2 |
| user-service | 40 | 39 | 0 | 1 |
| admin-ui | 43 | 43 | 0 | 0 |
| **Total** | **383** | **378** | **0** | **5** |
