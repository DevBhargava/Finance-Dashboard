package com.finance.dashboard.enums;

/**
 * User roles in the system.
 * VIEWER  -> read-only access to dashboard and transactions
 * ANALYST -> read access + summary/insights endpoints
 * ADMIN   -> full access including user management
 */
public enum Role {
    VIEWER,
    ANALYST,
    ADMIN
}
