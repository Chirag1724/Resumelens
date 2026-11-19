package com.resumelens.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OAuthRequest(
    @NotBlank(message = "OAuth provider is required")
    String provider,
    String token,
    String email,
    String name,
    String providerId
) {}
