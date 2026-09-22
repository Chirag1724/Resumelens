package com.resumelens.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "section_scores")
public class SectionScore {

    // Section name constants - matches SCORING_RULES.md dimension names
    public static final String ATS_COMPATIBILITY     = "ATS_COMPATIBILITY";
    public static final String SKILLS_VALIDATION     = "SKILLS_VALIDATION";
    public static final String IMPACT_ACHIEVEMENTS   = "IMPACT_ACHIEVEMENTS";
    public static final String PROJECT_QUALITY       = "PROJECT_QUALITY";
    public static final String WRITING_QUALITY       = "WRITING_QUALITY";
    public static final String PROFESSIONALISM       = "PROFESSIONALISM";

    // Status constants
    public static final String EXCELLENT = "EXCELLENT";  // >= 85
    public static final String GOOD      = "GOOD";       // 70-84
    public static final String AVERAGE   = "AVERAGE";    // 40-69
    public static final String POOR      = "POOR";       // < 40

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_result_id", nullable = false)
    private MatchResult matchResult;

    @Column(nullable = false)
    private String sectionName;

    @Column(nullable = false)
    private Double score;

    @Column(nullable = false)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(columnDefinition = "TEXT")
    private String matchedKeywords;   // JSON array stored as string

    @Column(columnDefinition = "TEXT")
    private String missingKeywords;   // JSON array stored as string

    protected SectionScore() {}

    public SectionScore(MatchResult matchResult, String sectionName, Double score,
                        String feedback, String matchedKeywords, String missingKeywords) {
        this.matchResult = matchResult;
        this.sectionName = sectionName;
        this.score = score;
        this.status = deriveStatus(score);
        this.feedback = feedback;
        this.matchedKeywords = matchedKeywords;
        this.missingKeywords = missingKeywords;
    }

    private static String deriveStatus(double score) {
        if (score >= 85) return EXCELLENT;
        if (score >= 70) return GOOD;
        if (score >= 40) return AVERAGE;
        return POOR;
    }

    public Long getId() { return id; }
    public MatchResult getMatchResult() { return matchResult; }
    public String getSectionName() { return sectionName; }
    public Double getScore() { return score; }
    public String getStatus() { return status; }
    public String getFeedback() { return feedback; }
    public String getMatchedKeywords() { return matchedKeywords; }
    public String getMissingKeywords() { return missingKeywords; }
}