#!/bin/bash

# Phase 10.5: UI Validation & Verification Test Script
# This script automates the validation tests for the Admin UI

set -e

echo "=========================================="
echo "Phase 10.5: UI Validation & Verification"
echo "=========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test results
TESTS_PASSED=0
TESTS_FAILED=0

# Function to print test result
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ PASS${NC}: $2"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ FAIL${NC}: $2"
        ((TESTS_FAILED++))
    fi
}

echo "=========================================="
echo "10.5.1: Visual, Accessibility & i18n Audit"
echo "=========================================="
echo ""

# Test 10.5.1.1: Responsive Check
echo "Test 10.5.1.1: Responsive Design"
echo "--------------------------------"

# Check for responsive classes in base.html
if grep -q "hidden md:flex" admin-ui/src/main/resources/templates/base.html; then
    print_result 0 "Sidebar has responsive classes (hidden md:flex)"
else
    print_result 1 "Sidebar missing responsive classes"
fi

# Check for responsive grid in dashboard
if grep -q "grid-cols-1 md:grid-cols-2 lg:grid-cols-4" admin-ui/src/main/resources/templates/dashboard.html; then
    print_result 0 "Dashboard grid has responsive layout"
else
    print_result 1 "Dashboard grid missing responsive layout"
fi

# Check for overflow handling in tables
if grep -q "overflow-x-auto" admin-ui/src/main/resources/templates/books/list.html; then
    print_result 0 "Books table has mobile overflow handling"
else
    print_result 1 "Books table missing overflow handling"
fi

echo ""

# Test 10.5.1.2: i18n Verification
echo "Test 10.5.1.2: i18n Configuration"
echo "---------------------------------"

# Check application.yml for i18n config
if grep -q "basename: i18n/messages" admin-ui/src/main/resources/application.yml; then
    print_result 0 "i18n basename configured correctly"
else
    print_result 1 "i18n basename not configured"
fi

# Check for Spanish translations
if [ -f "shared-common/src/main/resources/i18n/messages_es.properties" ]; then
    print_result 0 "Spanish translations file exists"
else
    print_result 1 "Spanish translations file missing"
fi

# Check for English translations
if [ -f "shared-common/src/main/resources/i18n/messages.properties" ]; then
    print_result 0 "English translations file exists"
else
    print_result 1 "English translations file missing"
fi

# Check for language switcher in templates
if grep -q "lang='es'" admin-ui/src/main/resources/templates/base.html; then
    print_result 0 "Language switcher implemented"
else
    print_result 1 "Language switcher missing"
fi

# Verify i18n keys are used in templates
if grep -q 'th:text="#{' admin-ui/src/main/resources/templates/dashboard.html; then
    print_result 0 "Templates use i18n keys"
else
    print_result 1 "Templates not using i18n keys"
fi

echo ""

# Test 10.5.1.3: A11y & Agent-Readiness
echo "Test 10.5.1.3: Accessibility & Test Hooks"
echo "-----------------------------------------"

# Check for data-testid attributes
testid_count=$(grep -r "data-testid" admin-ui/src/main/resources/templates/ | wc -l)
if [ $testid_count -gt 30 ]; then
    print_result 0 "Sufficient data-testid attributes found ($testid_count)"
else
    print_result 1 "Insufficient data-testid attributes ($testid_count)"
fi

# Check for semantic HTML
if grep -q 'role="navigation"' admin-ui/src/main/resources/templates/base.html; then
    print_result 0 "ARIA navigation role present"
else
    print_result 1 "ARIA navigation role missing"
fi

if grep -q 'role="main"' admin-ui/src/main/resources/templates/base.html; then
    print_result 0 "ARIA main role present"
else
    print_result 1 "ARIA main role missing"
fi

# Check for form labels
label_count=$(grep -r '<label for=' admin-ui/src/main/resources/templates/ | wc -l)
if [ $label_count -gt 5 ]; then
    print_result 0 "Form labels properly associated ($label_count)"
else
    print_result 1 "Insufficient form labels ($label_count)"
fi

echo ""
echo "=========================================="
echo "10.5.2: Integration & Design Token Validation"
echo "=========================================="
echo ""

# Test 10.5.2.1: Token Consistency
echo "Test 10.5.2.1: Design Token Validation"
echo "--------------------------------------"

# Check for design tokens file
if [ -f "shared-common/src/main/resources/design/tokens.json" ]; then
    print_result 0 "Design tokens file exists"
else
    print_result 1 "Design tokens file missing"
fi

# Check for brand-primary color
if grep -q '"primary": "#4F46E5"' shared-common/src/main/resources/design/tokens.json; then
    print_result 0 "Brand primary color defined (#4F46E5)"
else
    print_result 1 "Brand primary color not defined correctly"
fi

# Check Tailwind config imports tokens
if grep -q "require('./src/main/resources/design/tokens.json')" admin-ui/tailwind.config.js; then
    print_result 0 "Tailwind config imports design tokens"
else
    print_result 1 "Tailwind config doesn't import tokens"
fi

# Check for token usage in CSS
if grep -q "brand-primary" admin-ui/src/main/resources/static/css/input.css; then
    print_result 0 "CSS uses brand-primary token"
else
    print_result 1 "CSS doesn't use brand-primary token"
fi

# Check for status colors
if grep -q "status-success" admin-ui/src/main/resources/static/css/input.css; then
    print_result 0 "CSS uses status color tokens"
else
    print_result 1 "CSS doesn't use status color tokens"
fi

echo ""

# Test 10.5.2.2: E2E Data Flow (Manual)
echo "Test 10.5.2.2: E2E Data Flow"
echo "----------------------------"
echo -e "${YELLOW}⏳ MANUAL TEST REQUIRED${NC}"
echo "This test requires running services. Please run:"
echo "  1. docker-compose up -d"
echo "  2. Access http://localhost:8090/admin"
echo "  3. Login with admin/admin123"
echo "  4. Create a test book and verify it appears in the list"
echo ""

echo "=========================================="
echo "10.5.3: Security & RBAC"
echo "=========================================="
echo ""

# Test 10.5.3.1: Security Configuration
echo "Test 10.5.3.1: Security Configuration"
echo "-------------------------------------"

# Check for Spring Security configuration
if grep -q "spring.security" admin-ui/src/main/resources/application.yml; then
    print_result 0 "Spring Security configured"
else
    print_result 1 "Spring Security not configured"
fi

# Check for JWT secret
if grep -q "jwt.secret" admin-ui/src/main/resources/application.yml; then
    print_result 0 "JWT secret configured"
else
    print_result 1 "JWT secret not configured"
fi

# Check for session configuration
if grep -q "session:" admin-ui/src/main/resources/application.yml; then
    print_result 0 "Session configuration present"
else
    print_result 1 "Session configuration missing"
fi

echo ""
echo -e "${YELLOW}⏳ MANUAL TESTS REQUIRED${NC}"
echo "  - Test unauthorized access with ROLE_CUSTOMER"
echo "  - Verify JWT propagation through Gateway"
echo ""

echo "=========================================="
echo "Test Summary"
echo "=========================================="
echo -e "${GREEN}Passed: $TESTS_PASSED${NC}"
echo -e "${RED}Failed: $TESTS_FAILED${NC}"
echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}✅ All automated tests passed!${NC}"
    exit 0
else
    echo -e "${RED}❌ Some tests failed. Please review the results above.${NC}"
    exit 1
fi
