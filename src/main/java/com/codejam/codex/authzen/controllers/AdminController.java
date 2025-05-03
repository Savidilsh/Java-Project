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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiEndpoint.ADMIN)
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminEndpoint adminEndpoint;
    private final AuthEndpoint authEndpoint;

    public AdminController(AdminEndpoint adminEndpoint, AuthEndpoint authEndpoint) {
        this.adminEndpoint = adminEndpoint;
        this.authEndpoint  = authEndpoint;
    }

    private String verifyAdmin(HttpServletRequest request) {
        String username = authEndpoint.getUsername(request);
        if (username == null || !authEndpoint.isAuthenticated(request)) {
            throw new AccessDeniedException("Unauthorized: missing/invalid token");
        }
        UserResponse u = authEndpoint.getUserDetails(username);
        if (u == null || !u.getRoles().contains("ROLE_ADMIN")) {
            throw new AccessDeniedException("Forbidden: insufficient permissions");
        }
        return username;
    }

    @GetMapping(ApiEndpoint.ADMIN_ALL_USERS)
    @Secured("ROLE_ADMIN")
    @PreAuthorize("hasAuthority('VIEW_USER')")
    public ResponseEntity<AuthzenResponse<List<UserResponse>>> getAllUsers(HttpServletRequest req) {
        String who = verifyAdmin(req);
        List<UserResponse> list = adminEndpoint.getAllUsers(who);
        return ResponseEntity.ok(AuthzenResponse.success(list, "Users listed successfully."));
    }

    @GetMapping(ApiEndpoint.ADMIN_USERS)
    @Secured("ROLE_ADMIN")
    @PreAuthorize("hasAuthority('VIEW_USER')")
    public ResponseEntity<AuthzenResponse<UserResponse>> getUserDetails(
            @PathVariable("id") Long id, HttpServletRequest req) {
        verifyAdmin(req);
        UserResponse u = adminEndpoint.getUserById(id);
        return ResponseEntity.ok(AuthzenResponse.success(u, "User details retrieved successfully."));
    }

    @PutMapping(ApiEndpoint.ADMIN_USER_ROLES)
    @Secured("ROLE_ADMIN")
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    public ResponseEntity<AuthzenResponse<UpdateUserResponse>> updateUserRole(
            @PathVariable("id") Long id,
            @RequestBody RoleUpdateRequest request,
            HttpServletRequest httpReq
    ) {
        String who = verifyAdmin(httpReq);
        UpdateUserResponse out = adminEndpoint.updateUserRoles(id, request, who);
        return ResponseEntity.ok(AuthzenResponse.success(out, "User roles updated successfully."));
    }

    @PostMapping(ApiEndpoint.ADMIN_ROLES)
    @Secured("ROLE_ADMIN")
    @PreAuthorize("hasAuthority('CREATE_USER')")
    public ResponseEntity<AuthzenResponse<String>> createRole(
            @RequestBody RoleRequest request,
            HttpServletRequest httpReq
    ) {
        String who = verifyAdmin(httpReq);
        String msg = adminEndpoint.createRole(request, who);
        return ResponseEntity.ok(AuthzenResponse.success(msg, "Role created successfully."));
    }

    @GetMapping(ApiEndpoint.ADMIN_AUDIT_LOGS)
    @Secured("ROLE_ADMIN")
    @PreAuthorize("hasAuthority('VIEW_AUDIT_LOG')")
    public ResponseEntity<AuthzenResponse<List<AuditLogResponse>>> getAuditLogs(HttpServletRequest req) {
        String who = verifyAdmin(req);
        List<AuditLogResponse> logs = adminEndpoint.getAuditLogs(who);
        return ResponseEntity.ok(AuthzenResponse.success(logs, "Audit logs listed successfully."));
    }

    @PostMapping(ApiEndpoint.ADMIN_DELEGATE)
    @Secured("ROLE_ADMIN")
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    public ResponseEntity<AuthzenResponse<String>> delegatePermissions(
            @RequestBody DelegateRequest dr,
            HttpServletRequest req
    ) {
        String who = verifyAdmin(req);
        String msg = adminEndpoint.delegatePermissions(dr, who);
        return ResponseEntity.ok(AuthzenResponse.success(msg, "Permissions delegated successfully."));
    }
}
