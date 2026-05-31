package com.mentorlink.modules.meetings.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/** Request body when a student logs a past meeting. */
@Data
public class CreateMeetingRequest {
    @NotNull(message = "Meeting date is required")
    private LocalDate meetingDate;

    private String details;

    /** Explicit getters (Lombok may not generate in some setups). */
    public LocalDate getMeetingDate() { return meetingDate; }
    public String getDetails() { return details; }
}
