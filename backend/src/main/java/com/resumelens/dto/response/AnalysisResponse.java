package com.resumelens.dto.response;

import java.util.List;

public record AnalysisResponse(
        Long matchResultId,
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
) {}