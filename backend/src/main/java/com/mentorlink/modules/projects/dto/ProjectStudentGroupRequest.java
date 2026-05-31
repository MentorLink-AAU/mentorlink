package com.mentorlink.modules.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectStudentGroupRequest {
    private Long projectId;
    private List<Long> studentIds;
}
