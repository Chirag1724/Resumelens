package com.resumelens.dto.response;

public record RiskResponse(
        String title,
        String severity,
        String description,
        String fix
) {}
