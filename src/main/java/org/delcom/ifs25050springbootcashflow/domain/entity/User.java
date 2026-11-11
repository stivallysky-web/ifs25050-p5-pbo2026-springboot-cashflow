package org.delcom.ifs25050springbootcashflow.domain.entity;

import java.time.LocalDateTime;

public class User {
    private Long id;
    private String name;
    private String email;
    private String passwordHash;
    private Role role;
    private String photoPath;
    private LocalDateTime createdAt;

    public User(Long id, String name, String email, String passwordHash,
                Role role, String photoPath, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.photoPath = photoPath;
        this.createdAt = createdAt;
    }

    public User(String name, String email, String passwordHash, Role role) {
        this(null, name, email, passwordHash, role, null, LocalDateTime.now());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public boolean isAdmin() { return role == Role.ADMIN; }
}