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
            return ResponseEntity.ok(new AuthzenResponse<>(userResponse, true, "User registered successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new AuthzenResponse<>(null, false, "Invalid registration data: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthzenResponse<>(null, false, "Registration failed: " + e.getMessage()));
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
                    .map(t -> ResponseEntity.ok(new AuthzenResponse<>(t, true, "User logged in successfully")))
                    .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new AuthzenResponse<>(null, false, "Invalid credentials")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthzenResponse<>(null, false, "Login error: " + e.getMessage()));
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
                    .map(t -> ResponseEntity.ok(new AuthzenResponse<>(t, true, "User logged in via OAuth successfully")))
                    .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new AuthzenResponse<>(null, false, "OAuth login failed")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthzenResponse<>(null, false, "OAuth login error: " + e.getMessage()));
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
                return ResponseEntity.ok(new AuthzenResponse<>(null, true, "Password reset email sent"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthzenResponse<>(null, false, "Failed to send reset email"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthzenResponse<>(null, false, "Reset request failed: " + e.getMessage()));
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
                return ResponseEntity.ok(new AuthzenResponse<>(null, true, "Password reset successfully"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthzenResponse<>(null, false, "Password reset failed"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthzenResponse<>(null, false, "Reset error: " + e.getMessage()));
        }
    }

    /**
     * Refreshes the user's JWT access token using a valid refresh token.
     */
    @PostMapping(ApiEndpoint.AUTH_REFRESH)
    public ResponseEntity<AuthzenResponse<TokenResponse>> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            TokenResponse refreshed = authEndpoint.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(new AuthzenResponse<>(refreshed, true, "Token refreshed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthzenResponse<>(null, false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthzenResponse<>(null, false, "Refresh token error: " + e.getMessage()));
        }
    }
}
