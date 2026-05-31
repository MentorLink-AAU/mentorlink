package com.mentorlink.modules.admin.dto;

import com.mentorlink.modules.groups.dto.GroupResponseDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AutoGroupResultDto {
    private int groupsCreated;
    private int studentsGrouped;
    private int facultyAssigned;  // faculty auto-assigned to new groups (respecting 3 slots max)
    private List<String> studentsNotFound;
    private List<String> studentsSkipped;  // already in a group
    private List<String> errors;
    private List<GroupResponseDto> createdGroups;
}
