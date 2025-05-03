package com.codejam.codex.authzen.controllers;

import com.codejam.codex.authzen.constants.ApiEndpoint;
import com.codejam.codex.authzen.dtos.inputs.*;
import com.codejam.codex.authzen.dtos.outputs.TokenResponse;
import com.codejam.codex.authzen.dtos.outputs.UserResponse;
import com.codejam.codex.authzen.endpoint.AuthEndpoint;
import com.codejam.codex.authzen.responses.AuthzenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(ApiEndpoint.AUTH)
public class AuthController {

    private final AuthEndpoint authEndpoint;

    public AuthController(AuthEndpoint authEndpoint) {
        this.authEndpoint = authEndpoint;
    }

    @PostMapping(ApiEndpoint.AUTH_REGISTER)
    public ResponseEntity<AuthzenResponse<UserResponse>> register(@RequestBody RegisterRequest req) {
        try {
            UserResponse u = authEndpoint.registerUser(req);
            return ResponseEntity.ok(AuthzenResponse.success(u, "User registered successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(AuthzenResponse.failure("Invalid registration data: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthzenResponse.failure("Registration error: " + e.getMessage()));
        }
    }

    @PostMapping(ApiEndpoint.AUTH_LOGIN)
    public ResponseEntity<AuthzenResponse<TokenResponse>> login(@RequestBody LoginRequest req) {
        try {
            TokenResponse t = authEndpoint.authenticateUser(req);
            return Optional.ofNullable(t)
                    .map(tok -> ResponseEntity.ok(AuthzenResponse.success(tok, "User logged in successfully")))
                    .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(AuthzenResponse.failure("Invalid credentials")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthzenResponse.failure("Login error: " + e.getMessage()));
        }
    }

    @PostMapping(ApiEndpoint.AUTH_OAUTH)
    public ResponseEntity<AuthzenResponse<TokenResponse>> oauthLogin(@RequestBody OAuthRequest req) {
        try {
            TokenResponse t = authEndpoint.authenticateOAuth(req);
            return Optional.ofNullable(t)
                    .map(tok -> ResponseEntity.ok(AuthzenResponse.success(tok, "User logged in via OAuth")))
                    .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(AuthzenResponse.failure("OAuth login failed")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthzenResponse.failure("OAuth error: " + e.getMessage()));
        }
    }

    @PostMapping(ApiEndpoint.AUTH_RESET_REQUEST)
    public ResponseEntity<AuthzenResponse<Object>> resetRequest(@RequestBody ResetRequest req) {
        try {
            boolean sent = authEndpoint.sendPasswordResetEmail(req);
            return sent
                ? ResponseEntity.ok(AuthzenResponse.success(null, "Password reset email sent"))
                : ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthzenResponse.failure("Failed to send reset email"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthzenResponse.failure("Reset request error: " + e.getMessage()));
        }
    }

    @PostMapping(ApiEndpoint.AUTH_RESET_PASSWORD)
    public ResponseEntity<AuthzenResponse<Object>> resetPassword(@RequestBody ResetPasswordRequest req) {
        try {
            boolean ok = authEndpoint.resetUserPassword(req);
            return ok
                ? ResponseEntity.ok(AuthzenResponse.success(null, "Password reset successfully"))
                : ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthzenResponse.failure("Password reset failed"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthzenResponse.failure("Reset error: " + e.getMessage()));
        }
    }

    @PostMapping(ApiEndpoint.AUTH_REFRESH)
    public ResponseEntity<AuthzenResponse<TokenResponse>> refresh(@RequestBody RefreshTokenRequest req) {
        try {
            TokenResponse t = authEndpoint.refreshToken(req.getRefreshToken());
            return ResponseEntity.ok(AuthzenResponse.success(t, "Token refreshed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthzenResponse.failure(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthzenResponse.failure("Refresh error: " + e.getMessage()));
        }
    }
}
