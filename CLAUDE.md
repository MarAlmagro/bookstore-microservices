# Bookstore Microservices Project

## Project Overview
Spring Boot microservices bookstore with polyglot persistence. Portfolio project demonstrating enterprise architecture patterns.

## Git Commit Strategy

### IMPORTANT: Create Commit Message After Each Build Plan Step

After completing each step in @BUILD_PLAN.md, you MUST:
1. Stage all changes
2. Create the message for a commit with the format below
3. detail key features for major phase steps

### Commit Message Format
```
<type>(<scope>): <description>

[optional body for complex changes]
```

**Types:**
- `feat` - New feature or major functionality
- `chore` - Project setup, configuration, dependencies
- `test` - Adding or modifying tests
- `docs` - Documentation changes
- `fix` - Bug fixes
- `refactor` - Code restructuring without behavior change

**Scope:** The module/service being modified
- `root` - Root project files
- `shared-common` - Shared common module
- `catalog` - Catalog service
- `order` - Order service
- `user` - User service
- `docker` - Docker configuration

### Examples by Build Plan Phase

**Phase 1: Foundation**
```bash
# Step 1.1
git add .
git commit -m "chore(root): add .gitignore, README, and docker-compose skeleton"

# If you created multiple files, you can be more specific:
git add .gitignore README.md
git commit -m "docs(root): add project README and .gitignore"

git add docker-compose.yml .env.example
git commit -m "chore(docker): add database services configuration"
```

**Phase 2: Shared Common**
```bash
# Step 2.1
git add shared-common/
git commit -m "chore(shared-common): create module structure and pom.xml"

# Step 2.2
git add shared-common/src/main/java/com/bookstore/common/dto/
git commit -m "feat(shared-common): add all Dto classes with validation"

# Step 2.3
git add shared-common/src/main/java/com/bookstore/common/exception/
git commit -m "feat(shared-common): add custom exceptions and global handler"

# Step 2.4
git add shared-common/src/main/java/com/bookstore/common/constants/
git commit -m "feat(shared-common): add enums and constants"

# Step 2.5 - after build succeeds
git commit -m "chore(shared-common): verify module builds successfully" --allow-empty
```

**Phase 3: Catalog Service**
```bash
# Step 3.1
git add catalog-service/
git commit -m "chore(catalog): create service skeleton with application config"

# Step 3.2
git add catalog-service/src/main/java/com/bookstore/catalog/entity/
git add catalog-service/src/main/java/com/bookstore/catalog/repository/
git commit -m "feat(catalog): add Book entity and repository with custom queries"

# Step 3.3
git add catalog-service/src/main/java/com/bookstore/catalog/service/
git add catalog-service/src/main/java/com/bookstore/catalog/mapper/
git commit -m "feat(catalog): implement book service layer with CRUD operations"

# Step 3.4
git add catalog-service/src/main/java/com/bookstore/catalog/controller/
git commit -m "feat(catalog): add REST controller with all book endpoints"

# Step 3.5
git add catalog-service/pom.xml catalog-service/src/main/java/com/bookstore/catalog/config/
git commit -m "chore(catalog): add Swagger/OpenAPI configuration"

# Step 3.6
git add catalog-service/src/test/
git commit -m "test(catalog): add unit, controller, and integration tests"

# Step 3.7
git add catalog-service/Dockerfile docker-compose.yml
git commit -m "chore(docker): add catalog-service to Docker setup"
```

**Phase 4: Order Service**
```bash
# Step 4.1
git add order-service/
git commit -m "chore(order): create service skeleton with MongoDB config"

# Step 4.2
git add order-service/src/main/java/com/bookstore/order/document/
git add order-service/src/main/java/com/bookstore/order/repository/
git commit -m "feat(order): add Order document and repository"

# Step 4.3
git add order-service/src/main/java/com/bookstore/order/service/
git add order-service/src/main/java/com/bookstore/order/config/
git commit -m "feat(order): implement order service with catalog integration"

# Step 4.4
git add order-service/src/main/java/com/bookstore/order/controller/
git commit -m "feat(order): add REST controller for order management"

# Step 4.6
git add order-service/src/test/
git commit -m "test(order): add comprehensive test suite"

# Step 4.7
git add order-service/Dockerfile docker-compose.yml
git commit -m "chore(docker): add order-service to Docker setup"
```

**Phase 5: User Service**
```bash
# Step 5.1
git add user-service/
git commit -m "chore(user): create service skeleton with PostgreSQL config"

# Step 5.2
git add user-service/src/main/java/com/bookstore/user/entity/
git add user-service/src/main/java/com/bookstore/user/repository/
git commit -m "feat(user): add User entity and repository"

# Step 5.3
git add user-service/src/main/java/com/bookstore/user/security/
git add user-service/src/main/java/com/bookstore/user/config/
git commit -m "feat(user): implement JWT authentication and Spring Security"

# Step 5.4
git add user-service/src/main/java/com/bookstore/user/service/
git commit -m "feat(user): implement user and auth service layers"

# Step 5.5
git add user-service/src/main/java/com/bookstore/user/controller/
git commit -m "feat(user): add authentication and user profile endpoints"

# Step 5.6
git add user-service/src/test/
git commit -m "test(user): add security and service tests"

# Step 5.7
git add user-service/Dockerfile docker-compose.yml
git commit -m "chore(docker): add user-service to Docker setup"
```

**Phase 6: Integration & Polish**
```bash
# Step 6.2
git add docker-compose.yml
git commit -m "chore(docker): add health checks and service orchestration"

# Step 6.3
git add README.md
git commit -m "docs(root): update README with comprehensive documentation"

# Step 6.4 (if applicable)
git add scripts/
git commit -m "chore(root): add sample data initialization scripts"
```

### After Each Commit

Always verify the commit:
```bash
git log -1  # Show the last commit
git status  # Verify working directory is clean
```

### Multi-File Changes

If a step modifies files across different concerns, split into multiple commits:
```bash
# Bad: mixing concerns
git add catalog-service/
git commit -m "feat(catalog): add everything"

# Good: separate commits
git add catalog-service/src/main/java/com/bookstore/catalog/entity/
git commit -m "feat(catalog): add Book entity"

git add catalog-service/src/main/java/com/bookstore/catalog/repository/
git commit -m "feat(catalog): add BookRepository with queries"
```

### Important Notes

- Commit after EACH BUILD_PLAN.md step, even small ones
- If a step produces no file changes (e.g., verification steps), use `--allow-empty`
- Keep commit messages concise but descriptive (50-72 characters)
- Always run `git status` before committing to review changes
- If tests fail, do NOT commit - fix first, then commit



## Tech Stack
- Java 11
- Spring Boot 2.7.18
- Maven 3.8+
- MySQL 8.0 (Catalog), MongoDB 6.0 (Order), PostgreSQL 15 (User)

## Project Structure
Maven multi-module: shared-common, catalog-service, order-service, user-service

## Key Commands
```bash
# Build all modules
mvn clean install

# Build specific service
cd <service-name> && mvn clean install

# Run service locally
mvn spring-boot:run

# Run tests
mvn test

# Docker compose
docker-compose up --build
```

## Development Standards
- Use Java 11 features only (no newer syntax)
- Follow Spring Boot 2.7.x conventions
- Package structure: com.bookstore.<service>.<layer>
- Layers: controller, service, repository, entity/document, mapper, config
- REST endpoints: /api/v1/<resource>
- Use Dtos for API responses (no entity exposure)

## File Boundaries
- DO modify: src/main/java/, src/test/java/, pom.xml
- DO NOT modify: .git/, target/, node_modules/

## Testing Requirements
- Unit tests for all service methods
- Controller tests with MockMvc
- Integration tests with embedded databases (H2/embedded MongoDB)
- Minimum 80% coverage

## Git Workflow
- Feature branches: feature/<service>-<description>
- Commit format: feat(<service>): <description>
- Merge to main only after tests pass

## Port Assignments
- Catalog Service: 8081
- Order Service: 8082
- User Service: 8083
- API Gateway (future): 8080

## Important Notes
- ALWAYS use Spring Boot 2.7.18 dependencies (Java 11 compatible)
- NEVER expose entity classes directly in REST responses
- Use RestTemplate for service-to-service communication (not WebClient in Boot 2.7)
- Implement proper exception handling with GlobalExceptionHandler
```
