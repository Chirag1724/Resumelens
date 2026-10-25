package com.resumelens.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AnalysisRequest(
        @NotNull UUID resumeId,
        Long jobDescriptionId,        // provide this OR rawJobDescription
        String rawJobDescription,
        String jobTitle               // optional, used in prompt generation
) {}