package com.resumelens.dto.response;

public record RiskResponse(
        String title,
        String severity,
        String description,
        String fix,
        String code,
        Double points,
        String section
) {
    public RiskResponse(String title, String severity, String description, String fix) {
        this(title, severity, description, fix, null, null, null);
    }
}
