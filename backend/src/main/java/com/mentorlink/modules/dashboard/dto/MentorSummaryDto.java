package com.mentorlink.modules.dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MentorSummaryDto {
    private Long facultyId;
    private String name;
    private String email;
    private String department;
    private String expertise;
}
