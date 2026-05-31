package com.mentorlink.modules.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FacultyProfileDto {
    private Long facultyId;
    private Long userId;
    private String fullName;
    private String email;
    private String department;
    private String expertise;
    private String phoneNumber;
    private String bio;
    private String profilePictureUrl;
    private int currentLoad;
    private int maxGroups;
    private boolean available;
}
