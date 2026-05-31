package com.mentorlink.modules.meetings.controller;

import com.mentorlink.common.dto.ApiResponse;
import com.mentorlink.modules.meetings.dto.CounterProposeRequest;
import com.mentorlink.modules.meetings.dto.MeetingScheduleRequestDto;
import com.mentorlink.modules.meetings.dto.ProposeMeetingRequest;
import com.mentorlink.modules.meetings.service.MeetingScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for future meeting schedule flow.
 * Students propose dates; faculty approve or counter-propose; both must agree to schedule.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MeetingScheduleController {

    private final MeetingScheduleService meetingScheduleService;

    /** Student: Propose a future meeting date */
    @PostMapping("/projects/{projectId}/meeting-schedule")
    public ResponseEntity<ApiResponse<MeetingScheduleRequestDto>> propose(
            @PathVariable Long projectId,
            @Valid @RequestBody ProposeMeetingRequest request,
            Authentication auth) {
        return ResponseEntity.ok(
                ApiResponse.success(meetingScheduleService.propose(projectId, request, auth.getName())));
    }

    /** Faculty: Approve proposed date */
    @PostMapping("/projects/{projectId}/meeting-schedule/{scheduleId}/approve")
    public ResponseEntity<ApiResponse<MeetingScheduleRequestDto>> approve(
            @PathVariable Long projectId,
            @PathVariable Long scheduleId,
            Authentication auth) {
        return ResponseEntity.ok(
                ApiResponse.success(meetingScheduleService.approve(projectId, scheduleId, auth.getName())));
    }

    /** Faculty: Counter-propose ("I'm free on this date") */
    @PostMapping("/projects/{projectId}/meeting-schedule/{scheduleId}/counter")
    public ResponseEntity<ApiResponse<MeetingScheduleRequestDto>> counterPropose(
            @PathVariable Long projectId,
            @PathVariable Long scheduleId,
            @Valid @RequestBody CounterProposeRequest request,
            Authentication auth) {
        return ResponseEntity.ok(
                ApiResponse.success(meetingScheduleService.counterPropose(projectId, scheduleId, request, auth.getName())));
    }

    /** Student: Accept faculty's counter-proposal */
    @PostMapping("/projects/{projectId}/meeting-schedule/{scheduleId}/accept")
    public ResponseEntity<ApiResponse<MeetingScheduleRequestDto>> acceptCounter(
            @PathVariable Long projectId,
            @PathVariable Long scheduleId,
            Authentication auth) {
        return ResponseEntity.ok(
                ApiResponse.success(meetingScheduleService.acceptCounter(projectId, scheduleId, auth.getName())));
    }

    /** Student: Propose new date (after faculty counter) */
    @PostMapping("/projects/{projectId}/meeting-schedule/{scheduleId}/propose-new")
    public ResponseEntity<ApiResponse<MeetingScheduleRequestDto>> proposeNew(
            @PathVariable Long projectId,
            @PathVariable Long scheduleId,
            @Valid @RequestBody ProposeMeetingRequest request,
            Authentication auth) {
        return ResponseEntity.ok(
                ApiResponse.success(meetingScheduleService.proposeNew(projectId, scheduleId, request, auth.getName())));
    }

    /** List schedule requests for a project */
    @GetMapping("/projects/{projectId}/meeting-schedule")
    public ResponseEntity<ApiResponse<List<MeetingScheduleRequestDto>>> listByProject(
            @PathVariable Long projectId,
            Authentication auth) {
        return ResponseEntity.ok(
                ApiResponse.success(meetingScheduleService.listByProject(projectId, auth.getName())));
    }

    /** Admin: List all scheduled (approved) future meetings */
    @GetMapping("/admin/meeting-schedule")
    public ResponseEntity<ApiResponse<List<MeetingScheduleRequestDto>>> listScheduledForAdmin() {
        return ResponseEntity.ok(
                ApiResponse.success(meetingScheduleService.listScheduledForAdmin()));
    }
}
