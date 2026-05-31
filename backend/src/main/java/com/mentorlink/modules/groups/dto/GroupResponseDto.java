package com.mentorlink.modules.groups.dto;

import com.mentorlink.modules.dashboard.dto.MemberSummaryDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GroupResponseDto {
    private Long id;
    private String name;
    private String joinToken;
    private String mentorJoinToken;
    private Long leaderId;
    private Long projectId;
    private String projectTitle;    // linked project title
    private String projectDescription;
    private int memberCount;
    private List<MemberSummaryDto> members;
    private String mentorName;      // faculty mentor name if assigned
}
