package com.mentorlink.modules.groups;

import com.mentorlink.common.dto.ApiResponse;
import com.mentorlink.modules.faculty.dto.MentorshipRequestDto;
import com.mentorlink.modules.faculty.dto.RequestMentorshipDto;
import com.mentorlink.modules.faculty.entity.FacultyMentorshipRequest;
import com.mentorlink.modules.faculty.service.FacultyMentorshipRequestService;
import com.mentorlink.modules.groups.dto.GroupRequestDto;
import jakarta.validation.Valid;
import com.mentorlink.modules.groups.dto.GroupResponseDto;
import com.mentorlink.modules.groups.service.GroupService;
import com.mentorlink.modules.groups.repository.GroupRepository;
import com.mentorlink.modules.groups.entity.Group;
import com.mentorlink.modules.users.UserRepository;
import com.mentorlink.modules.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Groups: create, join, mentor join, and request faculty mentorship. */
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final FacultyMentorshipRequestService mentorshipRequestService;

    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupResponseDto>> getGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(ApiResponse.success(groupService.getById(groupId)));
    }

    /** Create group; leader is the authenticated student. */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<GroupResponseDto>> createGroup(@Valid @RequestBody GroupRequestDto dto,
                                                                     Authentication authentication) {
        String email = authentication.getName(); // email comes from JWT
        User leader = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        GroupResponseDto group = groupService.createGroup(dto, leader.getId());
        return ResponseEntity.ok(ApiResponse.success(group));
    }

    /** Join group using student join token. */
    @PostMapping("/join/{token}")
    public ResponseEntity<ApiResponse<GroupResponseDto>> joinGroup(@PathVariable String token,
                                                                   Authentication authentication) {
        String email = authentication.getName(); // email comes from JWT
        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        GroupResponseDto group = groupService.joinGroup(token, student.getId());
        return ResponseEntity.ok(ApiResponse.success(group));
    }

    /** Faculty joins group as mentor using mentor token. */
    @PostMapping("/mentor/join/{token}")
    public ResponseEntity<ApiResponse<GroupResponseDto>> mentorJoin(@PathVariable String token,
                                                                    Authentication authentication) {
        String email = authentication.getName();
        User facultyUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        if (!facultyUser.getRoles().contains("FACULTY")) {
            throw new RuntimeException("Only faculty can join as mentor");
        }

        var group = groupService.mentorJoinByToken(token, email);
        return ResponseEntity.ok(ApiResponse.success(groupService.getById(group.getId())));
    }

    /** Student (group member) requests faculty mentorship for their group. */
    @PostMapping("/{groupId}/request-faculty")
    public ResponseEntity<ApiResponse<MentorshipRequestDto>> requestFaculty(
            @PathVariable Long groupId,
            @Valid @RequestBody RequestMentorshipDto dto,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        boolean isMember = group.getMembers().stream().anyMatch(m -> m.getId().equals(user.getId()));
        if (!isMember) {
            throw new RuntimeException("Only group members can request faculty mentorship");
        }
        String projectTopic = dto.getProjectTopic() != null && !dto.getProjectTopic().isBlank()
                ? dto.getProjectTopic() : (group.getProject() != null ? group.getProject().getTitle() : "Project");
        String projectDesc = dto.getProjectDescription() != null ? dto.getProjectDescription() : "";
        Long projectId = group.getProject() != null ? group.getProject().getId() : null;
        FacultyMentorshipRequest req = mentorshipRequestService.requestMentorship(
                groupId, dto.getFacultyId(), projectTopic, projectDesc, projectId);
        return ResponseEntity.ok(ApiResponse.success(mentorshipRequestService.toDto(req)));
    }
}
