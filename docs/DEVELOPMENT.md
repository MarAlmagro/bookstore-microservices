# Development Guide

Instructions for building, developing, and contributing to the Bookstore Microservices project.

---

## Build System

The project uses **Maven** as a multi-module build tool. The parent POM (`pom.xml`) defines shared dependencies, plugin versions, and build profiles.

### Modules

| Module | Type | Description |
|--------|------|-------------|
| `shared-common` | Library | Shared DTOs, exceptions, JWT utilities |
| `eureka-server` | Application | Netflix Eureka service registry |
| `api-gateway` | Application | Spring Cloud Gateway (reactive) |
| `catalog-service` | Application | Book catalog REST API |
| `order-service` | Application | Order management REST API |
| `user-service` | Application | Authentication & user REST API |
| `admin-ui` | Application | Thymeleaf admin dashboard |

### Build Commands

```bash
# Full build (skip tests for speed)
mvn clean install -DskipTests

# Build a single module
mvn clean install -pl catalog-service -am -DskipTests

# Build with all quality checks
mvn clean install -P quality
```

The `-am` flag (also-make) ensures that dependencies like `shared-common` are built first.

---

## Maven Profiles

| Profile | Command | Purpose |
|---------|---------|---------|
| `jacoco` | `mvn test -P jacoco` | Generate code coverage reports (HTML in `target/site/jacoco/`) |
| `checkstyle` | `mvn checkstyle:check -P checkstyle` | Enforce code style rules |
| `spotbugs` | `mvn spotbugs:check -P spotbugs` | Static bug analysis |
| `security` | `mvn dependency-check:check -P security` | OWASP dependency vulnerability scan |
| `sonar` | `mvn sonar:sonar -P sonar` | SonarQube integration |
| `quality` | `mvn verify -P quality` | Run all quality checks combined |

### Quality Gate with Docker Compose

A separate Compose file runs quality tools:

```bash
docker-compose -f docker-compose.quality.yml up
```

---

## Key Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| Spring Boot | 2.7.18 | Application framework |
| Spring Cloud | 2021.0.8 | Eureka, Gateway, Sleuth |
| SpringDoc OpenAPI | 1.8.0 | Swagger UI and API docs |
| jjwt | 0.11.5 | JWT token creation and validation |
| Lombok | 1.18.30 | Boilerplate reduction |
| ModelMapper | 3.1.1 | Entity ↔ DTO mapping |
| Testcontainers | 1.19.3 | Integration tests with real databases |
| MySQL Connector | 8.4.0 | MySQL JDBC driver |
| PostgreSQL Driver | 42.7.4 | PostgreSQL JDBC driver |

---

## Coding Conventions

### General

- **Java 11** language level — no `var` keyword in lambdas, no records.
- **Lombok** is used throughout; ensure your IDE has the Lombok plugin installed.
- **DTOs** live in `shared-common`. Do not duplicate them in individual services.
- Each REST controller delegates to a service interface for testability.

### Naming

| Element | Convention | Example |
|---------|-----------|---------|
| Packages | `com.bookstore.<module>.<layer>` | `com.bookstore.catalog.service` |
| REST endpoints | `/api/v1/<resource>` | `/api/v1/books` |
| DTOs | `<Entity>Dto` | `BookDto` |
| Mappers | `<Entity>Mapper` | `BookMapper` |
| Exceptions | `<Domain>Exception` | `BookNotFoundException` |
| Test classes | `<Class>Test` | `BookServiceTest` |
| Integration tests | `<Feature>IntegrationTest` | `BookIntegrationTest` |

### Testing

- Every interactive UI element must include a `data-testid` attribute (admin-ui).
- Tests follow the `given_when_then` pattern.
- See [Testing Guide](TESTING.md) for full details.

---

## Running Individual Services Locally

For faster iteration, you can run a single service outside Docker while keeping its database in Docker:

```bash
# Start only databases and Eureka
docker-compose up -d eureka-server mysql-catalog mongodb-order postgres-user redis

# Run one service locally
cd catalog-service
mvn spring-boot:run -Dspring-boot.run.profiles=default
```

Ensure the local service can reach `localhost` ports for its database and Eureka.

---

## Adding a New Service

1. Create a new Maven module directory (e.g., `notification-service/`).
2. Add it to the parent `pom.xml` `<modules>` section.
3. Include `shared-common` as a dependency.
4. Register with Eureka by adding `spring-cloud-starter-netflix-eureka-client`.
5. Add a `Dockerfile` following the pattern from existing services.
6. Add the service to `docker-compose.yml`.
7. Add routes in the API Gateway configuration.
8. Update this documentation.

---

## IDE Setup

### IntelliJ IDEA

1. Import the project as a Maven project.
2. Install the **Lombok** plugin.
3. Enable annotation processing: *Settings → Build → Compiler → Annotation Processors → Enable*.
4. Set SDK to Java 11.

### VS Code

1. Install the **Extension Pack for Java**.
2. Install the **Lombok Annotations Support** extension.
3. The `.vscode/` directory is git-ignored; configure as needed.

---

## Contributing

1. Create a feature branch from `main`.
2. Follow the coding conventions above.
3. Write or update tests before implementation (TDD preferred).
4. Ensure `mvn clean install` passes (including tests).
5. Run `mvn verify -P quality` and resolve any findings.
6. Commit using conventional commit messages: `type(scope): description`.
   - Types: `feat`, `fix`, `docs`, `test`, `refactor`, `chore`, `ci`.
   - Example: `feat(catalog): add batch import endpoint`.

---

[← Back to Documentation Hub](README.md) · [Testing Guide →](TESTING.md)
