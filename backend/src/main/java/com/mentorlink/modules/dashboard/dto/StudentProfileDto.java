package com.mentorlink.modules.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StudentProfileDto {
    private Long userId;
    private String fullName;
    private String email;
    private String rollNumber;
    private String department;
    private Integer yearOfStudy;
    private String contactNumber;
    private String bio;
    private String profilePictureUrl;
    private List<String> skills;
    private List<String> interests;
    // Mentor & group info
    private MentorSummaryDto assignedMentor;
    private GroupSummaryDto group;
}
