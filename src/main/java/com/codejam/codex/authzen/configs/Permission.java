package com.codejam.codex.authzen.configs;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    /**
     * Static factory method to easily create Permission instances
     */
    public static Permission of(String name, String description) {
        return Permission.builder()
                .name(name)
                .description(description)
                .build();
    }
}
