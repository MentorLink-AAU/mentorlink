package com.mentorlink.modules.faculty.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FacultyListDto {
    private Long id;
    private String name;
    private String email;
    private String department;
    private String expertise;
    private int currentLoad;
    private int maxGroups;
    private boolean available; // currentLoad < maxGroups
}
