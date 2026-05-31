package com.mentorlink.modules.meetings.controller;

import com.mentorlink.common.dto.ApiResponse;
import com.mentorlink.modules.meetings.dto.CreateMeetingRequest;
import com.mentorlink.modules.meetings.dto.LastMeetingByGroupDto;
import com.mentorlink.modules.meetings.dto.MeetingDto;
import com.mentorlink.modules.meetings.service.MeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REST controller for past meeting logs: student logs, faculty verifies, admin views last-by-group. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    /** Student: Log a new meeting */
    @PostMapping("/projects/{projectId}/meetings")
    public ResponseEntity<ApiResponse<MeetingDto>> addMeeting(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateMeetingRequest request,
            Authentication auth) {
        return ResponseEntity.ok(
                ApiResponse.success(meetingService.addMeeting(projectId, request, auth.getName())));
    }

    /** List meetings for a project (student, faculty, admin) */
    @GetMapping("/projects/{projectId}/meetings")
    public ResponseEntity<ApiResponse<List<MeetingDto>>> listMeetings(
            @PathVariable Long projectId,
            Authentication auth) {
        return ResponseEntity.ok(
                ApiResponse.success(meetingService.listByProject(projectId, auth.getName())));
    }

    /** Faculty: Verify a meeting */
    @PostMapping("/projects/{projectId}/meetings/{meetingId}/verify")
    public ResponseEntity<ApiResponse<MeetingDto>> verifyMeeting(
            @PathVariable Long projectId,
            @PathVariable Long meetingId,
            Authentication auth) {
        return ResponseEntity.ok(
                ApiResponse.success(meetingService.verifyMeeting(projectId, meetingId, auth.getName())));
    }

    /** Admin: Get last meeting date per group */
    @GetMapping("/admin/meetings/last-by-group")
    public ResponseEntity<ApiResponse<List<LastMeetingByGroupDto>>> getLastMeetingByGroup() {
        return ResponseEntity.ok(
                ApiResponse.success(meetingService.getLastMeetingByGroupForAdmin()));
    }
}
