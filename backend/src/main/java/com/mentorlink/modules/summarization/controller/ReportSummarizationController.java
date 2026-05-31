package com.mentorlink.modules.summarization.controller;

import com.mentorlink.common.dto.ApiResponse;
import com.mentorlink.modules.summarization.dto.ReportSummaryDto;
import com.mentorlink.modules.summarization.service.ReportSummarizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/** Report summarization: upload PDF, generate NLP summary, list by project. */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ReportSummarizationController {

    private final ReportSummarizationService summarizationService;

    /**
     * POST /api/projects/{projectId}/summarize-report
     * Upload PDF report, generate structured summary via NLP, store in database.
     */
    @PostMapping("/{projectId}/summarize-report")
    public ResponseEntity<ApiResponse<ReportSummaryDto>> summarizeReport(
            @PathVariable Long projectId,
            @RequestParam("file") MultipartFile file,
            Authentication auth) throws IOException {
        String email = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        ReportSummaryDto result = summarizationService.summarizeReport(projectId, file, email, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * GET /api/projects/{projectId}/summaries
     * List all report summaries for a project (for faculty dashboard, etc.)
     */
    @GetMapping("/{projectId}/summaries")
    public ResponseEntity<ApiResponse<List<ReportSummaryDto>>> listSummaries(@PathVariable Long projectId) {
        List<ReportSummaryDto> summaries = summarizationService.listByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(summaries));
    }
}
