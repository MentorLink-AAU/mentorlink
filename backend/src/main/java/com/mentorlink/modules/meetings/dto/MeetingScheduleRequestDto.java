package com.mentorlink.modules.meetings.dto;

import com.mentorlink.modules.meetings.entity.MeetingScheduleRequest.ScheduleStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

/** DTO for a future meeting schedule request (proposed/counter/approved). */
@Data
@Builder
public class MeetingScheduleRequestDto {
    private Long id;
    private Long projectId;
    private String projectTitle;
    private Long proposedById;
    private String proposedByName;
    private LocalDate proposedDate;
    private String notes;
    private ScheduleStatus status;
    private LocalDate facultyCounterDate;
    private String facultyCounterNotes;
    private LocalDate agreedDate;
    private Instant createdAt;
}
