package com.bookstore.common.constants;

/**
 * Enum representing user roles in the bookstore system.
 * <p>
 * Used for role-based access control (RBAC) and authorization.
 * </p>
 */
public enum UserRole {
    /**
     * Standard customer role with basic permissions
     * - Can browse books
     * - Can create orders
     * - Can view own profile and orders
     */
    CUSTOMER,

    /**
     * Administrator role with elevated permissions
     * - All customer permissions
     * - Can manage book catalog
     * - Can view all orders
     * - Can manage users
     */
    ADMIN
}
