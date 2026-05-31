package com.mentorlink.modules.dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberSummaryDto {
    private Long userId;
    private String fullName;
    private String email;
    private boolean isLeader;
}
