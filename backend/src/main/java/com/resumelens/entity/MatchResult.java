package com.resumelens.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "match_results")
public class MatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_description_id", nullable = false)
    private JobDescription jobDescription;

    @Column(nullable = false)
    private Double overallScore;

    private Double atsScore;
    private Double skillsScore;
    private Double impactScore;
    private Double projectScore;
    private Double writingScore;
    private Double professionalismScore;

    /** Derived from overallScore per SCORING_RULES.md thresholds */
    private String statusLabel;

    /** Keyword match ratio (0.0–1.0) stored for the history view */
    private Double keywordMatchPercent;

    @Column(columnDefinition = "TEXT")
    private String improvementPrompt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime analyzedAt;

    @PrePersist
    void prePersist() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
        if (this.analyzedAt == null) {
            this.analyzedAt = LocalDateTime.now();
        }
    }

    protected MatchResult() {}

    public MatchResult(Resume resume, JobDescription jobDescription) {
        this.resume = resume;
        this.jobDescription = jobDescription;
    }

    // Setters used only by ATSScoreEngine during assembly
    public void setOverallScore(Double v) { this.overallScore = v; }
    public void setAtsScore(Double v) { this.atsScore = v; }
    public void setSkillsScore(Double v) { this.skillsScore = v; }
    public void setImpactScore(Double v) { this.impactScore = v; }
    public void setProjectScore(Double v) { this.projectScore = v; }
    public void setWritingScore(Double v) { this.writingScore = v; }
    public void setProfessionalismScore(Double v) { this.professionalismScore = v; }
    public void setStatusLabel(String v) { this.statusLabel = v; }
    public void setKeywordMatchPercent(Double v) { this.keywordMatchPercent = v; }
    public void setImprovementPrompt(String v) { this.improvementPrompt = v; }

    public Long getId() { return id; }
    public UUID getPublicId() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
        return publicId;
    }
    public Resume getResume() { return resume; }
    public JobDescription getJobDescription() { return jobDescription; }
    public Double getOverallScore() { return overallScore; }
    public Double getAtsScore() { return atsScore; }
    public Double getSkillsScore() { return skillsScore; }
    public Double getImpactScore() { return impactScore; }
    public Double getProjectScore() { return projectScore; }
    public Double getWritingScore() { return writingScore; }
    public Double getProfessionalismScore() { return professionalismScore; }
    public String getStatusLabel() { return statusLabel; }
    public Double getKeywordMatchPercent() { return keywordMatchPercent; }
    public String getImprovementPrompt() { return improvementPrompt; }
    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
}