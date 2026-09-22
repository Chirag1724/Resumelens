package com.resumelens.dto.response;

import java.time.LocalDateTime;

public record HistoryItemResponse(
        Long matchResultId,
        Long resumeId,
        String resumeFileName,
        String jobTitle,
        Double overallScore,
        String statusLabel,
        LocalDateTime analyzedAt
) {}