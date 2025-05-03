package com.codejam.codex.authzen.constants;

import javax.annotation.processing.Generated;

/**
 * Centralized definition of all REST API endpoint paths.
 */
@Generated("ApiEndpoint")
public final class ApiEndpoint {
    public static final String AUTHENTICATE_PATH = "/api/authenticate";

    public static final String ADMIN = AUTHENTICATE_PATH + "/admin";
    public static final String AUTH  = AUTHENTICATE_PATH + "/auth";
    public static final String USER  = AUTHENTICATE_PATH + "/user";

    public static final String HEALTH = AUTHENTICATE_PATH + "/health";

    // Public auth
    public static final String AUTH_REGISTER     = "/register";
    public static final String AUTH_LOGIN        = "/login";
    public static final String AUTH_OAUTH        = "/oauth";
    public static final String AUTH_RESET_REQUEST= "/reset-request";
    public static final String AUTH_RESET_PASSWORD = "/reset-password";
    public static final String AUTH_REFRESH      = "/refresh";

    // Authenticated user
    public static final String AUTH_ME     = "/me";
    public static final String AUTH_UPDATE = "/update";
    public static final String AUTH_LOGOUT = "/logout";

    // Admin
    public static final String ADMIN_ALL_USERS   = "/users";
    public static final String ADMIN_USERS       = "/users/{id}";
    public static final String ADMIN_USER_ROLES  = "/users/{id}/roles";
    public static final String ADMIN_AUDIT_LOGS  = "/audit-logs";
    public static final String ADMIN_ROLES       = "/roles";
    public static final String ADMIN_DELEGATE    = "/delegate";

    private ApiEndpoint() {}
}
