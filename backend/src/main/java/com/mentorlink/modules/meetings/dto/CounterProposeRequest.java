package com.mentorlink.modules.meetings.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/** Request body when faculty suggests an alternative date ("I'm free on X"). */
@Data
public class CounterProposeRequest {
    @NotNull(message = "Alternative date is required")
    private LocalDate facultyCounterDate;

    private String facultyCounterNotes;
}
