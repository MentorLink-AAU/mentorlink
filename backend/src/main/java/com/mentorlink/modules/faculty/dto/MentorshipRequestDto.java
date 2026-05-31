package com.mentorlink.modules.faculty.dto;

import com.mentorlink.modules.faculty.entity.FacultyMentorshipRequest;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class MentorshipRequestDto {
    private Long id;
    private Long groupId;
    private String groupName;
    private Long facultyId;
    private String facultyName;
    private Long projectId;
    private String projectTopic;
    private String projectDescription;
    private FacultyMentorshipRequest.RequestStatus status;
    private Instant requestedAt;
    private String leaderEmail;  // for email notification
}
