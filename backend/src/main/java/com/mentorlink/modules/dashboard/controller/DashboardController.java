package com.mentorlink.modules.dashboard.controller;

import com.mentorlink.common.dto.ApiResponse;
import com.mentorlink.modules.dashboard.dto.*;
import com.mentorlink.modules.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Role-specific dashboards: student, faculty, admin. */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/student")
    public ResponseEntity<ApiResponse<StudentDashboardDto>> studentDashboard(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getStudentDashboard(email)));
    }

    @GetMapping("/faculty")
    public ResponseEntity<ApiResponse<FacultyDashboardDto>> facultyDashboard(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getFacultyDashboard(email)));
    }

    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<AdminDashboardDto>> adminDashboard(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getAdminDashboard(email)));
    }
}
