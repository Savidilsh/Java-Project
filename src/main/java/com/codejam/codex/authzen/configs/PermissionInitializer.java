package com.codejam.codex.authzen.configs;

import com.codejam.codex.authzen.models.*;
import com.codejam.codex.authzen.repositories.PermissionRepository;
import com.codejam.codex.authzen.repositories.RolePermissionRepository;
import com.codejam.codex.authzen.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;

/**
 * Initializes permissions and assigns them to roles at application startup.
 */
@Configuration
@RequiredArgsConstructor
public class PermissionInitializer {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    private static final Logger logger = LoggerFactory.getLogger(PermissionInitializer.class);

    public CommandLineRunner initializePermissions() {
        return args -> {
            List<Permission> allPermissions = getDefaultPermissions();

            // Create or update permissions in the DB
            for (Permission perm : allPermissions) {
                PermissionEntity saved = permissionRepository.save(
                    Permission.of(perm.getName(), perm.getDescription())
                );

                Role adminRole = roleRepository.findByNameWithPermissions("ROLE_ADMIN")
                        .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found"));

                boolean already = adminRole.getRolePermissions().stream()
                        .anyMatch(rp -> rp.getPermission().getName().equals(perm.getName()));

                if (!already) {
                    RolePermission rp = RolePermission.builder()
                            .role(adminRole)
                            .permission(saved)
                            .build();
                    rolePermissionRepository.save(rp);
                    logger.info(format("Assigned '%s' to ROLE_ADMIN", perm.getName()));
                }
            }

            logger.info("Permission initialization complete.");

            // Assign selected permissions to ROLE_USER
            List<Permission> userPermissions = getUserPermissions();

            Role userRole = roleRepository.findByNameWithPermissions("ROLE_USER")
                    .orElseThrow(() -> new IllegalStateException("ROLE_USER not found"));

            for (Permission perm : userPermissions) {
                boolean already = userRole.getRolePermissions().stream()
                        .anyMatch(rp -> rp.getPermission().getName().equals(perm.getName()));

                if (!already) {
                    PermissionEntity saved = permissionRepository.save(
                        PermissionEntity.of(perm.getName(), perm.getDescription())
                    );

                    RolePermission rp = RolePermission.builder()
                            .role(userRole)
                            .permission(saved)
                            .build();
                    rolePermissionRepository.save(rp);
                    logger.info(format("Assigned '%s' to ROLE_USER", perm.getName()));
                }
            }
        };
    }

    private List<Permission> getDefaultPermissions() {
        return Arrays.asList(
                Permission.of("CREATE_USER", "Create new user"),
                Permission.of("UPDATE_USER", "Update user information"),
                Permission.of("DELETE_USER", "Delete a user"),
                Permission.of("VIEW_USER", "View user details"),
                Permission.of("CREATE_ROLE", "Create new role"),
                Permission.of("UPDATE_ROLE", "Update role"),
                Permission.of("DELETE_ROLE", "Delete a role"),
                Permission.of("ASSIGN_ROLE", "Assign role to user"),
                Permission.of("VIEW_ROLES", "View role list"),
                Permission.of("CREATE_PERMISSION", "Create new permission"),
                Permission.of("UPDATE_PERMISSION", "Update permission"),
                Permission.of("DELETE_PERMISSION", "Delete permission"),
                Permission.of("ASSIGN_PERMISSION", "Assign permission to roles"),
                Permission.of("VIEW_PERMISSIONS", "View permission details"),
                Permission.of("VIEW_AUDIT_LOG", "View audit logs"),
                Permission.of("EXPORT_AUDIT_LOG", "Export audit logs"),
                Permission.of("VIEW_DASHBOARD", "Access dashboard"),
                Permission.of("VIEW_STATS", "View system statistics"),
                Permission.of("CREATE_CONTENT", "Create content")
        );
    }

    private List<Permission> getUserPermissions() {
        return Arrays.asList(
                Permission.of("VIEW_USER", "View user details"),
                Permission.of("UPDATE_USER", "Update user information"),
                Permission.of("VIEW_STATS", "View system statistics")
        );
    }
}
