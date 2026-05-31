package com.mentorlink.modules.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StudentDashboardDto {
    private StudentProfileDto profile;
    private ProjectSummaryDto assignedProject;
    private GroupSummaryDto group;
    private List<ProjectSummaryDto> availableProjects;
    private List<SubmissionSummaryDto> mySubmissions;
    private long unreadNotificationCount;
}
