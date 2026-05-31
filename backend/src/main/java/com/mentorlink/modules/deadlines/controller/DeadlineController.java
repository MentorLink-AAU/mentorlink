package com.mentorlink.modules.deadlines.controller;

import com.mentorlink.common.dto.ApiResponse;
import com.mentorlink.modules.deadlines.entity.Deadline;
import com.mentorlink.modules.deadlines.service.DeadlineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Lists deadlines for authenticated users. */
@RestController
@RequestMapping("/api/deadlines")
@RequiredArgsConstructor
public class DeadlineController {

    private final DeadlineService deadlineService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Deadline>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(deadlineService.getAll()));
    }
}
