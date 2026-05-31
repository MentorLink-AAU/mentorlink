package com.mentorlink.common.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

/** API error payload: code, message, path, timestamp. */
@Data
@AllArgsConstructor
public class ApiError {
    private String code;       // e.g., VALIDATION_ERROR, INTERNAL_ERROR
    private String message;    // human-readable error
    private String path;       // request URI
    private Instant timestamp; // when it happened

    public ApiError(String code, String message, String path) {
        this.code = code;
        this.message = message;
        this.path = path;
        this.timestamp = Instant.now();
    }
}
