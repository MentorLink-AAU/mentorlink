package com.mentorlink.modules.submissions.controller;

import com.mentorlink.common.dto.ApiResponse;
import com.mentorlink.modules.submissions.dto.SubmissionResponseDto;
import com.mentorlink.modules.submissions.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/** Project submissions: upload, list, download, delete (Report/Research Paper/PPT). */
@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    /**
     * Upload submission (Report, Research Paper, or PPT).
     * Only group members can submit. One submission per category per project.
     */
    @PostMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<SubmissionResponseDto>> upload(
            @PathVariable Long projectId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category,
            Authentication auth) throws IOException {
        String email = auth.getName();
        SubmissionResponseDto dto = submissionService.upload(projectId, file, category, email);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    /**
     * List submissions for a project (students, faculty, admin).
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<List<SubmissionResponseDto>>> listByProject(@PathVariable Long projectId,
                                                                                  Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(submissionService.listByProject(projectId, auth.getName())));
    }

    /**
     * List submissions for a group (students, faculty, admin).
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<ApiResponse<List<SubmissionResponseDto>>> listByGroup(@PathVariable Long groupId,
                                                                                Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(submissionService.listByGroup(groupId, auth.getName())));
    }

    /**
     * Delete a submission. Only faculty (group mentor) or admin can delete.
     */
    @DeleteMapping("/{submissionId}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long submissionId, Authentication auth) throws IOException {
        String email = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        submissionService.delete(submissionId, email, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Submission removed successfully"));
    }

    /**
     * Download submission file. Group members, mentor, and admin can download.
     */
    @GetMapping("/{submissionId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long submissionId, Authentication auth) throws IOException {
        String email = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        var download = submissionService.getFile(submissionId, email, isAdmin);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + download.filename() + "\"")
                .body(download.resource());
    }
}
