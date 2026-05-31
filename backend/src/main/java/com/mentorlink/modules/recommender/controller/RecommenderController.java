package com.mentorlink.modules.recommender.controller;

import com.mentorlink.common.dto.ApiResponse;
import com.mentorlink.modules.recommender.dto.RecommenderJobResponseDto;
import com.mentorlink.modules.recommender.dto.RecommenderResultRowDto;
import com.mentorlink.service.RecommenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RecommenderController {

    private final RecommenderService recommenderService;

    @PostMapping("/api/run/matrix_factorization")
    public ResponseEntity<ApiResponse<Map<String, String>>> runMatrixFactorization() {
        String jobId = recommenderService.startMatrixFactorizationJobFromDatabase();
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "jobId", jobId,
                "status", "RUNNING",
                "downloadUrl", "/api/recommender/result/" + jobId + "/download"
        )));
    }

    @GetMapping("/api/recommender/status/{jobId}")
    public ResponseEntity<ApiResponse<RecommenderJobResponseDto>> getStatus(@PathVariable String jobId) {
        return ResponseEntity.ok(ApiResponse.success(recommenderService.getJobStatus(jobId)));
    }

    @GetMapping("/api/recommender/result/{jobId}")
    public ResponseEntity<ApiResponse<List<RecommenderResultRowDto>>> getResult(@PathVariable String jobId) {
        return ResponseEntity.ok(ApiResponse.success(recommenderService.getJobResult(jobId)));
    }

    @GetMapping("/api/recommender/result/{jobId}/download")
    public ResponseEntity<byte[]> downloadResult(@PathVariable String jobId) throws Exception {
        byte[] bytes = recommenderService.downloadJobResultExcel(jobId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"auto-allocation-groups-" + jobId + ".xlsx\"")
                .body(bytes);
    }
}
