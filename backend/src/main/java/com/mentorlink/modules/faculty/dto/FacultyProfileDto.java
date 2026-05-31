package com.mentorlink.modules.faculty.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FacultyProfileDto {
    private Long id;
    private String name;
    private String email;
    private String department;
    private String expertise;
    private Integer currentLoad;
    private Integer maxGroups;
}
