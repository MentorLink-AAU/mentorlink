package com.mentorlink.common.exception;

import lombok.*;

import java.time.Instant;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ErrorResponse {
    private String code;
    private String message;
    private String path;
    private Instant timestamp;
}
