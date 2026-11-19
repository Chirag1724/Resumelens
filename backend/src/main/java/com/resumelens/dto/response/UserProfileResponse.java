package com.resumelens.dto.response;

import java.time.LocalDateTime;

public record UserProfileResponse(
    String email,
    String name,
    String provider,
    LocalDateTime createdAt,
    long totalScans
) {}
