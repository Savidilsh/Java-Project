package com.codejam.codex.authzen.controllers;

import com.codejam.codex.authzen.dtos.inputs.UpdateUserRequest;
import com.codejam.codex.authzen.dtos.outputs.UpdateUserResponse;
import com.codejam.codex.authzen.dtos.outputs.UserResponse;
import com.codejam.codex.authzen.endpoint.AuthEndpoint;
import com.codejam.codex.authzen.endpoint.UserEndpoint;
import com.codejam.codex.authzen.responses.AuthzenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/authenticate/user")
@PreAuthorize("hasRole('USER')")
public class UserController {

    private final AuthEndpoint authEndpoint;
    private final UserEndpoint userEndpoint;

    @Autowired
    public UserController(AuthEndpoint authEndpoint, UserEndpoint userEndpoint) {
        this.authEndpoint = authEndpoint;
        this.userEndpoint = userEndpoint;
    }

    @PreAuthorize("hasAuthority('VIEW_USER')")
    @GetMapping("/me")
    public ResponseEntity<AuthzenResponse<UserResponse>> getProfile(HttpServletRequest request) {
        String username = authEndpoint.getUsername(request);
        if (username == null || !authEndpoint.isAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthzenResponse.failure("Unauthorized: Invalid or missing token"));
        }

        UserResponse profile = userEndpoint.getProfile(username);
        return ResponseEntity.ok(AuthzenResponse.success(profile, "User profile retrieved successfully"));
    }

    @PreAuthorize("hasAuthority('UPDATE_USER')")
    @PutMapping("/update")
    public ResponseEntity<AuthzenResponse<UpdateUserResponse>> updateProfile(
            HttpServletRequest request,
            @Valid @RequestBody UpdateUserRequest updateRequest) {

        String username = authEndpoint.getUsername(request);
        if (username == null || !authEndpoint.isAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthzenResponse.failure("Unauthorized"));
        }

        UpdateUserResponse result = userEndpoint.updateUser(username, updateRequest);
        return ResponseEntity.ok(AuthzenResponse.success(result, "User updated successfully"));
    }

    @PreAuthorize("hasAuthority('USER_LOGOUT')")
    @PostMapping("/logout")
    public ResponseEntity<AuthzenResponse<Object>> logout(HttpServletRequest request) {
        if (!authEndpoint.isAuthenticated(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthzenResponse.failure("Unauthorized"));
        }

        boolean blacklisted = authEndpoint.blacklistToken(request);
        if (blacklisted) {
            return ResponseEntity.ok(AuthzenResponse.success(null, "User logged out successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthzenResponse.failure("Failed to blacklist token"));
        }
    }
}