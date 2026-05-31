package com.mentorlink.modules.recommender.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FacultyRecommendationDto {
    private Long facultyId;
    private String facultyName;
    private String email;
    private String department;
    private String expertise;
    private double similarityScore;
    private int currentLoad;
    private int maxGroups;
    private boolean available;
}
