package com.mentorlink.modules.recommender.service;

import com.mentorlink.modules.faculty.entity.FacultyProfile;
import com.mentorlink.modules.faculty.repository.FacultyProfileRepository;
import com.mentorlink.modules.projects.repository.ProjectRepository;
import com.mentorlink.modules.recommender.dto.FacultyRecommendationDto;
import com.mentorlink.modules.recommender.dto.ProjectRecommendationRequest;
import com.mentorlink.modules.recommender.dto.RecommendationResponse;
import com.mentorlink.recommender.algorithm.TFIDFVectorizer;
import com.mentorlink.recommender.algorithm.TextPreprocessor;
import com.mentorlink.recommender.algorithm.VectorCosineSimilarity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Faculty Recommendation Engine using TF-IDF and Cosine Similarity.
 * Matches project description to faculty expertise.
 */
@Service
@RequiredArgsConstructor
public class FacultyRecommendationService {

    private final FacultyProfileRepository facultyProfileRepository;
    private final ProjectRepository projectRepository;

    private static final int DEFAULT_TOP_N = 10;

    /**
     * Recommend faculty based on project description using TF-IDF + Cosine Similarity.
     */
    public RecommendationResponse recommendFaculty(ProjectRecommendationRequest request) {
        return recommendFaculty(request, DEFAULT_TOP_N);
    }

    /**
     * Recommend top N faculty based on project description.
     */
    public RecommendationResponse recommendFaculty(ProjectRecommendationRequest request, int topN) {
        String combinedText = buildProjectText(request);
        if (combinedText == null || combinedText.isBlank()) {
            return RecommendationResponse.builder()
                    .projectDescription(request != null ? request.getProjectDescription() : "")
                    .recommendedFaculty(List.of())
                    .build();
        }

        List<FacultyProfile> faculties = facultyProfileRepository.findAll();
        if (faculties.isEmpty()) {
            return RecommendationResponse.builder()
                    .projectDescription(combinedText)
                    .recommendedFaculty(List.of())
                    .build();
        }

        // Preprocess project text
        List<String> projectTokens = TextPreprocessor.preprocess(combinedText);
        if (projectTokens.isEmpty()) {
            return RecommendationResponse.builder()
                    .projectDescription(combinedText)
                    .recommendedFaculty(List.of())
                    .build();
        }

        // Build corpus: project + all faculty expertise
        List<List<String>> corpus = new ArrayList<>();
        corpus.add(projectTokens);
        for (FacultyProfile f : faculties) {
            String expertise = f.getExpertise() != null ? f.getExpertise() : "";
            if (f.getDepartment() != null) expertise += " " + f.getDepartment();
            corpus.add(TextPreprocessor.preprocess(expertise));
        }

        // Compute IDF from corpus
        Map<String, Double> idf = TFIDFVectorizer.computeIDF(corpus);

        // Project TF-IDF vector
        Map<String, Double> projectVector = TFIDFVectorizer.computeTFIDF(projectTokens, idf);

        // Score each faculty
        List<FacultyRecommendationDto> scored = new ArrayList<>();
        for (int i = 0; i < faculties.size(); i++) {
            FacultyProfile f = faculties.get(i);
            String expertise = f.getExpertise() != null ? f.getExpertise() : "";
            if (f.getDepartment() != null) expertise += " " + f.getDepartment();
            List<String> facultyTokens = TextPreprocessor.preprocess(expertise);
            Map<String, Double> facultyVector = TFIDFVectorizer.computeTFIDF(facultyTokens, idf);

            double score = VectorCosineSimilarity.cosineSimilarity(projectVector, facultyVector);

            scored.add(FacultyRecommendationDto.builder()
                    .facultyId(f.getId())
                    .facultyName(f.getName())
                    .email(f.getEmail())
                    .department(f.getDepartment())
                    .expertise(f.getExpertise())
                    .similarityScore(Math.round(score * 100.0) / 100.0)
                    .currentLoad(f.getCurrentLoad())
                    .maxGroups(f.getMaxGroups())
                    .available(f.getCurrentLoad() < f.getMaxGroups())
                    .build());
        }

        // Sort by score descending, optionally prioritize available faculty
        List<FacultyRecommendationDto> sorted = scored.stream()
                .sorted((a, b) -> {
                    int cmp = Double.compare(b.getSimilarityScore(), a.getSimilarityScore());
                    if (cmp != 0) return cmp;
                    return Boolean.compare(b.isAvailable(), a.isAvailable());
                })
                .limit(topN)
                .collect(Collectors.toList());

        return RecommendationResponse.builder()
                .projectDescription(combinedText)
                .recommendedFaculty(sorted)
                .build();
    }

    /**
     * Convenience: recommend from project description string only.
     */
    public RecommendationResponse recommendFaculty(String projectDescription) {
        ProjectRecommendationRequest req = new ProjectRecommendationRequest();
        req.setProjectDescription(projectDescription);
        return recommendFaculty(req);
    }

    /**
     * Recommend faculty for an existing project (uses title, description, domain).
     */
    public RecommendationResponse recommendForProject(Long projectId) {
        return projectRepository.findById(projectId)
                .map(p -> {
                    ProjectRecommendationRequest req = new ProjectRecommendationRequest();
                    req.setProjectTitle(p.getTitle());
                    req.setProjectDescription(p.getDescription());
                    req.setDomain(p.getDomain());
                    return recommendFaculty(req);
                })
                .orElse(RecommendationResponse.builder()
                        .projectDescription("")
                        .recommendedFaculty(List.of())
                        .build());
    }

    private String buildProjectText(ProjectRecommendationRequest request) {
        if (request == null) return "";
        StringBuilder sb = new StringBuilder();
        if (request.getProjectDescription() != null) sb.append(request.getProjectDescription()).append(" ");
        if (request.getProjectTitle() != null) sb.append(request.getProjectTitle()).append(" ");
        if (request.getDomain() != null) sb.append(request.getDomain()).append(" ");
        return sb.toString().trim();
    }
}
