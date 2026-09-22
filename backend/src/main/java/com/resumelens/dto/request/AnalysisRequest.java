package com.resumelens.dto.request;

import jakarta.validation.constraints.NotNull;

public record AnalysisRequest(
        @NotNull Long resumeId,
        Long jobDescriptionId,        // provide this OR rawJobDescription
        String rawJobDescription,
        String jobTitle               // optional, used in prompt generation
) {}