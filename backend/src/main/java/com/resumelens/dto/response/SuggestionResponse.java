package com.resumelens.dto.response;

public record SuggestionResponse(
        String priority,    // HIGH | MEDIUM | LOW
        String title,
        String description,
        String example
) {}