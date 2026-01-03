-- User Service Database Initialization Script
-- Creates admin and customer test accounts with BCrypt hashed passwords

-- Note: All passwords are BCrypt hashed with strength 10
-- Test Credentials (for .env.example documentation):
-- Admin: admin@bookstore.com / admin123
-- Customers:
--   - john.doe@bookstore.com / customer123
--   - jane.smith@bookstore.com / customer123
--   - mike.wilson@bookstore.com / customer123
--   - sarah.jones@bookstore.com / customer123
--   - alex.brown@bookstore.com / customer123

-- Create users table if not exists (matches JPA entity structure)
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Insert Admin Account
-- Password: admin123 (BCrypt hashed)
INSERT INTO users (email, password, first_name, last_name, role, created_at, updated_at, enabled)
VALUES (
    'admin@bookstore.com',
    '$2a$10$FRjmPaqwA.oVx6Ae./LYOehm6oE7tbmdpQE6CSaIpr5rL5ohh8Ity',
    'Admin',
    'User',
    'ADMIN',
    NOW(),
    NOW(),
    true
) ON CONFLICT (email) DO NOTHING;

-- Insert Customer Accounts
-- Password for all: customer123 (BCrypt hashed)
INSERT INTO users (email, password, first_name, last_name, role, created_at, updated_at, enabled)
VALUES
(
    'john.doe@bookstore.com',
    '$2a$10$dTDo926ioFAU9/XjJr.oVe.zYhmadM7M7N8f.pEaVrzPuUi8HDdfC',
    'John',
    'Doe',
    'CUSTOMER',
    NOW(),
    NOW(),
    true
),
(
    'jane.smith@bookstore.com',
    '$2a$10$dTDo926ioFAU9/XjJr.oVe.zYhmadM7M7N8f.pEaVrzPuUi8HDdfC',
    'Jane',
    'Smith',
    'CUSTOMER',
    NOW(),
    NOW(),
    true
),
(
    'mike.wilson@bookstore.com',
    '$2a$10$dTDo926ioFAU9/XjJr.oVe.zYhmadM7M7N8f.pEaVrzPuUi8HDdfC',
    'Mike',
    'Wilson',
    'CUSTOMER',
    NOW(),
    NOW(),
    true
),
(
    'sarah.jones@bookstore.com',
    '$2a$10$dTDo926ioFAU9/XjJr.oVe.zYhmadM7M7N8f.pEaVrzPuUi8HDdfC',
    'Sarah',
    'Jones',
    'CUSTOMER',
    NOW(),
    NOW(),
    true
),
(
    'alex.brown@bookstore.com',
    '$2a$10$dTDo926ioFAU9/XjJr.oVe.zYhmadM7M7N8f.pEaVrzPuUi8HDdfC',
    'Alex',
    'Brown',
    'CUSTOMER',
    NOW(),
    NOW(),
    true
) ON CONFLICT (email) DO NOTHING;

-- Verify data insertion
SELECT 'User database initialization completed successfully' AS status;
SELECT COUNT(*) AS total_users FROM users;
SELECT email, first_name, last_name, role FROM users ORDER BY role DESC, email;
