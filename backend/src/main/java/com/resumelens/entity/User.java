package com.resumelens.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private String name;

    @Column(nullable = false)
    private String provider = "LOCAL";

    private String providerId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (this.provider == null) {
            this.provider = "LOCAL";
        }
        this.createdAt = LocalDateTime.now();
    }

    // JPA requires no-arg constructor
    protected User() {}

    public User(String email, String passwordHash, String name) {
        this(email, passwordHash, name, "LOCAL", null);
    }

    public User(String email, String passwordHash, String name, String provider, String providerId) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.provider = (provider != null && !provider.isBlank()) ? provider : "LOCAL";
        this.providerId = providerId;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getName() { return name; }
    public String getProvider() { return provider; }
    public String getProviderId() { return providerId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}