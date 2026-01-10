# Phase 10.5: UI Validation & Verification Test Script (PowerShell)
# This script automates the validation tests for the Admin UI

$ErrorActionPreference = "Stop"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Phase 10.5: UI Validation & Verification" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Test results
$script:TestsPassed = 0
$script:TestsFailed = 0

# Function to print test result
function Print-Result {
    param(
        [bool]$Success,
        [string]$Message
    )
    
    if ($Success) {
        Write-Host "✅ PASS: $Message" -ForegroundColor Green
        $script:TestsPassed++
    } else {
        Write-Host "❌ FAIL: $Message" -ForegroundColor Red
        $script:TestsFailed++
    }
}

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "10.5.1: Visual, Accessibility & i18n Audit" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Test 10.5.1.1: Responsive Check
Write-Host "Test 10.5.1.1: Responsive Design" -ForegroundColor Yellow
Write-Host "--------------------------------" -ForegroundColor Yellow

# Check for responsive classes in base.html
$baseHtml = Get-Content "admin-ui\src\main\resources\templates\base.html" -Raw
Print-Result ($baseHtml -match "hidden md:flex") "Sidebar has responsive classes (hidden md:flex)"

# Check for responsive grid in dashboard
$dashboardHtml = Get-Content "admin-ui\src\main\resources\templates\dashboard.html" -Raw
Print-Result ($dashboardHtml -match "grid-cols-1 md:grid-cols-2 lg:grid-cols-4") "Dashboard grid has responsive layout"

# Check for overflow handling in tables
$booksListHtml = Get-Content "admin-ui\src\main\resources\templates\books\list.html" -Raw
Print-Result ($booksListHtml -match "overflow-x-auto") "Books table has mobile overflow handling"

Write-Host ""

# Test 10.5.1.2: i18n Verification
Write-Host "Test 10.5.1.2: i18n Configuration" -ForegroundColor Yellow
Write-Host "---------------------------------" -ForegroundColor Yellow

# Check application.yml for i18n config
$appYml = Get-Content "admin-ui\src\main\resources\application.yml" -Raw
Print-Result ($appYml -match "basename: i18n/messages") "i18n basename configured correctly"

# Check for Spanish translations
Print-Result (Test-Path "shared-common\src\main\resources\i18n\messages_es.properties") "Spanish translations file exists"

# Check for English translations
Print-Result (Test-Path "shared-common\src\main\resources\i18n\messages.properties") "English translations file exists"

# Check for language switcher in templates
Print-Result ($baseHtml -match "lang='es'") "Language switcher implemented"

# Verify i18n keys are used in templates
Print-Result ($dashboardHtml -match 'th:text="#{') "Templates use i18n keys"

Write-Host ""

# Test 10.5.1.3: A11y & Agent-Readiness
Write-Host "Test 10.5.1.3: Accessibility & Test Hooks" -ForegroundColor Yellow
Write-Host "-----------------------------------------" -ForegroundColor Yellow

# Check for data-testid attributes
$testidFiles = Get-ChildItem -Path "admin-ui\src\main\resources\templates" -Filter "*.html" -Recurse
$testidCount = ($testidFiles | Select-String -Pattern "data-testid").Count
Print-Result ($testidCount -gt 30) "Sufficient data-testid attributes found ($testidCount)"

# Check for semantic HTML
Print-Result ($baseHtml -match 'role="navigation"') "ARIA navigation role present"
Print-Result ($baseHtml -match 'role="main"') "ARIA main role present"

# Check for form labels
$labelCount = ($testidFiles | Select-String -Pattern '<label for=').Count
Print-Result ($labelCount -gt 5) "Form labels properly associated ($labelCount)"

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "10.5.2: Integration & Design Token Validation" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Test 10.5.2.1: Token Consistency
Write-Host "Test 10.5.2.1: Design Token Validation" -ForegroundColor Yellow
Write-Host "--------------------------------------" -ForegroundColor Yellow

# Check for design tokens file
Print-Result (Test-Path "shared-common\src\main\resources\design\tokens.json") "Design tokens file exists"

# Check for brand-primary color
$tokensJson = Get-Content "shared-common\src\main\resources\design\tokens.json" -Raw
Print-Result ($tokensJson -match '"primary": "#4F46E5"') "Brand primary color defined (#4F46E5)"

# Check Tailwind config imports tokens
$tailwindConfig = Get-Content "admin-ui\tailwind.config.js" -Raw
Print-Result ($tailwindConfig -match "require\('./src/main/resources/design/tokens.json'\)") "Tailwind config imports design tokens"

# Check for token usage in CSS
$inputCss = Get-Content "admin-ui\src\main\resources\static\css\input.css" -Raw
Print-Result ($inputCss -match "brand-primary") "CSS uses brand-primary token"
Print-Result ($inputCss -match "status-success") "CSS uses status color tokens"

Write-Host ""

# Test 10.5.2.2: E2E Data Flow (Manual)
Write-Host "Test 10.5.2.2: E2E Data Flow" -ForegroundColor Yellow
Write-Host "----------------------------" -ForegroundColor Yellow
Write-Host "⏳ MANUAL TEST REQUIRED" -ForegroundColor Yellow
Write-Host "This test requires running services. Please run:"
Write-Host "  1. docker-compose up -d"
Write-Host "  2. Access http://localhost:8090/admin"
Write-Host "  3. Login with admin/admin123"
Write-Host "  4. Create a test book and verify it appears in the list"
Write-Host ""

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "10.5.3: Security & RBAC" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Test 10.5.3.1: Security Configuration
Write-Host "Test 10.5.3.1: Security Configuration" -ForegroundColor Yellow
Write-Host "-------------------------------------" -ForegroundColor Yellow

# Check for Spring Security configuration
Print-Result ($appYml -match "spring.security") "Spring Security configured"

# Check for JWT secret
Print-Result ($appYml -match "jwt.secret") "JWT secret configured"

# Check for session configuration
Print-Result ($appYml -match "session:") "Session configuration present"

Write-Host ""
Write-Host "⏳ MANUAL TESTS REQUIRED" -ForegroundColor Yellow
Write-Host "  - Test unauthorized access with ROLE_CUSTOMER"
Write-Host "  - Verify JWT propagation through Gateway"
Write-Host ""

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Test Summary" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Passed: $script:TestsPassed" -ForegroundColor Green
Write-Host "Failed: $script:TestsFailed" -ForegroundColor Red
Write-Host ""

if ($script:TestsFailed -eq 0) {
    Write-Host "✅ All automated tests passed!" -ForegroundColor Green
    exit 0
} else {
    Write-Host "❌ Some tests failed. Please review the results above." -ForegroundColor Red
    exit 1
}
