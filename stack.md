# Bookstore Microservices - Technology Stack

## Core Versions
| Technology | Version |
|------------|---------|
| Java | 11 |
| Spring Boot | 2.7.18 |
| Spring Cloud | 2021.0.8 |

## Modules
| Module | Description | Port |
|--------|-------------|------|
| eureka-server | Service Discovery | 8761 |
| api-gateway | API Gateway | 8080 |
| catalog-service | Book Catalog (MySQL) | 8081 |
| order-service | Order Management (MongoDB) | 8082 |
| user-service | User/Auth (PostgreSQL) | 8083 |
| admin-ui | Admin Dashboard | - |
| shared-common | Shared Library | - |

## Core Dependencies
- **spring-boot-starter-web** - REST API
- **spring-boot-starter-data-jpa** - JPA/Hibernate
- **spring-boot-starter-data-mongodb** - MongoDB support
- **spring-boot-starter-security** - Security framework
- **spring-boot-starter-validation** - Bean Validation
- **spring-cloud-starter-netflix-eureka-client/server** - Service Discovery
- **springdoc-openapi-ui** (1.8.0) - API Documentation
- **jjwt** (0.11.5) - JWT Authentication
- **lombok** (1.18.30) - Boilerplate reduction
- **modelmapper** (3.1.1) - DTO mapping

## Databases
| Service | Database | Version | Port |
|---------|----------|---------|------|
| catalog-service | MySQL | 8.0 | 3306 |
| order-service | MongoDB | 6.0 | 27017 |
| user-service | PostgreSQL | 15 | 5432 |

## Observability Stack
| Component | Port |
|-----------|------|
| Zipkin (Tracing) | 9411 |
| Prometheus (Metrics) | 9090 |
| Grafana (Dashboards) | 3000 |
| Loki (Logs) | 3100 |

## Maven Profiles
| Profile | Purpose |
|---------|---------|
| jacoco | Code coverage reporting |
| checkstyle | Code style validation |
| spotbugs | Static analysis |
| security | OWASP dependency check |
| sonar | SonarQube integration |
| quality | Combined quality checks |
