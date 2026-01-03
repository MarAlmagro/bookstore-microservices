# Bookstore Microservices Build Plan

**Status**: In Progress
**Current Phase**: Phase 4 - Order Service
**Last Updated**: 2026-01-02

---

## Phase 1: Project Foundation

### 1.1 Project Setup
- [x] Parent POM created
- [x] Root .gitignore configured (Maven, IDE, Docker artifacts)
- [x] Root README.md with setup instructions
- [x] Docker compose skeleton (databases only, no services yet)
- [x] .env.example file for environment variables

**Prompt**: 
```
Create .gitignore, README.md, docker-compose.yml (databases only), 
and .env.example following @PROJECT_SPECIFICATION.md
```

**Validation**: 
- Check .gitignore excludes target/, .idea/, .vscode/
- Verify docker-compose has mysql, mongodb, postgres services
- Confirm .env.example has all required variables

---

## Phase 2: Shared Common Module

### 2.1 Module Structure
- [x] Create shared-common/ with pom.xml
- [x] Create package structure: dto, exception, constants, validation
- [x] Add dependencies: validation, lombok (optional)

**Prompt**:
```
Create the shared-common module with complete Maven structure and pom.xml. 
Include package folders: dto, exception, constants, validation
```

### 2.2 DTOs
- [x] BookDTO.java (id, isbn, title, author, price, stock, category)
- [x] OrderDTO.java (id, userId, items, totalAmount, status, createdAt)
- [x] OrderItemDTO.java (bookId, quantity, price)
- [x] UserDTO.java (id, email, firstName, lastName, role)
- [x] AuthRequestDTO.java (email, password)
- [x] AuthResponseDTO.java (token, refreshToken, user)

**Prompt**:
```
Create all DTO classes in shared-common/dto/ with:
- Proper validation annotations (@NotNull, @Email, @Min, etc.)
- Lombok @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor
- Javadoc comments
Follow the field specifications in @PROJECT_SPECIFICATION.md
```

### 2.3 Exceptions
- [x] ResourceNotFoundException.java
- [x] InvalidRequestException.java  
- [x] UnauthorizedException.java
- [x] GlobalExceptionHandler.java (with @ControllerAdvice)
- [x] ErrorResponse.java (timestamp, status, error, message, path)

**Prompt**:
```
Create custom exception classes and GlobalExceptionHandler in shared-common/exception/.
Include ErrorResponse DTO for consistent error formatting.
```

### 2.4 Constants & Enums
- [x] OrderStatus enum (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
- [x] UserRole enum (CUSTOMER, ADMIN)
- [x] ApiConstants.java (API_V1_PREFIX = "/api/v1")

**Prompt**:
```
Create OrderStatus and UserRole enums, plus ApiConstants class 
in shared-common/constants/
```

### 2.5 Build & Verify
- [x] Build shared-common: `mvn clean install`
- [x] Verify JAR in local Maven repo (~/.m2/repository/)

**Prompt**:
```
Build the shared-common module and verify it compiles successfully
```

---

## Phase 3: Catalog Service

### 3.1 Service Skeleton
- [x] Create catalog-service/ with pom.xml
- [x] Add dependencies: web, data-jpa, mysql, validation, shared-common
- [x] Create package structure (controller, service, repository, entity, mapper, config)
- [x] Create CatalogServiceApplication.java with @SpringBootApplication
- [x] Create application.yml with MySQL config (port 8081)

**Prompt**:
```
Create catalog-service module skeleton with Maven structure, main application class,
and application.yml configured for MySQL on port 8081.
Reference @PROJECT_SPECIFICATION.md for configuration details.
```

### 3.2 Database Layer
- [x] Book entity (id, isbn, title, author, description, price, stock, category, createdAt, updatedAt)
- [x] Add JPA annotations (@Entity, @Table, @Id, @GeneratedValue, etc.)
- [x] BookRepository interface extending JpaRepository
- [x] Add custom query methods (findByCategory, findByAuthorContaining, etc.)

**Prompt**:
```
Create Book entity with JPA annotations and BookRepository with custom query methods
in catalog-service. Include audit fields (createdAt, updatedAt) with @PrePersist/@PreUpdate.
```

### 3.3 Service Layer
- [x] BookService interface
- [x] BookServiceImpl with business logic
- [x] BookMapper (entity ↔ DTO conversion) using MapStruct or manual
- [x] Implement: findAll, findById, create, update, delete, search methods
- [x] Add proper exception handling (throw ResourceNotFoundException)

**Prompt**:
```
Create BookService interface and implementation in catalog-service with full CRUD
and search logic. Include BookMapper for entity-DTO conversion.
```

### 3.4 Controller Layer
- [x] BookController with @RestController, @RequestMapping("/api/v1/books")
- [x] Implement endpoints: GET /, GET /{id}, POST /, PUT /{id}, DELETE /{id}, GET /search
- [x] Add OpenAPI annotations (@Operation, @ApiResponse)
- [x] Add validation (@Valid, @PathVariable, @RequestParam)
- [x] Return ResponseEntity with proper HTTP status codes

**Prompt**:
```
Create BookController with all REST endpoints and OpenAPI documentation.
Use ResponseEntity and proper status codes (200, 201, 204, 404).
```

### 3.5 Configuration
- [x] Add Swagger/OpenAPI dependency (springdoc-openapi-ui)
- [x] Create OpenApiConfig.java with API info
- [x] Add CORS configuration (if needed)

**Prompt**:
```
Add Swagger configuration to catalog-service and create OpenApiConfig class
```

### 3.6 Testing
- [x] BookServiceTest (unit tests with Mockito)
- [x] BookControllerTest (MockMvc tests)
- [x] BookIntegrationTest (with H2 embedded database)
- [x] Test data builders for Book entities

**Prompt**:
```
Create comprehensive tests for catalog-service:
- Unit tests for BookService (Mockito)
- Controller tests with MockMvc
- Integration tests with H2 database
Include test fixtures and data builders.
```

### 3.7 Docker
- [x] Create Dockerfile for catalog-service
- [x] Update docker-compose.yml to include catalog-service

**Prompt**:
```
Create Dockerfile for catalog-service and add service definition 
to docker-compose.yml with dependency on mysql-catalog
```

### 3.8 Build & Verify
- [x] Build: `mvn clean install`
- [x] Run tests: `mvn test`
- [x] Run locally: `mvn spring-boot:run`
- [x] Test endpoints via Swagger: http://localhost:8081/swagger-ui.html
- [x] Test with curl/Postman: Create, read, update, delete a book

**Validation Commands**:
```bash
cd catalog-service
mvn clean test
mvn spring-boot:run
# In another terminal:
curl http://localhost:8081/api/v1/books
```

---

## Phase 4: Order Service

### 4.1 Service Skeleton
- [x] Create order-service/ with pom.xml
- [x] Add dependencies: web, data-mongodb, validation, shared-common
- [x] Create package structure (controller, service, repository, document, mapper, config)
- [x] Create OrderServiceApplication.java
- [x] Create application.yml with MongoDB config (port 8082)

**Prompt**:
```
Create order-service module skeleton with Maven structure, main application class,
and application.yml configured for MongoDB on port 8082
```

### 4.2 Database Layer
- [x] Order document (@Document, @Id)
- [x] Fields: id, userId, items (List<OrderItem>), totalAmount, status, createdAt
- [x] OrderItem embedded class (bookId, quantity, price)
- [x] OrderRepository extending MongoRepository
- [x] Custom queries: findByUserId, findByStatus

**Prompt**:
```
Create Order document with MongoDB annotations and OrderRepository.
Include embedded OrderItem class. Add custom query methods.
```

### 4.3 Service Layer
- [x] OrderService interface
- [x] OrderServiceImpl with business logic
- [x] Implement: createOrder, getOrderById, getUserOrders, updateOrderStatus
- [x] Add RestTemplate bean configuration for calling Catalog Service
- [x] Implement validation: check book availability via Catalog Service API
- [x] OrderMapper for document-DTO conversion

**Prompt**:
```
Create OrderService with business logic including:
- RestTemplate configuration for service-to-service calls
- Validation logic (call catalog-service to check book availability)
- Order creation workflow
- OrderMapper for document-DTO conversion
```

### 4.4 Controller Layer
- [x] OrderController with REST endpoints
- [x] POST /api/v1/orders (create order)
- [x] GET /api/v1/orders (user's orders)
- [x] GET /api/v1/orders/{id} (order details)
- [x] PUT /api/v1/orders/{id}/status (update status)
- [x] Add OpenAPI documentation

**Prompt**:
```
Create OrderController with all REST endpoints and OpenAPI documentation.
Include proper validation and error handling.
```

### 4.5 Testing
- [x] OrderServiceTest with Mockito (mock RestTemplate)
- [x] OrderControllerTest with MockMvc
- [x] OrderIntegrationTest with embedded MongoDB (de.flapdoodle.embed.mongo)

**Prompt**:
```
Create comprehensive tests for order-service with:
- Mocked RestTemplate for catalog service calls
- Embedded MongoDB for integration tests
- Test fixtures
```

### 4.6 Docker
- [x] Create Dockerfile for order-service
- [x] Update docker-compose.yml

**Prompt**:
```
Create Dockerfile for order-service and update docker-compose.yml
```

### 4.7 Build & Verify
- [x] Build and test
- [x] Run both catalog-service and order-service
- [x] Test order creation via Swagger
- [x] Verify service-to-service communication works

**Validation**:
```bash
# Terminal 1: Catalog Service
cd catalog-service && mvn spring-boot:run

# Terminal 2: Order Service  
cd order-service && mvn spring-boot:run

# Test: Create a book in catalog, then create an order
```

---

## Phase 5: User Service

### 5.1 Service Skeleton
- [ ] Create user-service/ with pom.xml
- [ ] Add dependencies: web, data-jpa, postgresql, security, jwt, validation, shared-common
- [ ] Create package structure (controller, service, repository, entity, security, config)
- [ ] Create UserServiceApplication.java
- [ ] Create application.yml with PostgreSQL config (port 8083)

**Prompt**:
```
Create user-service module skeleton with Maven structure, main application class,
and application.yml configured for PostgreSQL on port 8083.
Add JWT dependencies (io.jsonwebtoken:jjwt-api, jjwt-impl, jjwt-jackson).
```

### 5.2 Database Layer
- [ ] User entity (id, email, password, firstName, lastName, role, enabled, createdAt)
- [ ] Add JPA annotations
- [ ] UserRepository extending JpaRepository
- [ ] Custom queries: findByEmail, existsByEmail

**Prompt**:
```
Create User entity with JPA annotations and UserRepository.
Include role field (UserRole enum) and password field for BCrypt hash.
```

### 5.3 Security Configuration
- [ ] JwtTokenProvider class (generate, validate, extract claims)
- [ ] JwtAuthenticationFilter (intercept requests, validate token)
- [ ] SecurityConfig class (@Configuration, @EnableWebSecurity)
- [ ] Configure: BCryptPasswordEncoder, authentication manager, HTTP security
- [ ] Permit: /api/v1/auth/**, /swagger-ui/**, /api-docs/**
- [ ] Secure: all other endpoints

**Prompt**:
```
Create complete Spring Security configuration for user-service:
- JwtTokenProvider for token operations
- JwtAuthenticationFilter for request interception
- SecurityConfig with BCrypt and endpoint security rules
- JWT secret from environment variable
```

### 5.4 Service Layer
- [ ] UserService interface
- [ ] UserServiceImpl with business logic
- [ ] AuthService interface  
- [ ] AuthServiceImpl (register, login, refresh token)
- [ ] Implement: password hashing, token generation, user validation
- [ ] UserMapper for entity-DTO conversion

**Prompt**:
```
Create UserService and AuthService with:
- User registration (email uniqueness check, password hashing)
- Login (authentication, JWT generation)
- User profile operations
- UserMapper for entity-DTO conversion
```

### 5.5 Controller Layer
- [ ] AuthController (/api/v1/auth/register, /login, /refresh)
- [ ] UserController (/api/v1/users/profile - GET/PUT)
- [ ] Add @PreAuthorize for role-based access
- [ ] OpenAPI documentation

**Prompt**:
```
Create AuthController and UserController with:
- Registration endpoint (public)
- Login endpoint (public, returns JWT)
- Profile endpoints (secured, user can access own profile, admin can access all)
- OpenAPI documentation
```

### 5.6 Testing
- [ ] UserServiceTest and AuthServiceTest with Mockito
- [ ] AuthControllerTest and UserControllerTest with MockMvc
- [ ] Integration tests with H2 database
- [ ] Test JWT generation/validation

**Prompt**:
```
Create comprehensive tests for user-service including:
- Service layer tests with mocked repository
- Controller tests with MockMvc and security context
- Integration tests with H2 and actual JWT validation
```

### 5.7 Docker
- [ ] Create Dockerfile for user-service
- [ ] Update docker-compose.yml

**Prompt**:
```
Create Dockerfile for user-service and update docker-compose.yml
```

### 5.8 Build & Verify
- [ ] Build and test
- [ ] Run all three services
- [ ] Test registration and login via Swagger
- [ ] Verify JWT token works for protected endpoints

**Validation**:
```bash
# Register user
curl -X POST http://localhost:8083/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"test123","firstName":"Test","lastName":"User"}'

# Login (get JWT)
curl -X POST http://localhost:8083/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"test123"}'

# Use token for protected endpoint
curl http://localhost:8083/api/v1/users/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Phase 6: Integration & Polish

### 6.1 Inter-Service Integration
- [ ] Test full flow: Register → Login → Browse books → Create order
- [ ] Verify order service validates with catalog service
- [ ] Test error scenarios (invalid book ID, insufficient stock)

**Prompt**:
```
Help me test the complete workflow across all services and identify
any integration issues
```

### 6.2 Docker Compose Full Stack
- [ ] Update docker-compose.yml with all services
- [ ] Add health checks
- [ ] Configure service dependencies
- [ ] Test: `docker-compose up --build`

**Prompt**:
```
Update docker-compose.yml to orchestrate all services with proper
dependencies and health checks. Test full stack deployment.
```

### 6.3 Documentation
- [ ] Update root README.md with:
  - Architecture diagram (ASCII or link to image)
  - Setup instructions
  - API endpoint summary
  - Testing instructions
  - Troubleshooting guide
- [ ] Add API usage examples
- [ ] Document environment variables

**Prompt**:
```
Update root README.md with comprehensive documentation including:
- Setup instructions, API endpoints, testing guide, troubleshooting
```

### 6.4 Sample Data Scripts (Optional)
- [ ] Create data initialization scripts
- [ ] Add sample books to catalog
- [ ] Create test users with different roles

**Prompt**:
```
Create SQL script for sample book data and script to seed test users
```

### 6.5 Final Validation
- [ ] All services build successfully
- [ ] All tests pass (`mvn test` in root)
- [ ] Docker compose starts all services
- [ ] Swagger UI accessible for all services
- [ ] Full user journey works end-to-end
- [ ] README instructions work for fresh setup

---

## Usage Instructions

### For Claude Code:
1. Reference this file in prompts: `@BUILD_PLAN.md`
2. Work on one checklist item at a time
3. After completing an item, ask Claude: "Mark item X.X as complete in @BUILD_PLAN.md"
4. Use `/clear` between phases to reset context

### Recommended Workflow:
```
# Start a phase
"Let's work on Phase 2: Shared Common Module. 
Start with item 2.1 following @BUILD_PLAN.md"

# After completion
"Mark item 2.1 as complete in @BUILD_PLAN.md and show me what's next"

# Between major phases
/clear
"Continue with Phase 3 following @BUILD_PLAN.md"
```

### Rollback Strategy:
- Each phase is independently testable
- Git commit after each completed phase
- Can rebuild individual modules without affecting others

---

## Notes
- Estimated time: 8-12 hours with Claude Code (depending on debugging)
- Each service takes ~2-3 hours including tests
- Use `/clear` liberally to manage context
- Commit frequently: after each phase or sub-phase