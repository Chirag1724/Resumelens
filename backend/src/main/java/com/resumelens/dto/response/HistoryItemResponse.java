package com.resumelens.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record HistoryItemResponse(
        UUID id,
        UUID resumeId,
        String resumeFileName,
        String jobTitle,
        Double overallScore,
        String statusLabel,
        LocalDateTime analyzedAt
) {}