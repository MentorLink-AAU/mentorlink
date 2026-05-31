package com.mentorlink.modules.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FacultyDashboardDto {
    private FacultyProfileDto profile;
    private List<ProjectSummaryDto> supervisedProjects;
    private List<GroupSummaryDto> assignedGroups;
    private long unreadNotificationCount;
}
