package com.mentorlink.modules.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponseDto {
    private Long id;
    private String title;
    private String description;
    private String domain;
    private String techStack;
    private int progress;
    private Long groupId;
    private String joinToken;       // for other students to join the group (max 3 members)
    private String mentorJoinToken; // for faculty to join as mentor
    private Boolean hasMentor;      // true if mentor is assigned (for meeting log visibility)
}
