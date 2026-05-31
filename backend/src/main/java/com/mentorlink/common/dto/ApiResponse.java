package com.mentorlink.common.dto;

import com.mentorlink.common.exception.ApiError;
import lombok.AllArgsConstructor;
import lombok.Data;

/** Wrapper for API responses: success/data or error payload. */
@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private ApiError error;   // ✅ FIX: changed from ErrorResponse → ApiError

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> error(ApiError error) {
        return new ApiResponse<>(false, null, error);
    }
}
