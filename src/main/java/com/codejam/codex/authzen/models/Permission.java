package com.codejam.codex.authzen.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
// models/Permission.java
public class Permission {
    private String name;
    private String description;

    public static Permission of(String name, String description) {
        Permission p = new Permission();
        p.name = name;
        p.description = description;
        return p;
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
}

