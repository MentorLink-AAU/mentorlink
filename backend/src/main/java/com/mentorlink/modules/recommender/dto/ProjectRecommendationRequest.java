package com.mentorlink.modules.recommender.dto;

import lombok.Data;

@Data
public class ProjectRecommendationRequest {
    private String projectDescription;

    /**
     * Optional: combine with title and domain for richer matching.
     */
    private String projectTitle;
    private String domain;
}
