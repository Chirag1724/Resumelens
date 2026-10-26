package com.resumelens.dto.response;

import java.util.List;
import java.util.UUID;

public record AnalysisResponse(
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
        this(id, overallScore, statusLabel, atsScore, skillsScore, impactScore,
             projectScore, writingScore, professionalismScore, keywordMatchPercent,
             sectionScores, suggestions, List.of(), improvementPrompt);
    }
}