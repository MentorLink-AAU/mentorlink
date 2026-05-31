package com.mentorlink.modules.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GroupSummaryDto {
    private Long groupId;
    private String name;
    private Long projectId;
    private String projectTitle;
    private Long leaderId;
    private String leaderName;
    private List<MemberSummaryDto> members;
    private int memberCount;
}
