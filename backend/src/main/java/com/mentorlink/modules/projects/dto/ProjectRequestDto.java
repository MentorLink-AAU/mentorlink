package com.mentorlink.modules.projects.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProjectRequestDto {
    @NotBlank(message = "Project title is required")
    @Size(max = 255)
    private String title;
    @Size(max = 2000)
    private String description;
    @Size(max = 100)
    private String domain;
    @Size(max = 255)
    private String techStack;
    private Long groupId; // ignored - a group is auto-created when project is created
}
