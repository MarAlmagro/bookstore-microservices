# API Gateway & Service Discovery Guide

## Overview

This guide covers the API Gateway and Service Discovery implementation using **Spring Cloud Gateway** and **Netflix Eureka** in the Bookstore Microservices project.

## Architecture

### Components

1. **Eureka Server** (Port 8761) - Service Registry
2. **API Gateway** (Port 8080) - Single Entry Point
3. **Microservices** - Register with Eureka and are discovered by Gateway

### Request Flow

```
Client Request
    ↓
API Gateway (8080)
    ↓
Eureka Server (8761) - Service Discovery
    ↓
Load Balanced Request
    ↓
Microservice Instance (catalog/order/user)
```

---

## Eureka Server

### Configuration

**Location**: `eureka-server/src/main/resources/application.yml`

```yaml
spring:
  application:
    name: eureka-server

server:
  port: 8761

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://localhost:8761/eureka/
  server:
    enable-self-preservation: false
```

### Key Settings

- **register-with-eureka: false** - Server doesn't register with itself
- **fetch-registry: false** - Server doesn't fetch registry from itself
- **enable-self-preservation: false** - Disabled for development (faster deregistration)

### Accessing Eureka Dashboard

Open browser: **http://localhost:8761**

The dashboard shows:
- Registered service instances
- Service health status
- Instance metadata
- Renewal statistics

---

## API Gateway

### Configuration

**Location**: `api-gateway/src/main/resources/application.yml`

```yaml
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        # Catalog Service Routes
        - id: catalog-service
          uri: lb://catalog-service
          predicates:
            - Path=/api/v1/books/**
          filters:
            - RewritePath=/api/v1/books/(?<segment>.*), /api/v1/books/${segment}
            
        # Order Service Routes
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/v1/orders/**, /api/v1/cart/**
          filters:
            - RewritePath=/api/v1/(?<segment>.*), /api/v1/${segment}
            
        # User Service Routes
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/v1/auth/**, /api/v1/users/**
          filters:
            - RewritePath=/api/v1/(?<segment>.*), /api/v1/${segment}
            
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins: "*"
            allowed-methods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowed-headers: "*"
            allow-credentials: false

server:
  port: 8080

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
```

### Route Configuration Explained

#### Route Components

1. **id**: Unique identifier for the route
2. **uri**: Destination URI (lb:// prefix enables load balancing via Eureka)
3. **predicates**: Conditions that must be met for routing
4. **filters**: Transformations applied to requests/responses

#### Load Balancing

The `lb://` prefix in the URI tells Gateway to:
- Look up the service in Eureka
- Load balance across available instances
- Handle failover automatically

Example: `lb://catalog-service` → Gateway queries Eureka for catalog-service instances

---

## Route Patterns

### Current Routes

| Service | Path Pattern | Example |
|---------|-------------|---------|
| Catalog | `/api/v1/books/**` | `GET /api/v1/books` |
| Order | `/api/v1/orders/**` | `POST /api/v1/orders` |
| Order | `/api/v1/cart/**` | `GET /api/v1/cart` |
| User | `/api/v1/auth/**` | `POST /api/v1/auth/login` |
| User | `/api/v1/users/**` | `GET /api/v1/users/profile` |

### Path Matching

- `*` - Matches single path segment
- `**` - Matches multiple path segments
- `?` - Matches single character

---

## Adding New Routes

### Step 1: Register Service with Eureka

Add to service's `application.yml`:

```yaml
spring:
  application:
    name: my-new-service

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${server.port}
```

Add dependency to service's `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

### Step 2: Add Route to Gateway

Update `api-gateway/src/main/resources/application.yml`:

```yaml
spring:
  cloud:
    gateway:
      routes:
        # ... existing routes ...
        
        # New Service Route
        - id: my-new-service
          uri: lb://my-new-service
          predicates:
            - Path=/api/v1/myservice/**
          filters:
            - RewritePath=/api/v1/myservice/(?<segment>.*), /api/v1/myservice/${segment}
```

### Step 3: Restart Gateway

```bash
cd api-gateway
mvn spring-boot:run
```

### Step 4: Verify

```bash
# Check service registered in Eureka
curl http://localhost:8761/eureka/apps/MY-NEW-SERVICE

# Test route through Gateway
curl http://localhost:8080/api/v1/myservice/health
```

---

## CORS Configuration

### Global CORS

Configured in Gateway for all routes:

```yaml
globalcors:
  cors-configurations:
    '[/**]':
      allowed-origins: "*"
      allowed-methods:
        - GET
        - POST
        - PUT
        - DELETE
        - OPTIONS
      allowed-headers: "*"
      allow-credentials: false
```

### Production CORS

For production, restrict origins:

```yaml
globalcors:
  cors-configurations:
    '[/**]':
      allowed-origins: 
        - "https://yourdomain.com"
        - "https://app.yourdomain.com"
      allowed-methods:
        - GET
        - POST
        - PUT
        - DELETE
      allowed-headers: "*"
      allow-credentials: true
```

---

## Custom Filters

### Request Logging Filter

**Location**: `api-gateway/src/main/java/com/bookstore/gateway/config/GatewayConfig.java`

```java
@Configuration
public class GatewayConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(GatewayConfig.class);
    
    @Bean
    @Order(1)
    public GlobalFilter requestLoggingFilter() {
        return (exchange, chain) -> {
            logger.info("Request: {} {}", 
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI());
            return chain.filter(exchange);
        };
    }
}
```

### Adding Custom Headers

```java
@Bean
public GlobalFilter customHeaderFilter() {
    return (exchange, chain) -> {
        exchange.getRequest().mutate()
            .header("X-Gateway-Request", "true")
            .build();
        return chain.filter(exchange);
    };
}
```

### Response Time Filter

```java
@Bean
public GlobalFilter responseTimeFilter() {
    return (exchange, chain) -> {
        long startTime = System.currentTimeMillis();
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Request to {} took {}ms", 
                exchange.getRequest().getURI(), duration);
        }));
    };
}
```

---

## Service-to-Service Communication

### Using RestTemplate with Load Balancing

**Configuration**:

```java
@Configuration
public class RestTemplateConfig {
    
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

**Usage**:

```java
@Service
public class OrderService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public Book getBook(Long bookId) {
        // Use service name instead of localhost:port
        String url = "http://catalog-service/api/v1/books/" + bookId;
        return restTemplate.getForObject(url, Book.class);
    }
}
```

### Benefits

- **Dynamic Discovery**: No hardcoded URLs
- **Load Balancing**: Automatic distribution across instances
- **Failover**: Automatic retry on failure
- **Scalability**: Add instances without code changes

---

## Monitoring & Management

### Gateway Actuator Endpoints

Enable in `application.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,gateway
  endpoint:
    gateway:
      enabled: true
```

### Available Endpoints

| Endpoint | Description |
|----------|-------------|
| `GET /actuator/gateway/routes` | List all configured routes |
| `GET /actuator/gateway/routes/{id}` | Get specific route details |
| `POST /actuator/gateway/refresh` | Refresh route configuration |
| `GET /actuator/health` | Gateway health status |

### Examples

```bash
# View all routes
curl http://localhost:8080/actuator/gateway/routes | jq

# View specific route
curl http://localhost:8080/actuator/gateway/routes/catalog-service | jq

# Check gateway health
curl http://localhost:8080/actuator/health
```

---

## Troubleshooting

### Issue: Service Not Found (503)

**Symptoms**: Gateway returns 503 Service Unavailable

**Causes**:
1. Service not registered with Eureka
2. Service name mismatch in route configuration
3. Service is down or unhealthy

**Solutions**:

```bash
# Check Eureka dashboard
open http://localhost:8761

# Verify service registration
curl http://localhost:8761/eureka/apps

# Check service health
curl http://localhost:8081/actuator/health  # Direct service access

# Check Gateway logs
docker logs api-gateway  # If using Docker
```

### Issue: Route Not Working

**Symptoms**: 404 Not Found or wrong service called

**Causes**:
1. Path predicate doesn't match request
2. Route order issues
3. Filter rewrite path incorrect

**Solutions**:

```bash
# View configured routes
curl http://localhost:8080/actuator/gateway/routes | jq

# Enable debug logging
# Add to application.yml:
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    reactor.netty.http.client: DEBUG
```

### Issue: CORS Errors

**Symptoms**: Browser console shows CORS errors

**Solutions**:

1. Check Gateway CORS configuration
2. Ensure OPTIONS requests are allowed
3. Verify allowed-origins includes your frontend URL

```yaml
globalcors:
  cors-configurations:
    '[/**]':
      allowed-origins: "http://localhost:4200"  # Your frontend URL
      allowed-methods:
        - GET
        - POST
        - PUT
        - DELETE
        - OPTIONS
      allowed-headers: "*"
```

### Issue: Slow Response Times

**Causes**:
1. Service discovery latency
2. Too many instances causing overhead
3. Network issues

**Solutions**:

```yaml
# Tune Eureka client settings
eureka:
  client:
    registry-fetch-interval-seconds: 5  # Default: 30
    instance-info-replication-interval-seconds: 10  # Default: 30
  instance:
    lease-renewal-interval-in-seconds: 10  # Default: 30
```

### Issue: Gateway Not Registering with Eureka

**Symptoms**: Gateway not visible in Eureka dashboard

**Solutions**:

```bash
# Check Eureka URL is correct
# In api-gateway/application.yml:
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/  # Verify URL

# Check Gateway logs for errors
docker logs api-gateway

# Verify Eureka server is running
curl http://localhost:8761/actuator/health
```

---

## Best Practices

### 1. Route Organization

- Group routes by service
- Use meaningful route IDs
- Document route purposes

### 2. Security

- Implement authentication at Gateway level
- Use JWT validation filters
- Restrict CORS in production
- Enable HTTPS in production

### 3. Performance

- Enable response caching where appropriate
- Use circuit breakers for resilience
- Monitor response times
- Set appropriate timeouts

### 4. Monitoring

- Enable actuator endpoints
- Log all requests
- Track response times
- Monitor Eureka health

### 5. Testing

- Test routes individually
- Verify load balancing
- Test failover scenarios
- Validate CORS configuration

---

## Docker Configuration

### Eureka Server Dockerfile

```dockerfile
FROM openjdk:11-jre-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### API Gateway Dockerfile

```dockerfile
FROM openjdk:11-jre-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Docker Compose

```yaml
eureka-server:
  build: ./eureka-server
  container_name: eureka-server
  ports:
    - "8761:8761"
  networks:
    - bookstore-network
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
    interval: 10s
    timeout: 5s
    retries: 5

api-gateway:
  build: ./api-gateway
  container_name: api-gateway
  ports:
    - "8080:8080"
  environment:
    - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
  depends_on:
    eureka-server:
      condition: service_healthy
  networks:
    - bookstore-network
```

---

## Testing Gateway

### Manual Testing

```bash
# Test catalog route
curl http://localhost:8080/api/v1/books

# Test user authentication
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@bookstore.com","password":"admin123"}'

# Test order creation
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"items":[{"bookId":1,"quantity":2}]}'
```

### Automated Testing

Use the provided test scripts:

```bash
# Full integration test via Gateway
./scripts/test-integration.sh

# Individual service tests via Gateway
./scripts/test-catalog.sh
./scripts/test-user.sh
./scripts/test-order.sh
```

---

## Production Considerations

### 1. Multiple Gateway Instances

Deploy multiple Gateway instances behind a load balancer:

```yaml
# application-prod.yml
eureka:
  instance:
    instance-id: ${spring.application.name}:${random.value}
```

### 2. Security

- Enable HTTPS
- Implement rate limiting
- Add authentication filters
- Restrict CORS origins

### 3. Resilience

- Configure circuit breakers
- Set appropriate timeouts
- Implement retry logic
- Add fallback responses

### 4. Monitoring

- Integrate with monitoring tools (Prometheus, Grafana)
- Enable distributed tracing (Zipkin, Jaeger)
- Set up alerts for failures
- Monitor Eureka health

---

## Additional Resources

- [Spring Cloud Gateway Documentation](https://spring.io/projects/spring-cloud-gateway)
- [Netflix Eureka Documentation](https://github.com/Netflix/eureka/wiki)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Project Architecture Guide](ARCHITECTURE.md)
- [Testing Guide](TESTING.md)

---

**Last Updated**: Phase 7 - January 2026
