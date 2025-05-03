package com.codejam.codex.authzen.controllers;

import com.codejam.codex.authzen.constants.ApiEndpoint;
import com.codejam.codex.authzen.dtos.inputs.UpdateUserRequest;
import com.codejam.codex.authzen.dtos.outputs.UpdateUserResponse;
import com.codejam.codex.authzen.dtos.outputs.UserResponse;
import com.codejam.codex.authzen.endpoint.AuthEndpoint;
import com.codejam.codex.authzen.endpoint.UserEndpoint;
import com.codejam.codex.authzen.responses.AuthzenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiEndpoint.USER)
@PreAuthorize("hasRole('USER')")
public class UserController {
    private final AuthEndpoint authEndpoint;
    private final UserEndpoint userEndpoint;

    public UserController(AuthEndpoint authEndpoint, UserEndpoint userEndpoint) {
        this.authEndpoint  = authEndpoint;
        this.userEndpoint  = userEndpoint;
    }

    @GetMapping(ApiEndpoint.AUTH_ME)
    @PreAuthorize("hasAuthority('VIEW_USER')")
    public ResponseEntity<AuthzenResponse<UserResponse>> getProfile(HttpServletRequest req) {
        String u = authEndpoint.getUsername(req);
        if (u == null || !authEndpoint.isAuthenticated(req)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthzenResponse.failure("Unauthorized: missing/invalid token"));
        }
        UserResponse profile = userEndpoint.getProfile(u);
        return ResponseEntity.ok(AuthzenResponse.success(profile, "User profile retrieved successfully"));
    }

    @PutMapping(ApiEndpoint.AUTH_UPDATE)
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    public ResponseEntity<AuthzenResponse<UpdateUserResponse>> updateProfile(
            HttpServletRequest req,
            @Valid @RequestBody UpdateUserRequest upd
    ) {
        String u = authEndpoint.getUsername(req);
        if (u == null || !authEndpoint.isAuthenticated(req)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthzenResponse.failure("Unauthorized"));
        }
        UpdateUserResponse out = userEndpoint.updateUser(u, upd);
        return ResponseEntity.ok(AuthzenResponse.success(out, "User updated successfully"));
    }

    @PostMapping(ApiEndpoint.AUTH_LOGOUT)
    @PreAuthorize("hasAuthority('USER_LOGOUT')")
    public ResponseEntity<AuthzenResponse<Object>> logout(HttpServletRequest req) {
        if (!authEndpoint.isAuthenticated(req)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthzenResponse.failure("Unauthorized"));
        }
        boolean blacklisted = authEndpoint.blacklistToken(req);
        if (blacklisted) {
            return ResponseEntity.ok(AuthzenResponse.success(null, "User logged out successfully"));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AuthzenResponse.failure("Failed to blacklist token"));
    }
}
