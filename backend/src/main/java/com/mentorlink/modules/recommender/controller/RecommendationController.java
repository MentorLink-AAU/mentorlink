package com.mentorlink.modules.recommender.controller;

import com.mentorlink.common.dto.ApiResponse;
import com.mentorlink.modules.recommender.dto.ProjectRecommendationRequest;
import com.mentorlink.modules.recommender.dto.RecommendationResponse;
import com.mentorlink.modules.recommender.service.FacultyRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Faculty recommender: TF-IDF + cosine similarity on project description. */
@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendationController {

    private final FacultyRecommendationService facultyRecommendationService;

    /**
     * Recommend faculty mentors based on project description using TF-IDF + Cosine Similarity.
     *
     * POST /api/recommend/mentor
     * Body: { "projectDescription": "Machine learning system for predicting student performance" }
     */
    @PostMapping("/mentor")
    public ResponseEntity<ApiResponse<RecommendationResponse>> recommendMentor(
            @RequestBody ProjectRecommendationRequest request) {
        RecommendationResponse response = facultyRecommendationService.recommendFaculty(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Recommend top N faculty. Default topN=10.
     */
    @PostMapping("/mentor/top/{topN}")
    public ResponseEntity<ApiResponse<RecommendationResponse>> recommendMentorTopN(
            @RequestBody ProjectRecommendationRequest request,
            @PathVariable int topN) {
        int n = Math.max(1, Math.min(50, topN));
        RecommendationResponse response = facultyRecommendationService.recommendFaculty(request, n);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Recommend by project ID (uses project title, description, domain).
     */
    @GetMapping("/mentor/project/{projectId}")
    public ResponseEntity<ApiResponse<RecommendationResponse>> recommendByProject(
            @PathVariable Long projectId,
            Authentication auth) {
        RecommendationResponse response = facultyRecommendationService.recommendForProject(projectId, auth.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
