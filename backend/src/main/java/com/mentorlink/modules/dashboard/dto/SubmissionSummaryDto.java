package com.mentorlink.modules.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class SubmissionSummaryDto {
    private Long id;
    private Long projectId;
    private String category;
    private String originalFilename;
    private Instant submittedAt;
}
