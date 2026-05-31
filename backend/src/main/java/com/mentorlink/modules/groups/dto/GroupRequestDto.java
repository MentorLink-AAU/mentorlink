package com.mentorlink.modules.groups.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GroupRequestDto {
    @NotBlank(message = "Group name is required")
    @Size(max = 100)
    private String name;
    private Long projectId;           // optional: use existing project
    private String projectTitle;      // optional: create new project if projectId null, or update existing project
    private String projectDescription;
    private String projectDomain;     // optional: domain when creating/updating project
    private String projectTechStack;  // optional: tech stack when creating/updating project
}
