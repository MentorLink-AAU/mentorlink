package com.mentorlink.modules.recommender.dto;

import com.mentorlink.modules.recommender.entity.RecommenderJobStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class RecommenderJobResponseDto {
    private String jobId;
    private RecommenderJobStatus status;
    private String errorMessage;
    private Instant startedAt;
    private Instant finishedAt;
}
