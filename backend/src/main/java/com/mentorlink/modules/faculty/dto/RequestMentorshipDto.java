package com.mentorlink.modules.faculty.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestMentorshipDto {
    @NotNull(message = "facultyId is required")
    private Long facultyId;
    private String projectTopic;
    private String projectDescription;
}
