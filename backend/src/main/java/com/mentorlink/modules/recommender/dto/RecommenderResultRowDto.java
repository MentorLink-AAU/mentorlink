package com.mentorlink.modules.recommender.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecommenderResultRowDto {
    private String groupId;
    private List<String> students;
    private String facultyName;
    private Double similarityScore;
    private String algorithmUsed;
}
