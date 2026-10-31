package com.resumelens.dto.response;

public record SuggestionResponse(
        String priority,    // HIGH | MEDIUM | LOW
        String title,
        String description,
        String example,
        String code
) {
    public SuggestionResponse(String priority, String title, String description, String example) {
        this(priority, title, description, example, null);
    }
}
