package com.mentorlink.modules.notifications.controller;

import com.mentorlink.common.dto.ApiResponse;
import com.mentorlink.modules.notifications.entity.Notification;
import com.mentorlink.modules.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Notifications: list, unread count, mark-as-read. */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getMine(
            Authentication auth,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getForUser(auth.getName(), limit)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> unreadCount(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getUnreadCount(auth.getName())));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Notification>> markRead(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.markAsRead(id, auth.getName())));
    }
}
