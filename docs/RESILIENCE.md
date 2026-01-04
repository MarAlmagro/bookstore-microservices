# Resilience Patterns Implementation Guide

## Overview

This document describes the resilience patterns implemented in the Order Service to ensure fault tolerance and graceful degradation when communicating with the Catalog Service. The implementation uses **Resilience4j** and **OpenFeign** to provide Circuit Breaker, Retry, and Bulkhead patterns.

## Architecture

### Communication Flow

```
Order Service → OpenFeign Client → Circuit Breaker → Retry → Bulkhead → Catalog Service
                                         ↓
                                    Fallback (if failure)
```

## Implemented Patterns

### 1. Circuit Breaker

The Circuit Breaker pattern prevents cascading failures by stopping requests to a failing service and providing fast failure responses.

**Configuration** (`application.yml`):
```yaml
resilience4j:
  circuitbreaker:
    instances:
      catalogService:
        slidingWindowSize: 10
        failureRateThreshold: 50.0
        waitDurationInOpenState: 10000
        permittedNumberOfCallsInHalfOpenState: 3
        registerHealthIndicator: true
        slidingWindowType: COUNT_BASED
```

**States**:
- **CLOSED**: Normal operation, requests pass through
- **OPEN**: Failure threshold exceeded, requests fail fast with fallback
- **HALF_OPEN**: Testing if service recovered, allows limited requests

**Thresholds**:
- Sliding window: 10 calls (count-based)
- Failure rate threshold: 50% (5 out of 10 failures triggers OPEN state)
- Wait duration in OPEN state: 10 seconds
- Permitted calls in HALF_OPEN: 3 successful calls to transition back to CLOSED

### 2. Retry Pattern

Automatic retry with exponential backoff for transient failures.

**Configuration**:
```yaml
resilience4j:
  retry:
    instances:
      catalogService:
        maxAttempts: 3
        waitDuration: 2000
        enableExponentialBackoff: true
        exponentialBackoffMultiplier: 2
```

**Behavior**:
- Maximum 3 attempts per request
- Initial wait: 2 seconds
- Exponential backoff: 2s → 4s → 8s

### 3. Bulkhead Pattern

Limits concurrent calls to prevent resource exhaustion.

**Configuration**:
```yaml
resilience4j:
  bulkhead:
    instances:
      catalogService:
        maxConcurrentCalls: 10
        maxWaitDuration: 0
```

**Behavior**:
- Maximum 10 concurrent calls to Catalog Service
- No waiting queue (maxWaitDuration: 0)
- Excess requests fail immediately

## Implementation Details

### OpenFeign Client

**Location**: `order-service/src/main/java/com/bookstore/order/client/CatalogClient.java`

```java
@FeignClient(name = "catalog-service", fallback = CatalogClientFallback.class)
public interface CatalogClient {
    @GetMapping("/api/v1/books/{id}")
    BookDTO getBookById(@PathVariable("id") Long id);
}
```

### Fallback Implementation

**Location**: `order-service/src/main/java/com/bookstore/order/client/CatalogClientFallback.java`

When the circuit breaker is OPEN or calls fail, the fallback returns a user-friendly error message:

```java
@Component
public class CatalogClientFallback implements CatalogClient {
    @Override
    public BookDTO getBookById(Long id) {
        log.error("Catalog service is unavailable. Circuit breaker activated for book id: {}", id);
        throw new InvalidRequestException("Catalog service is temporarily unavailable. Please try again later.");
    }
}
```

### Service Layer Integration

**Location**: `order-service/src/main/java/com/bookstore/order/service/OrderServiceImpl.java`

```java
@CircuitBreaker(name = "catalogService")
@Retry(name = "catalogService")
@Bulkhead(name = "catalogService")
private BookDTO fetchBookFromCatalog(Long bookId) {
    // Feign client call with resilience patterns applied
}
```

### JWT Propagation

**Location**: `order-service/src/main/java/com/bookstore/order/config/FeignClientInterceptor.java`

Ensures authentication tokens are propagated to downstream services:

```java
@Component
public class FeignClientInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String authToken = attributes.getRequest().getHeader("Authorization");
            if (authToken != null) {
                template.header("Authorization", authToken);
            }
        }
    }
}
```

## Observability

### Actuator Endpoints

Circuit breaker health and metrics are exposed via Spring Boot Actuator:

- **Health**: `http://localhost:8082/actuator/health`
- **Metrics**: `http://localhost:8082/actuator/prometheus`
- **Circuit Breakers**: `http://localhost:8082/actuator/circuitbreakers`

### Prometheus Metrics

Key metrics exported for monitoring:

- `resilience4j_circuitbreaker_state`: Current circuit breaker state (0=CLOSED, 1=OPEN, 2=HALF_OPEN)
- `resilience4j_circuitbreaker_failure_rate`: Failure rate percentage
- `resilience4j_circuitbreaker_calls_seconds_count`: Total calls by kind (successful, failed, not_permitted)
- `resilience4j_retry_calls_seconds_count`: Retry attempts by kind
- `resilience4j_bulkhead_available_concurrent_calls`: Available concurrent call slots
- `resilience4j_bulkhead_max_allowed_concurrent_calls`: Maximum allowed concurrent calls

### Grafana Dashboard

A pre-configured Grafana dashboard is available at:
- **Location**: `test-data/grafana/resilience4j-dashboard.json`
- **Panels**:
  - Circuit Breaker State
  - Failure Rate
  - Call Rates (successful/failed/not_permitted)
  - Retry Attempts
  - Bulkhead Concurrent Calls
  - Call Duration (p95, p99)

## Testing

### Automated Chaos Testing

**Script**: `scripts/test-resilience.sh`

The script performs comprehensive resilience testing:

1. **Normal Operation**: Verify circuit breaker is CLOSED
2. **Failure Injection**: Stop catalog service to simulate failure
3. **Circuit Opening**: Generate failures to trigger OPEN state
4. **Fast Failure**: Verify fallback is invoked when circuit is OPEN
5. **Recovery**: Restart catalog service and verify HALF_OPEN transition
6. **Circuit Closing**: Verify successful requests close the circuit

**Usage**:
```bash
cd scripts
chmod +x test-resilience.sh
./test-resilience.sh
```

### Manual Testing

#### Test Circuit Breaker Opening

1. Start all services: `docker-compose up -d`
2. Create orders successfully (circuit CLOSED)
3. Stop catalog service: `docker-compose stop catalog-service`
4. Send 10 order requests (5+ will fail, triggering OPEN state)
5. Observe fast failures with fallback message

#### Test Circuit Breaker Recovery

1. Wait 10 seconds (waitDurationInOpenState)
2. Restart catalog service: `docker-compose start catalog-service`
3. Send 3 successful requests (permittedNumberOfCallsInHalfOpenState)
4. Circuit transitions to CLOSED
5. Normal operation resumes

#### Monitor Metrics

```bash
# Check circuit breaker health
curl http://localhost:8082/actuator/health | jq '.components.circuitBreakers'

# View Prometheus metrics
curl http://localhost:8082/actuator/prometheus | grep resilience4j

# Check circuit breaker state
curl http://localhost:8082/actuator/circuitbreakers
```

## Failure Scenarios

### Scenario 1: Catalog Service Down

**Behavior**:
- First 5-10 requests attempt to reach catalog service (with retries)
- Circuit breaker opens after failure threshold (50%)
- Subsequent requests fail fast with fallback message
- No cascading failures to Order Service

**User Experience**:
- HTTP 400 Bad Request
- Message: "Catalog service is temporarily unavailable. Please try again later."

### Scenario 2: Slow Catalog Service

**Behavior**:
- Retry pattern attempts up to 3 times with exponential backoff
- Bulkhead limits concurrent calls to prevent resource exhaustion
- Circuit breaker may open if timeout failures exceed threshold

### Scenario 3: Intermittent Failures

**Behavior**:
- Retry pattern handles transient failures
- Circuit breaker remains CLOSED if failure rate < 50%
- Successful retries prevent circuit from opening

## Configuration Tuning

### Production Recommendations

For production environments, consider adjusting:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      catalogService:
        slidingWindowSize: 20              # Increase for more stable threshold
        failureRateThreshold: 40.0         # Lower for faster failure detection
        waitDurationInOpenState: 30000     # Longer wait before retry
        permittedNumberOfCallsInHalfOpenState: 5
  
  retry:
    instances:
      catalogService:
        maxAttempts: 5                     # More attempts for transient issues
        waitDuration: 1000                 # Faster initial retry
  
  bulkhead:
    instances:
      catalogService:
        maxConcurrentCalls: 25             # Scale based on load
```

### Development/Testing

Current configuration is optimized for testing and demonstration:
- Small sliding window (10) for quick circuit opening
- Short wait duration (10s) for faster testing cycles
- Moderate retry attempts (3) to observe behavior

## Dependencies

```xml
<!-- OpenFeign -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>

<!-- Resilience4j -->
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot2</artifactId>
    <version>1.7.0</version>
</dependency>

<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-micrometer</artifactId>
    <version>1.7.0</version>
</dependency>

<!-- Prometheus Metrics -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>

<!-- AOP (Required for Resilience4j annotations) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

## Best Practices

1. **Circuit Breaker Naming**: Use consistent naming across configuration and annotations
2. **Fallback Strategy**: Always provide meaningful fallback responses
3. **Monitoring**: Track circuit breaker state changes in production
4. **Testing**: Regularly test resilience patterns with chaos engineering
5. **Configuration**: Tune thresholds based on actual production metrics
6. **Logging**: Log circuit state transitions for debugging
7. **Alerts**: Set up alerts for circuit breaker OPEN state
8. **Documentation**: Keep configuration values documented and justified

## Troubleshooting

### Circuit Breaker Not Opening

- Verify failure rate exceeds threshold (50%)
- Check sliding window size (need at least 10 calls)
- Ensure annotations are on public methods
- Verify AOP is enabled

### Fallback Not Triggered

- Confirm `spring.cloud.openfeign.circuitbreaker.enabled: true`
- Check fallback class is a Spring component
- Verify exception types are not excluded

### Metrics Not Available

- Ensure `resilience4j-micrometer` dependency is present
- Check actuator endpoints are exposed
- Verify Prometheus endpoint is enabled

### JWT Not Propagated

- Confirm `FeignClientInterceptor` is registered as a bean
- Check `RequestContextHolder` has request attributes
- Verify Authorization header is present in original request

## Validation Results (Phase 9.5)

### Test Execution Summary

**Date**: January 4, 2026  
**Branch**: `feature/resilience-validation`  
**Status**: ✅ PASSED

### Unit & Integration Tests

All tests successfully updated and passing:
- **Total Tests**: 26/26 passed
- **Unit Tests**: 12/12 passed (OrderServiceTest)
- **Integration Tests**: 14/14 passed (OrderIntegrationTest, OrderControllerTest)

**Key Changes**:
- Updated tests to use `CatalogClient` (Feign) instead of `RestTemplate`
- Fixed Resilience4j version compatibility (1.7.0 for Java 11)
- Verified Spring Context loads correctly with Resilience4j AOP proxies

### Build Verification

```
[INFO] Reactor Summary for Bookstore Microservices 1.0.0-SNAPSHOT:
[INFO] 
[INFO] Bookstore Microservices ............................ SUCCESS
[INFO] Shared Common Module ............................... SUCCESS
[INFO] Eureka Server ...................................... SUCCESS
[INFO] API Gateway ........................................ SUCCESS
[INFO] Catalog Service .................................... SUCCESS
[INFO] Order Service ...................................... SUCCESS
[INFO] User Service ....................................... SUCCESS
[INFO] BUILD SUCCESS
```

### Observability Verification

**Actuator Endpoints Confirmed**:
- ✅ `/actuator/health` - Service health with circuit breaker status
- ✅ `/actuator/prometheus` - Prometheus metrics endpoint
- ✅ `/actuator/circuitbreakers` - Circuit breaker state information
- ✅ `/actuator/metrics` - Detailed metrics

**Resilience4j Metrics Exposed**:
```
resilience4j_circuitbreaker_state{name="catalogService"}
resilience4j_circuitbreaker_slow_call_rate{name="catalogService"}
resilience4j_circuitbreaker_buffered_calls{kind="failed",name="catalogService"}
resilience4j_circuitbreaker_buffered_calls{kind="successful",name="catalogService"}
resilience4j_circuitbreaker_slow_calls{kind="failed",name="catalogService"}
resilience4j_circuitbreaker_failure_rate{name="catalogService"}
resilience4j_circuitbreaker_calls_seconds_count{kind="successful",name="catalogService"}
resilience4j_circuitbreaker_calls_seconds_count{kind="failed",name="catalogService"}
resilience4j_retry_calls_seconds_count{name="catalogService"}
resilience4j_bulkhead_available_concurrent_calls{name="catalogService"}
```

**Circuit Breaker Configuration Verified**:
```json
{
  "circuitBreakers": ["catalogService"]
}
```

### Configuration Validation

**Resilience4j Settings**:
- Circuit Breaker: Sliding window of 10 calls, 50% failure threshold
- Retry: 3 attempts with exponential backoff (2s → 4s → 8s)
- Bulkhead: Maximum 10 concurrent calls
- Health Indicator: Registered and exposed
- Metrics: Exported to Prometheus

**Feign Client**:
- OpenFeign circuit breaker integration: ✅ Enabled
- JWT propagation via `FeignClientInterceptor`: ✅ Configured
- Fallback handler: ✅ Implemented (`CatalogClientFallback`)

### Known Issues & Resolutions

**Issue 1: Resilience4j Version Compatibility**
- **Problem**: Version 1.7.1 had SpelResolver compatibility issues
- **Solution**: Downgraded to 1.7.0 (Java 11 compatible)
- **Status**: ✅ Resolved

**Issue 2: Missing Prometheus Metrics**
- **Problem**: Prometheus endpoint not exposed initially
- **Solution**: Added `micrometer-registry-prometheus` dependency
- **Status**: ✅ Resolved

**Issue 3: Test Failures After Feign Migration**
- **Problem**: Tests used `RestTemplate` instead of `CatalogClient`
- **Solution**: Updated all tests to mock `CatalogClient`
- **Status**: ✅ Resolved

### Recommendations

1. **Production Tuning**: Adjust circuit breaker thresholds based on actual traffic patterns
2. **Monitoring**: Set up Grafana dashboards for real-time circuit breaker monitoring
3. **Alerting**: Configure alerts for circuit breaker OPEN state transitions
4. **Load Testing**: Perform load tests to validate bulkhead configuration
5. **Chaos Engineering**: Regular chaos testing to verify resilience patterns

## References

- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Spring Cloud OpenFeign](https://spring.io/projects/spring-cloud-openfeign)
- [Circuit Breaker Pattern](https://martinfowler.com/bliki/CircuitBreaker.html)
- [Microservices Patterns](https://microservices.io/patterns/reliability/circuit-breaker.html)
