---
description: Run code quality and security analysis tools
---

This workflow executes the atomized quality profiles defined in the root `pom.xml`.

// turbo
1. Run all quality checks (Checkstyle, SpotBugs, JaCoCo, Dependency-Check):
   ```powershell
   c:\opt\apache-maven\bin\mvn.cmd clean verify -Pquality
   ```

2. (Optional) Run SonarQube analysis (requires SonarQube to be running):
   ```powershell
   c:\opt\apache-maven\bin\mvn.cmd sonar:sonar -Psonar
   ```

3. Review the generated reports:
   - JaCoCo: `target/site/jacoco/index.html`
   - OWASP Dependency Check: `target/dependency-check-report.html`
   - Checkstyle: `target/checkstyle-result.xml`
   - SpotBugs: `target/spotbugsXml.xml`
