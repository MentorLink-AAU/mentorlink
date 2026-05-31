package com.mentorlink.modules.dashboard.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProfileUpdateDto {
    private String fullName;
    private String contactNumber;
    private String bio;
    private List<String> skills;
    private List<String> interests;
    // Student specific
    private String rollNumber;
    private String department;
    private Integer yearOfStudy;
    // Faculty specific
    private String expertise;
    private String phoneNumber;
}
