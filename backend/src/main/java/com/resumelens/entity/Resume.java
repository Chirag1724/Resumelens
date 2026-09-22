package com.resumelens.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resumes")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nullable: guest uploads have no associated user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String fileName;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String rawText;

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    private String targetRole;

    private Integer experienceYears;

    @PrePersist
    void prePersist() { this.uploadedAt = LocalDateTime.now(); }

    protected Resume() {}

    public Resume(User user, String fileName, String rawText, String targetRole, Integer experienceYears) {
        this.user = user;
        this.fileName = fileName;
        this.rawText = rawText;
        this.targetRole = targetRole;
        this.experienceYears = experienceYears;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getFileName() { return fileName; }
    public String getRawText() { return rawText; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public String getTargetRole() { return targetRole; }
    public Integer getExperienceYears() { return experienceYears; }
}