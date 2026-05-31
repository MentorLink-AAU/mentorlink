package com.mentorlink.modules.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AdminDashboardDto {
    private AdminProfileDto profile;
    private Map<String, Object> analytics;
    private long unreadNotificationCount;
}
