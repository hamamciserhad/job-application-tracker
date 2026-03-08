package com.jobtracker.dto.response;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        T data,
        String timestamp
) {
    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data, LocalDateTime.now().toString());
    }
}
