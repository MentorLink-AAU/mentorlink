package com.mentorlink.modules.faculty.dto;

import lombok.Data;

@Data
public class FacultyRequest {
    private Long userId;       // link to existing User
    private String name;
    private String email;
    private String department;
    private String expertise;
    private Integer maxGroups; // optional, default 3
}
