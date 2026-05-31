package com.mentorlink.modules.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class AdminGroupProgressDto {
    private Long groupId;
    private String groupName;
    private Long projectId;
    private String projectTitle;
    private String projectDescription;
    private int progress;
    private int memberCount;
    private List<MemberInfo> members;
    private String mentorName;
    private String mentorEmail;
    private LocalDate lastMeetingDate;
    private String lastMeetingDetails;
    private Boolean lastMeetingVerified;

    @Data
    @Builder
    public static class MemberInfo {
        private Long userId;
        private String fullName;
        private String email;
        private boolean isLeader;
    }
}
