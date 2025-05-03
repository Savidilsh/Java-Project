package com.codejam.codex.authzen.configs;

import com.codejam.codex.authzen.models.Permission;
import com.codejam.codex.authzen.models.Role;
import com.codejam.codex.authzen.models.RolePermission;
import com.codejam.codex.authzen.repositories.PermissionRepository;
import com.codejam.codex.authzen.repositories.RolePermissionRepository;
import com.codejam.codex.authzen.repositories.RoleRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Configuration
public class PermissionInitializer {

    private static final Logger logger = LoggerFactory.getLogger(PermissionInitializer.class);

    @Bean
    @Transactional
    public CommandLineRunner initializePermissions(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            RolePermissionRepository rolePermissionRepository) {

        return args -> {
            try {
                logger.info("Initializing default and user permissions...");

                List<Permission> defaultPermissions = getDefaultPermissions();
                List<Permission> userPermissions = getUserPermissions();

                // Step 1: Ensure all default permissions exist
                for (Permission permission : defaultPermissions) {
                    Optional<Permission> existing = permissionRepository.findByName(permission.getName());
                    if (existing.isEmpty()) {
                        permissionRepository.save(permission);
                        logger.info("Created permission: {}", permission.getName());
                    }
                }

                // Step 2: Assign all default permissions to ROLE_ADMIN
                Role adminRole = roleRepository.findByNameWithPermissions("ROLE_ADMIN")
                        .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found in DB"));

                for (Permission permission : defaultPermissions) {
                    if (adminRole.getRolePermissions().stream().noneMatch(
                            rp -> rp.getPermission().getName().equals(permission.getName()))) {

                        Permission persisted = permissionRepository.findByName(permission.getName())
                                .orElseThrow(() -> new IllegalStateException("Permission not found: " + permission.getName()));

                        RolePermission link = RolePermission.builder()
                                .role(adminRole)
                                .permission(persisted)
                                .build();

                        rolePermissionRepository.save(link);
                        logger.info("Assigned permission '{}' to ROLE_ADMIN", permission.getName());
                    }
                }

                // Step 3: Assign user-specific permissions to ROLE_USER
                Role userRole = roleRepository.findByNameWithPermissions("ROLE_USER")
                        .orElseThrow(() -> new IllegalStateException("ROLE_USER not found in DB"));

                for (Permission permission : userPermissions) {
                    if (userRole.getRolePermissions().stream().noneMatch(
                            rp -> rp.getPermission().getName().equals(permission.getName()))) {

                        Permission persisted = permissionRepository.findByName(permission.getName())
                                .orElseThrow(() -> new IllegalStateException("Permission not found: " + permission.getName()));

                        RolePermission link = RolePermission.builder()
                                .role(userRole)
                                .permission(persisted)
                                .build();

                        rolePermissionRepository.save(link);
                        logger.info("Assigned permission '{}' to ROLE_USER", permission.getName());
                    }
                }

                logger.info("Permission initialization completed successfully.");

            } catch (Exception e) {
                logger.error("Permission initialization failed: {}", e.getMessage(), e);
                throw e; // rethrow to ensure visibility on startup
            }
        };
    }

    private List<Permission> getDefaultPermissions() {
        return Arrays.asList(
                Permission.of("CREATE_USER", "Create new user"),
                Permission.of("UPDATE_USER", "Update user information"),
                Permission.of("DELETE_USER", "Delete a user"),
                Permission.of("USER_LOGOUT", "Logout user"),
                Permission.of("ACTIVATE_USER", "Activate user account"),
                Permission.of("DEACTIVATE_USER", "Deactivate user account"),
                Permission.of("VIEW_USER", "View user details"),

                Permission.of("CREATE_ROLE", "Create new role"),
                Permission.of("UPDATE_ROLE", "Update role"),
                Permission.of("DELETE_ROLE", "Delete a role"),
                Permission.of("ASSIGN_ROLE", "Assign role to user"),
                Permission.of("VIEW_ROLE", "View role details"),

                Permission.of("CREATE_PERMISSION", "Create new permission"),
                Permission.of("UPDATE_PERMISSION", "Update permission"),
                Permission.of("DELETE_PERMISSION", "Delete permission"),
                Permission.of("ASSIGN_PERMISSION", "Assign permission to role"),
                Permission.of("VIEW_PERMISSION", "View permission details"),

                Permission.of("VIEW_AUDIT_LOG", "View audit logs"),
                Permission.of("EXPORT_AUDIT_LOG", "Export audit logs"),

                Permission.of("VIEW_DASHBOARD", "Access dashboard"),
                Permission.of("VIEW_STATS", "View system statistics"),

                Permission.of("CREATE_CONTENT", "Create content"),
                Permission.of("UPDATE_CONTENT", "Update content"),
                Permission.of("DELETE_CONTENT", "Delete content"),
                Permission.of("VIEW_CONTENT", "View content"),

                Permission.of("VIEW_ADMIN_PANEL", "Access admin panel"),
                Permission.of("CONFIGURE_SYSTEM", "Configure system settings"),
                Permission.of("VIEW_SYSTEM_STATUS", "View system status")
        );
    }

    private List<Permission> getUserPermissions() {
        return Arrays.asList(
                Permission.of("VIEW_USER", "View user details"),
                Permission.of("UPDATE_USER", "Update user information"),
                Permission.of("VIEW_DASHBOARD", "Access dashboard"),
                Permission.of("VIEW_STATS", "View system statistics"),
                Permission.of("VIEW_CONTENT", "View content"),
                Permission.of("USER_LOGOUT", "Logout user")
        );
    }
}
