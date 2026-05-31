package com.mentorlink.modules.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSummaryDto {
    private Long projectId;
    private String title;
    private String description;
    private String domain;
    private int progress;
    private MentorSummaryDto mentor;
    private Long groupId;
    private LocalDate lastMeetingDate;
    private Boolean lastMeetingVerified;
}
