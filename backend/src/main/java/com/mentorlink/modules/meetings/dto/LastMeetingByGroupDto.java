package com.mentorlink.modules.meetings.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** DTO for admin view: last meeting date and details per group. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LastMeetingByGroupDto {
    private Long groupId;
    private String groupName;
    private Long projectId;
    private String projectTitle;
    private LocalDate lastMeetingDate;
    private String lastMeetingDetails;
    private boolean verified;
}
