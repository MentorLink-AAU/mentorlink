package com.mentorlink.modules.groups.service;

import com.mentorlink.modules.dashboard.dto.MemberSummaryDto;
import com.mentorlink.modules.groups.dto.GroupRequestDto;
import com.mentorlink.modules.groups.dto.GroupResponseDto;
import com.mentorlink.modules.groups.entity.Group;
import com.mentorlink.modules.projects.entity.Project;
import com.mentorlink.common.exception.ApiException;
import com.mentorlink.modules.faculty.entity.FacultyProfile;
import com.mentorlink.modules.faculty.repository.FacultyProfileRepository;
import com.mentorlink.modules.groups.repository.GroupRepository;
import com.mentorlink.modules.projects.repository.ProjectRepository;
import com.mentorlink.modules.users.entity.User;
import com.mentorlink.modules.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** Group CRUD: create, join, mentor join, project creation. */
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final FacultyProfileRepository facultyProfileRepository;

    // ✅ Create a group with leader + project
    public GroupResponseDto createGroup(GroupRequestDto dto, Long leaderId) {
        User leader = userRepository.findById(leaderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Leader (student) not found"));

        if (!groupRepository.findByMembersContaining(leader).isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ALREADY_IN_GROUP",
                    "You are already in a group. Each student can only belong to one group.");
        }

        Project project;
        if (dto.getProjectId() != null) {
            if (groupRepository.existsByProjectId(dto.getProjectId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "CONFLICT", "This project already has a group. Use the join token instead.");
            }
            project = projectRepository.findById(dto.getProjectId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Project not found"));
            // Update project with projectTitle/projectDescription when provided (so your body values are saved)
            if (dto.getProjectTitle() != null && !dto.getProjectTitle().isBlank()) {
                project.setTitle(dto.getProjectTitle());
            }
            if (dto.getProjectDescription() != null && !dto.getProjectDescription().isBlank()) {
                project.setDescription(dto.getProjectDescription());
            }
            if (dto.getProjectDomain() != null && !dto.getProjectDomain().isBlank()) {
                project.setDomain(dto.getProjectDomain());
            }
            if (dto.getProjectTechStack() != null && !dto.getProjectTechStack().isBlank()) {
                project.setTechStack(dto.getProjectTechStack());
            }
            project = projectRepository.save(project);
        } else {
            project = Project.builder()
                    .title(dto.getProjectTitle() != null && !dto.getProjectTitle().isBlank() ? dto.getProjectTitle() : "New Project")
                    .description(dto.getProjectDescription() != null ? dto.getProjectDescription() : "")
                    .domain(dto.getProjectDomain() != null && !dto.getProjectDomain().isBlank() ? dto.getProjectDomain() : "General")
                    .techStack(dto.getProjectTechStack() != null && !dto.getProjectTechStack().isBlank() ? dto.getProjectTechStack() : "TBD")
                    .progress(0)
                    .build();
            project = projectRepository.save(project);
        }

        Group group = Group.builder()
                .name(dto.getName())
                .project(project)
                .leader(leader)
                .joinToken(UUID.randomUUID().toString())
                .mentorJoinToken(UUID.randomUUID().toString())
                .build();

        group.getMembers().add(leader); // ✅ add leader as member

        Group saved = groupRepository.save(group);

        return mapToResponse(saved);
    }

    public GroupResponseDto getById(Long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Group not found"));
        return mapToResponse(group);
    }

    public static final int MAX_GROUP_MEMBERS = 3;

    // ✅ Join group using token (max 3 members per group)
    public GroupResponseDto joinGroup(String token, Long studentId) {
        Group group = groupRepository.findByJoinToken(token)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", "Invalid join token"));

        if (group.getMembers().size() >= MAX_GROUP_MEMBERS) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "GROUP_FULL", "Group is full. Maximum " + MAX_GROUP_MEMBERS + " members allowed.");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Student not found"));

        if (group.getMembers().contains(student)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ALREADY_MEMBER", "You are already a member of this group.");
        }

        for (Group g : groupRepository.findByMembersContaining(student)) {
            if (!g.getId().equals(group.getId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "ALREADY_IN_GROUP",
                        "You are already in another group. Each student can only belong to one group.");
            }
        }

        group.getMembers().add(student);
        Group saved = groupRepository.save(group);

        return mapToResponse(saved);
    }

    /** Faculty joins group as mentor using mentorJoinToken. No request/approval flow. */
    @Transactional
    public Group mentorJoinByToken(String mentorToken, String facultyEmail) {
        FacultyProfile faculty = facultyProfileRepository.findByUser_Email(facultyEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Faculty not found"));
        if (faculty.getCurrentLoad() >= faculty.getMaxGroups()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "FACULTY_CLOSED",
                    "Faculty slots are full (" + faculty.getCurrentLoad() + "/" + faculty.getMaxGroups() + "). No access to join as mentor.");
        }
        Group group = groupRepository.findByMentorJoinToken(mentorToken)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", "Invalid mentor token"));
        Project project = group.getProject();
        if (project == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "NO_PROJECT", "Group has no project linked");
        }
        if (project.getMentor() != null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CONFLICT", "Project already has a mentor");
        }
        project.setMentor(faculty);
        faculty.setCurrentLoad(faculty.getCurrentLoad() + 1);
        projectRepository.save(project);
        facultyProfileRepository.save(faculty);
        return group;
    }

    // ✅ Mapper
    private GroupResponseDto mapToResponse(Group group) {
        Project p = group.getProject();
        List<MemberSummaryDto> members = group.getMembers().stream()
                .map(m -> MemberSummaryDto.builder()
                        .userId(m.getId())
                        .fullName(m.getFullName())
                        .email(m.getEmail())
                        .isLeader(group.getLeader() != null && group.getLeader().getId().equals(m.getId()))
                        .build())
                .collect(Collectors.toList());
        String mentorName = (p != null && p.getMentor() != null) ? p.getMentor().getName() : null;
        return GroupResponseDto.builder()
                .id(group.getId())
                .name(group.getName())
                .joinToken(group.getJoinToken())
                .mentorJoinToken(group.getMentorJoinToken())
                .projectId(p.getId())
                .projectTitle(p.getTitle())
                .projectDescription(p.getDescription())
                .leaderId(group.getLeader().getId())
                .memberCount(group.getMembers().size())
                .members(members)
                .mentorName(mentorName)
                .build();
    }
}
