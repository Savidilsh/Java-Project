package com.codejam.codex.authzen.controllers;

import com.codejam.codex.authzen.constants.ApiEndpoint;
import com.codejam.codex.authzen.dtos.inputs.DelegateRequest;
import com.codejam.codex.authzen.dtos.inputs.RoleRequest;
import com.codejam.codex.authzen.dtos.inputs.RoleUpdateRequest;
import com.codejam.codex.authzen.dtos.outputs.AuditLogResponse;
import com.codejam.codex.authzen.dtos.outputs.UpdateUserResponse;
import com.codejam.codex.authzen.dtos.outputs.UserResponse;
import com.codejam.codex.authzen.endpoint.AdminEndpoint;
import com.codejam.codex.authzen.endpoint.AuthEndpoint;
import com.codejam.codex.authzen.responses.AuthzenResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Handles administrative endpoints such as user management,
 * role updates, audit logs, and permission delegation.
 */
@RestController
@RequestMapping(ApiEndpoint.ADMIN)
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminEndpoint adminEndpoint;
    private final AuthEndpoint authEndpoint;

    @Autowired
    public AdminController(AdminEndpoint adminEndpoint, AuthEndpoint authEndpoint) {
        this.adminEndpoint = adminEndpoint;
        this.authEndpoint = authEndpoint;
    }

    /**
     * Ensures the request is made by an authenticated admin.
     *
     * @param request current HTTP request
     * @return authenticated admin username
     */
    private String verifyAdmin(HttpServletRequest request) {
        String username = authEndpoint.getUsername(request);
        if (username == null || !authEndpoint.isAuthenticated(request)) {
            throw new AccessDeniedException("Unauthorized: Invalid or missing token.");
        }

        UserResponse userResponse = authEndpoint.getUserDetails(username);
        if (userResponse == null || !userResponse.getRoles().contains("ROLE_ADMIN")) {
            throw new AccessDeniedException("Forbidden: Insufficient permissions.");
        }

        return username;
    }

    @GetMapping(ApiEndpoint.ADMIN_ALL_USERS)
    @Secured("ROLE_ADMIN")
    @PreAuthorize("hasAuthority('VIEW_USER')")
    public ResponseEntity<AuthzenResponse<List<UserResponse>>> getAllUsers(HttpServletRequest request) {
        String username = verifyAdmin(request);
        List<UserResponse> users = adminEndpoint.getAllUsers(username);
        return ResponseEntity.ok(new AuthzenResponse<>(users, true, "Users listed successfully."));
    }

    @GetMapping(ApiEndpoint.ADMIN_USERS)
    @Secured("ROLE_ADMIN")
    @PreAuthorize("hasAuthority('VIEW_USER')")
    public ResponseEntity<AuthzenResponse<UserResponse>> getUserDetails(@PathVariable("id") Long userId, HttpServletRequest request) {
        verifyAdmin(request);
        UserResponse userResponse = adminEndpoint.getUserById(userId);
        return ResponseEntity.ok(new AuthzenResponse<>(userResponse, true, "User details retrieved successfully."));
    }

    @PutMapping(ApiEndpoint.ADMIN_USER_ROLES)
    @Secured("ROLE_ADMIN")
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    public ResponseEntity<AuthzenResponse<UpdateUserResponse>> updateUserRole(
            @PathVariable("id") Long userId,
            @RequestBody RoleUpdateRequest roleUpdateRequest,
            HttpServletRequest request) {
        String username = verifyAdmin(request);
        UpdateUserResponse updated = adminEndpoint.updateUserRoles(userId, roleUpdateRequest, username);
        return ResponseEntity.ok(new AuthzenResponse<>(updated, true, "User roles updated successfully."));
    }

    @PostMapping(ApiEndpoint.ADMIN_ROLES)
    @Secured("ROLE_ADMIN")
    @PreAuthorize("hasAuthority('CREATE_USER')")
    public ResponseEntity<AuthzenResponse<String>> createRole(
            @RequestBody RoleRequest roleRequest,
            HttpServletRequest request) {
        String username = verifyAdmin(request);
        String created = adminEndpoint.createRole(roleRequest, username);
        return ResponseEntity.ok(new AuthzenResponse<>(null, true, created));
    }

    @GetMapping(ApiEndpoint.ADMIN_AUDIT_LOGS)
    @Secured("ROLE_ADMIN")
    @PreAuthorize("hasAuthority('VIEW_USER')")
    public ResponseEntity<AuthzenResponse<List<AuditLogResponse>>> getAuditLogs(HttpServletRequest request) {
        String username = verifyAdmin(request);
        List<AuditLogResponse> auditLogs = adminEndpoint.getAuditLogs(username);
        return ResponseEntity.ok(new AuthzenResponse<>(auditLogs, true, "Audit logs listed successfully."));
    }

    @PostMapping(ApiEndpoint.ADMIN_DELEGATE)
    @Secured("ROLE_ADMIN")
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    public ResponseEntity<AuthzenResponse<String>> delegatePermissions(
            @RequestBody DelegateRequest delegateRequest,
            HttpServletRequest request) {
        String username = verifyAdmin(request);
        String message = adminEndpoint.delegatePermissions(delegateRequest, username);
        return ResponseEntity.ok(new AuthzenResponse<>(null, true, message));
    }
}
