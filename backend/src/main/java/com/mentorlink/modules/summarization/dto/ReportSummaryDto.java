package com.mentorlink.modules.summarization.dto;

import com.mentorlink.modules.summarization.entity.ReportSummary;
import com.mentorlink.modules.summarization.entity.ReportSummaryStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ReportSummaryDto {
    private Long id;
    private Long projectId;
    private Long studentId;
    private String reportFilePath;
    private String originalFilename;
    private String generatedSummary;
    private ReportSummaryStatus status;
    private String errorMessage;
    private Instant createdAt;

    public static ReportSummaryDto from(ReportSummary s) {
        return ReportSummaryDto.builder()
                .id(s.getId())
                .projectId(s.getProject().getId())
                .studentId(s.getSubmittedBy() != null ? s.getSubmittedBy().getId() : null)
                .reportFilePath(s.getReportFilePath())
                .originalFilename(s.getOriginalFilename())
                .generatedSummary(s.getGeneratedSummary())
                .status(s.getStatus() != null ? s.getStatus() : (s.getGeneratedSummary() != null ? ReportSummaryStatus.DONE : ReportSummaryStatus.PENDING))
                .errorMessage(s.getErrorMessage())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
