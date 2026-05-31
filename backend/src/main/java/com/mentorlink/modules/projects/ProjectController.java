package com.mentorlink.modules.projects;

import com.mentorlink.common.dto.ApiResponse;
import com.mentorlink.modules.projects.dto.ProjectRequestDto;
import com.mentorlink.modules.projects.dto.ProjectResponseDto;
import com.mentorlink.modules.projects.entity.Project;
import com.mentorlink.modules.projects.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> getProject(@PathVariable Long projectId,
                                                                     Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getById(projectId, auth.getName())));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> create(@Valid @RequestBody ProjectRequestDto dto,
                                                                  Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(projectService.createProject(dto, auth.getName())));
    }

    @PutMapping("/{projectId}/title")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> updateTitle(
            @PathVariable Long projectId,
            @RequestBody java.util.Map<String, String> body,
            Authentication auth) {
        String newTitle = body.get("title");
        return ResponseEntity.ok(ApiResponse.success(projectService.updateTitle(projectId, newTitle, auth.getName())));
    }

    @PutMapping("/{projectId}/progress")
    public ResponseEntity<ApiResponse<Project>> updateProgress(
            @PathVariable Long projectId,
            @RequestBody java.util.Map<String, Integer> body,
            Authentication auth) {
        int progress = body.getOrDefault("progress", 0);
        progress = Math.max(0, Math.min(100, progress));
        return ResponseEntity.ok(ApiResponse.success(projectService.updateProgress(projectId, progress, auth.getName())));
    }
}
