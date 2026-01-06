# Quality and Security Guide

This document outlines the tools and processes used to ensure high code quality and security standards in the Bookstore Microservices project.

## Quality Tools

We use a set of Maven-based tools to analyze our code. These are grouped into atomized Maven profiles for flexibility.

| Tool | Profile | Purpose | Output |
| :--- | :--- | :--- | :--- |
| **Checkstyle** | `checkstyle` | Enforces Coding Standards (Google Style) | `target/checkstyle-result.xml` |
| **SpotBugs** | `spotbugs` | Static analysis for common bugs | `target/spotbugsXml.xml` |
| **JaCoCo** | `jacoco` | Code Coverage analysis | `target/site/jacoco/index.html` |
| **OWASP Check** | `security` | Scans for vulnerable dependencies | `target/dependency-check-report.html` |
| **SonarQube** | `sonar` | Deep code quality and security analysis | SonarQube Dashboard |

## How to Run

### Run All Quality Checks
To run all tests and quality checks at once:
```bash
mvn clean verify -Pquality
```

### Run Specific Checks
You can activate specific profiles individually:
```bash
mvn checkstyle:check -Pcheckstyle
mvn spotbugs:check -Pspotbugs
mvn dependency-check:check -Psecurity
```

### Note on Security Scans (NVD API)
The `dependency-check` tool requires access to the National Vulnerability Database (NVD). Due to strict rate limiting, it is **highly recommended** to use an NVD API Key:

1. Request a free API Key at [nvd.nist.gov](https://nvd.nist.gov/developers/api-key-request).
2. Once you have the key, run the scan using:
   ```bash
   mvn dependency-check:check -Psecurity -DnvdApiKey=YOUR_API_KEY
   ```
   Alternatively, you can set `nvdApiKey` in your `pom.xml` properties or as an environment variable.

### Run SonarQube Locally
1. Start the SonarQube environment:
   ```bash
   docker-compose -f docker-compose.quality.yml up -d
   ```
2. Run the analysis:
   ```bash
   mvn sonar:sonar -Psonar
   ```

## Workflow Integration
For AI agents (like Antigravity), use the `/check-quality` workflow to automatically run and report on these tools.
