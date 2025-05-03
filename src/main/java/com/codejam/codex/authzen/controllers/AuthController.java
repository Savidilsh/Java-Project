package com.codejam.codex.authzen.controllers;

import com.codejam.codex.authzen.constants.ApiEndpoint;
import com.codejam.codex.authzen.dtos.inputs.*;
import com.codejam.codex.authzen.dtos.outputs.TokenResponse;
import com.codejam.codex.authzen.dtos.outputs.UserResponse;
import com.codejam.codex.authzen.responses.AuthzenResponse;
import com.codejam.codex.authzen.endpoint.AuthEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controller responsible for handling authentication and authorization related HTTP requests.
 */
@RestController
@RequestMapping(ApiEndpoint.AUTH)
public class AuthController {

    private final AuthEndpoint authEndpoint;

    @Autowired
    public AuthController(AuthEndpoint authEndpoint) {
        this.authEndpoint = authEndpoint;
    }

    /**
     * Registers a new user.
     */
    @PostMapping(ApiEndpoint.AUTH_REGISTER)
    public ResponseEntity<AuthzenResponse<UserResponse>> register(@RequestBody RegisterRequest request) {
        try {
            UserResponse userResponse = authEndpoint.registerUser(request);
            return ResponseEntity.ok(AuthzenResponse.success(userResponse, "User registered successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(AuthzenResponse.failure("Invalid registration data: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthzenResponse.failure("Registration failed: " + e.getMessage()));
        }
    }

    /**
     * Authenticates a user and issues access and refresh tokens.
     */
    @PostMapping(ApiEndpoint.AUTH_LOGIN)
    public ResponseEntity<AuthzenResponse<TokenResponse>> login(@RequestBody LoginRequest request) {
        try {
            TokenResponse token = authEndpoint.authenticateUser(request);
            return Optional.ofNullable(token)
                    .map(t -> ResponseEntity.ok(AuthzenResponse.success(t, "User logged in successfully")))
                    .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(AuthzenResponse.failure("Invalid credentials")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthzenResponse.failure("Login error: " + e.getMessage()));
        }
    }

    /**
     * Authenticates user via OAuth credentials.
     */
    @PostMapping(ApiEndpoint.AUTH_OAUTH)
    public ResponseEntity<AuthzenResponse<TokenResponse>> oauthLogin(@RequestBody OAuthRequest request) {
        try {
            TokenResponse oauthToken = authEndpoint.authenticateOAuth(request);
            return Optional.ofNullable(oauthToken)
                    .map(t -> ResponseEntity.ok(AuthzenResponse.success(t, "User logged in via OAuth successfully")))
                    .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(AuthzenResponse.failure("OAuth login failed")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthzenResponse.failure("OAuth login error: " + e.getMessage()));
        }
    }

    /**
     * Sends password reset email.
     */
    @PostMapping(ApiEndpoint.AUTH_RESET_REQUEST)
    public ResponseEntity<AuthzenResponse<Object>> resetPasswordRequest(@RequestBody ResetRequest request) {
        try {
            boolean sent = authEndpoint.sendPasswordResetEmail(request);
            if (sent) {
                return ResponseEntity.ok(AuthzenResponse.success(null, "Password reset email sent"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthzenResponse.failure("Failed to send reset email"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthzenResponse.failure("Reset request failed: " + e.getMessage()));
        }
    }

    /**
     * Resets user password using token.
     */
    @PostMapping(ApiEndpoint.AUTH_RESET_PASSWORD)
    public ResponseEntity<AuthzenResponse<Object>> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            boolean success = authEndpoint.resetUserPassword(request);
            if (success) {
                return ResponseEntity.ok(AuthzenResponse.success(null, "Password reset successfully"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthzenResponse.failure("Password reset failed"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthzenResponse.failure("Reset error: " + e.getMessage()));
        }
    }

    /**
     * Refreshes the user's JWT access token using a valid refresh token.
     */
    @PostMapping(ApiEndpoint.AUTH_REFRESH)
    public ResponseEntity<AuthzenResponse<TokenResponse>> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            TokenResponse refreshed = authEndpoint.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(AuthzenResponse.success(refreshed, "Token refreshed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthzenResponse.failure(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthzenResponse.failure("Refresh token error: " + e.getMessage()));
        }
    }
}
