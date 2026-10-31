package com.resumelens.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "resumes")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId;

    // Nullable: guest uploads have no associated user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String fileName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawText;

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    private String targetRole;

    private Integer experienceYears;

    @Column(nullable = false)
    private boolean hasGraphicsRisk = false;

    @Column(nullable = false)
    private boolean hasFormattingRisk = false;

    @PrePersist
    void prePersist() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
        if (this.uploadedAt == null) {
            this.uploadedAt = LocalDateTime.now();
        }
    }

    protected Resume() {}

    public Resume(User user, String fileName, String rawText, String targetRole, Integer experienceYears) {
        this(user, fileName, rawText, targetRole, experienceYears, false, false);
    }

    public Resume(User user, String fileName, String rawText, String targetRole, Integer experienceYears, boolean hasGraphicsRisk) {
        this(user, fileName, rawText, targetRole, experienceYears, hasGraphicsRisk, false);
    }

    public Resume(User user, String fileName, String rawText, String targetRole, Integer experienceYears, boolean hasGraphicsRisk, boolean hasFormattingRisk) {
        this.user = user;
        this.fileName = fileName;
        this.rawText = rawText;
        this.targetRole = targetRole;
        this.experienceYears = experienceYears;
        this.hasGraphicsRisk = hasGraphicsRisk;
        this.hasFormattingRisk = hasFormattingRisk;
    }

    public Long getId() { return id; }
    public UUID getPublicId() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
        return publicId;
    }
    public User getUser() { return user; }
    public String getFileName() { return fileName; }
    public String getRawText() { return rawText; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public String getTargetRole() { return targetRole; }
    public Integer getExperienceYears() { return experienceYears; }
    public boolean hasGraphicsRisk() { return hasGraphicsRisk; }
    public boolean isHasGraphicsRisk() { return hasGraphicsRisk; }
    public void setHasGraphicsRisk(boolean hasGraphicsRisk) { this.hasGraphicsRisk = hasGraphicsRisk; }
    public boolean hasFormattingRisk() { return hasFormattingRisk; }
    public boolean isHasFormattingRisk() { return hasFormattingRisk; }
    public void setHasFormattingRisk(boolean hasFormattingRisk) { this.hasFormattingRisk = hasFormattingRisk; }
}