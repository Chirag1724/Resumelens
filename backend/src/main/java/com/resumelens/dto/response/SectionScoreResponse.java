package com.resumelens.dto.response;

public record SectionScoreResponse(
        String sectionName,
        Double score,
        String status,
        String feedback,
        String matchedKeywords,
        String missingKeywords
) {}