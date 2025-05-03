package com.codejam.codex.authzen.configs;

import com.codejam.codex.authzen.models.Role;
import com.codejam.codex.authzen.models.User;
import com.codejam.codex.authzen.models.UserRole;
import com.codejam.codex.authzen.repositories.RoleRepository;
import com.codejam.codex.authzen.repositories.UserRepository;
import com.codejam.codex.authzen.repositories.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Timestamp;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class RoleInitializer {

    private static final Logger logger = LoggerFactory.getLogger(RoleInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Bean
    CommandLineRunner initializeRolesAndAdmin() {
        return args -> {
            logger.info("Initializing roles and admin account...");

            try {
                Role userRole = createRoleIfNotExists("ROLE_USER", "Default user role");
                Role adminRole = createRoleIfNotExists("ROLE_ADMIN", "Administrator with full access");

                createAdminUserIfNotExists(adminRole);

                logger.info("Role and admin initialization complete.");
            } catch (Exception e) {
                logger.error("Initialization failed: {}", e.getMessage(), e);
                throw e;
            }
        };
    }

    private Role createRoleIfNotExists(String name, String description) {
        return roleRepository.findByName(name).stream().findFirst().orElseGet(() -> {
            logger.info("Creating role: {}", name);
            Role newRole = Role.builder()
                    .name(name)
                    .description(description)
                    .build();
            return roleRepository.save(newRole);
        });
    }

    private void createAdminUserIfNotExists(Role adminRole) {
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            logger.info("Creating default admin user: {}", adminEmail);

            User admin = User.builder()
                    .username(adminUsername)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .isActive(true)
                    .isLocked(false)
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .build();

            User savedAdmin = userRepository.save(admin);

            UserRole userRole = UserRole.builder()
                    .user(savedAdmin)
                    .role(adminRole)
                    .build();

            userRoleRepository.save(userRole);

            logger.info("Admin user created and assigned ROLE_ADMIN.");
        } else {
            logger.info("Admin user already exists: {}", adminEmail);
        }
    }
}
