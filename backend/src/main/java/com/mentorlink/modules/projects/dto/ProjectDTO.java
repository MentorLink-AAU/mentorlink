package com.mentorlink.modules.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {
    private Long id;
    private String title;
    private String description;
    private String status;      // e.g. NOT_STARTED, IN_PROGRESS, COMPLETED
    private Integer progress;   // e.g. 0–100 (%)
    private String domain;
    private String techStack;
    private String joinToken;

    // ✅ Minimal constructor
    public ProjectDTO(Long id, String title, String description, String status, Integer progress, String joinToken) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.progress = progress;
        this.joinToken = joinToken;
    }
}
