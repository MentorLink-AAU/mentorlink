package com.mentorlink.common.exception;

import com.mentorlink.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ✅ Handle ApiException (e.g. 404, 400)
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleApiException(ApiException ex, HttpServletRequest request) {
        ApiError error = new ApiError(ex.getCode(), ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(ex.getStatus()).body(ApiResponse.error(error));
    }

    // ✅ Handle IllegalStateException (e.g. deadline not passed)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalState(
            IllegalStateException ex, HttpServletRequest request) {
        ApiError error = new ApiError("INVALID_STATE", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.badRequest().body(ApiResponse.error(error));
    }

    // ✅ Handle validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        ApiError error = new ApiError("VALIDATION_ERROR", message, request.getRequestURI());
        return ResponseEntity.badRequest().body(ApiResponse.error(error));
    }

    // ✅ Handle generic exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex, HttpServletRequest request) {

        ApiError error = new ApiError("INTERNAL_ERROR", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(error));
    }
}
