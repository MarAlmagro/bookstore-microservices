# Security Policy

## Security Audit Baseline

This project has undergone a comprehensive security audit using the **OWASP Dependency-Check** tool (with NVD API integration).

### Current Status
As of **January 2026**, the project has been "hardened" to the maximum extent compatible with **Java 11** and **Spring Boot 2.7.18**.

| Dependency | Status | Action Taken |
|:---|:---|:---|
| **SnakeYAML** | ✅ Clean | Upgraded to 2.3 (resolves all known CVEs). |
| **Tomcat** | ⚠️ Hardened | Upgraded to 9.0.102 (latest Java 11 branch). |
| **Jackson** | ⚠️ Managed | Using latest 2.13.x patch compatible with SB 2.7. |
| **Spring Framework** | ⚠️ Legacy | Fixed at 5.3.31 (Java 11 limit). |

## Why are there still vulnerabilities?

If you run a security scan (e.g., `mvn dependency-check:check -Psecurity`), you may see approximately **30 remaining vulnerabilities**. These are strictly due to the following project constraints:

1.  **Java 11 Requirement:** Many critical security fixes for the Spring Framework are only available in **Spring 6.x**, which requires **Java 17+**.
2.  **Spring Boot 2.7 EOL:** Spring Boot 2.7 is End-of-Life. While individual libraries (like Tomcat) have been manually overridden to their latest compatible versions, the core framework is no longer receiving backports for certain low/medium severity risks.
3.  **Hiring Requisite:** This specific technology stack (Java 11 / Spring Boot 2.7) was chosen to demonstrate proficiency in maintaining and securing **Legacy/LTS Enterprise environments**.

## How to run your own scan
To reproduce the security report:
1.  Add your `NVD_API_KEY` to the `.env` file.
2.  Run the following command:
    ```bash
    mvn dependency-check:check -Psecurity -DnvdApiKey=YOUR_KEY
    ```

---
*This documentation was created as part of the "Quality and Security Audit" task to demonstrate professional security awareness.*
