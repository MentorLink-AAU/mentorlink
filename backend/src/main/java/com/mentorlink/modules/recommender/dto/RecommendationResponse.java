package com.mentorlink.modules.recommender.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecommendationResponse {
    private String projectDescription;
    private List<FacultyRecommendationDto> recommendedFaculty;

    /**
     * Top single recommendation (convenience).
     */
    public FacultyRecommendationDto getTopRecommendation() {
        return recommendedFaculty != null && !recommendedFaculty.isEmpty()
                ? recommendedFaculty.get(0)
                : null;
    }
}
