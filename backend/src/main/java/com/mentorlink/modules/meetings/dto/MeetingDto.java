package com.mentorlink.modules.meetings.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

/** DTO for a meeting log entry (past meeting, student-logged, faculty-verified). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingDto {
    private Long id;
    private Long projectId;
    private LocalDate meetingDate;
    private String details;
    private boolean verified;
    private Instant verifiedAt;
    private Long loggedById;
    private String loggedByName;
    private Instant createdAt;
}
