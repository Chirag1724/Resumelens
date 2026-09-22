package com.resumelens.dto.response;

/**
 * Generic API envelope used by every controller response.
 * Entities are never returned directly - always wrapped here.
 */
public record ApiResponse<T>(boolean success, String message, T data) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "OK", data);
    }

    public static <T> ApiResponse<T> failure(String message) {
        return new ApiResponse<>(false, message, null);
    }
}