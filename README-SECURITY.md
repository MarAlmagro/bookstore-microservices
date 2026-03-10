# Security Configuration Guide

## Required Environment Variables

Before running the application, you must configure the following environment variables. **The application will fail to start without these.**

### 1. JWT Secret (REQUIRED)

Generate a secure JWT secret (minimum 32 characters):

```bash
# Linux/macOS
openssl rand -base64 32

# Windows PowerShell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Minimum 0 -Maximum 256 }))
```

Add to your `.env` file:
```
JWT_SECRET=<your-generated-secret>
```

### 2. Database Passwords (REQUIRED)

Set strong passwords for all databases:

```
DB_PASSWORD=<mysql-root-password>
POSTGRES_PASSWORD=<postgres-password>
POSTGRES_USER=postgres
```

### 3. CORS Origins (REQUIRED)

Configure allowed origins for cross-origin requests:

```
CORS_ALLOWED_ORIGIN=http://localhost:4200
CORS_ADMIN_ORIGIN=http://localhost:8084
```

### 4. NVD API Key (OPTIONAL)

For dependency vulnerability scanning:

1. Request an API key from: https://nvd.nist.gov/developers/request-an-api-key
2. Add to `.env`:
```
NVD_API_KEY=<your-api-key>
```

## Setup Instructions

1. Copy the example environment file:
```bash
cp .env.example .env
```

2. Edit `.env` and fill in all required values (no empty values allowed)

3. Verify configuration:
```bash
# Check that JWT_SECRET is set and valid
echo $JWT_SECRET | wc -c  # Should be >= 32

# PowerShell equivalent
($env:JWT_SECRET).Length  # Should be >= 32
```
4. Start the application:
```bash
docker-compose up -d
```

## Security Features Implemented

### 1. Secrets Externalization
- ✅ No hardcoded secrets in source code
- ✅ JWT secret validation at startup (minimum 256 bits)
- ✅ Database credentials required via environment variables
- ✅ Exposed NVD API key removed and rotated

### 2. Actuator Endpoint Security
- ✅ `/actuator/health` and `/actuator/info` - Public (for load balancers)
- ✅ All other actuator endpoints - Require ADMIN role
- ✅ Sensitive endpoints disabled: `/actuator/env`, `/actuator/beans`, `/actuator/configprops`

### 3. CORS Configuration
- ✅ Centralized at API Gateway level
- ✅ Service-level CORS disabled (services only accept gateway traffic)
- ✅ Strict origin validation (no defaults)

### 4. Rate Limiting
- ✅ Login endpoint protected with rate limiting
- ✅ Maximum 5 failed attempts per email
- ✅ 15-minute lockout after exceeding limit
- ✅ Automatic cache expiration

## Testing Security

### Test JWT Secret Validation
```bash
# Should fail without JWT_SECRET
unset JWT_SECRET
mvn spring-boot:run -pl user-service
# Expected: IllegalStateException - "JWT_SECRET environment variable must be set"
```

### Test Actuator Access Control
```bash
# Public endpoints (should work)
curl http://localhost:8083/actuator/health


# Protected endpoints (should return 401/403)
curl http://localhost:8083/actuator/env
curl http://localhost:8083/actuator/beans
```

### Test Login Rate Limiting
```bash
# Attempt 6 failed logins
for i in {1..6}; do
  curl -X POST http://localhost:8080/api/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test@test.com","password":"wrong"}'
  echo ""
done
# Expected: 6th attempt returns "Account temporarily locked"
```

## Production Deployment

### Additional Recommendations

1.**Use a secrets management service** in production:
   - AWS Secrets Manager
   - HashiCorp Vault
   - Azure Key Vault
   - Google Secret Manager

2. **Enable HTTPS** for all external communication

3. **Set up monitoring** for failed login attempts

4. **Implement IP-based rate limiting** at the infrastructure level

5. **Regular security audits** and dependency updates

## Troubleshooting

### Application won't start
- Verify all required environment variables are set in `.env`
- Check JWT_SECRET is at least 32 characters
- Ensure database passwords are set

### CORS errors
- Verify CORS_ALLOWED_ORIGIN and CORS_ADMIN_ORIGIN are set
- Check that origins match your frontend URLs exactly

### Login locked out
- Wait 15 minutes for automatic unlock
- Or restart the user-service to clear the cache (not recommended in production)

## References

- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- [Spring Security Best Practices](https://docs.spring.io/spring-security/reference/)
- [12-Factor App: Config](https://12factor.net/config)
