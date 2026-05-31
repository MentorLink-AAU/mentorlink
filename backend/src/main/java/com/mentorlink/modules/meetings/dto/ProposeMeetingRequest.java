package com.mentorlink.modules.meetings.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/** Request body when student proposes a future meeting date. */
@Data
public class ProposeMeetingRequest {
    @NotNull(message = "Meeting date is required")
    private LocalDate proposedDate;

    private String notes;
}
