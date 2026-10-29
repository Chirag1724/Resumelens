package com.resumelens.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AnalysisResponse(
        UUID id,
        String fileName,
        LocalDateTime analyzedAt,
        String targetRole,
        Integer experienceYears,
        Double overallScore,
        String statusLabel,
        Double uncappedScore,
        Boolean scoreCapped,
        Double atsScore,
        Double skillsScore,
        Double impactScore,
        Double projectScore,
        Double writingScore,
        Double professionalismScore,
        Double keywordMatchPercent,
        List<SectionScoreResponse> sectionScores,
        List<SuggestionResponse> suggestions,
        List<RiskResponse> risks,
        String improvementPrompt
) {
    public AnalysisResponse(
            UUID id,
            String fileName,
            LocalDateTime analyzedAt,
            String targetRole,
            Integer experienceYears,
            Double overallScore,
            String statusLabel,
            Double atsScore,
            Double skillsScore,
            Double impactScore,
            Double projectScore,
            Double writingScore,
            Double professionalismScore,
            Double keywordMatchPercent,
            List<SectionScoreResponse> sectionScores,
            List<SuggestionResponse> suggestions,
            List<RiskResponse> risks,
            String improvementPrompt
    ) {
        this(id, fileName, analyzedAt, targetRole, experienceYears, overallScore, statusLabel,
             overallScore, false, atsScore, skillsScore, impactScore, projectScore, writingScore,
             professionalismScore, keywordMatchPercent, sectionScores, suggestions, risks, improvementPrompt);
    }

    public AnalysisResponse(
            UUID id,
            String fileName,
            LocalDateTime analyzedAt,
            String targetRole,
            Integer experienceYears,
            Double overallScore,
            String statusLabel,
            Double atsScore,
            Double skillsScore,
            Double impactScore,
            Double projectScore,
            Double writingScore,
            Double professionalismScore,
            Double keywordMatchPercent,
            List<SectionScoreResponse> sectionScores,
            List<SuggestionResponse> suggestions,
            String improvementPrompt
    ) {
        this(id, fileName, analyzedAt, targetRole, experienceYears, overallScore, statusLabel,
             overallScore, false, atsScore, skillsScore, impactScore, projectScore, writingScore,
             professionalismScore, keywordMatchPercent, sectionScores, suggestions, List.of(), improvementPrompt);
    }

    public AnalysisResponse(
            UUID id,
            Double overallScore,
            String statusLabel,
            Double atsScore,
            Double skillsScore,
            Double impactScore,
            Double projectScore,
            Double writingScore,
            Double professionalismScore,
            Double keywordMatchPercent,
            List<SectionScoreResponse> sectionScores,
            List<SuggestionResponse> suggestions,
            List<RiskResponse> risks,
            String improvementPrompt
    ) {
        this(id, null, null, null, null, overallScore, statusLabel,
             overallScore, false, atsScore, skillsScore, impactScore, projectScore, writingScore,
             professionalismScore, keywordMatchPercent, sectionScores, suggestions, risks, improvementPrompt);
    }

    public AnalysisResponse(
            UUID id,
            Double overallScore,
            String statusLabel,
            Double atsScore,
            Double skillsScore,
            Double impactScore,
            Double projectScore,
            Double writingScore,
            Double professionalismScore,
            Double keywordMatchPercent,
            List<SectionScoreResponse> sectionScores,
            List<SuggestionResponse> suggestions,
            String improvementPrompt
    ) {
        this(id, null, null, null, null, overallScore, statusLabel,
             overallScore, false, atsScore, skillsScore, impactScore, projectScore, writingScore,
             professionalismScore, keywordMatchPercent, sectionScores, suggestions, List.of(), improvementPrompt);
    }
}