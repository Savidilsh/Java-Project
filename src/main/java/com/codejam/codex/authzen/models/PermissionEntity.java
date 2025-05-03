package com.codejam.codex.authzen.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@ToString
@Table(name = "permissions")
public class PermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    @OneToMany(
        mappedBy = "permission",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Set<RolePermission> rolePermissions = new HashSet<>();

    public static PermissionEntity of(String name, String description) {
        return PermissionEntity.builder()
                .name(name)
                .description(description)
                .build();
    }
}
