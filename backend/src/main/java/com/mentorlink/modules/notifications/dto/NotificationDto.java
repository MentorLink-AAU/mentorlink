package com.mentorlink.modules.notifications.dto;

import com.mentorlink.modules.notifications.entity.Notification;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationDto {
    private Long id;
    private String message;
    private boolean read;
    private Long userId;

    public static NotificationDto from(Notification n) {
        return NotificationDto.builder()
                .id(n.getId())
                .message(n.getMessage())
                .read(n.isRead())
                .userId(n.getUser() != null ? n.getUser().getId() : null)
                .build();
    }
}
