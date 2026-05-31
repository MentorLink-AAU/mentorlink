package com.mentorlink.modules.submissions.dto;

import com.mentorlink.modules.submissions.entity.Submission;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class SubmissionResponseDto {
    private Long id;
    private Long projectId;
    private String projectTitle;
    private Long groupId;
    private String category;
    private String originalFilename;
    private Long submittedById;
    private String submittedByName;
    private Instant submittedAt;

    public static SubmissionResponseDto from(Submission s) {
        return SubmissionResponseDto.builder()
                .id(s.getId())
                .projectId(s.getProject().getId())
                .projectTitle(s.getProject().getTitle())
                .groupId(s.getProject().getGroup() != null ? s.getProject().getGroup().getId() : null)
                .category(s.getCategory())
                .originalFilename(s.getOriginalFilename())
                .submittedById(s.getSubmittedBy() != null ? s.getSubmittedBy().getId() : null)
                .submittedByName(s.getSubmittedBy() != null ? s.getSubmittedBy().getFullName() : null)
                .submittedAt(s.getCreatedAt())
                .build();
    }
}
