package com.mentorlink.modules.projects.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mentorlink.common.exception.ApiException;
import com.mentorlink.modules.faculty.repository.FacultyProfileRepository;
import com.mentorlink.modules.groups.entity.Group;
import com.mentorlink.modules.groups.repository.GroupRepository;
import com.mentorlink.modules.projects.dto.ProjectRequestDto;
import com.mentorlink.modules.projects.dto.ProjectResponseDto;
import com.mentorlink.modules.projects.entity.Project;
import com.mentorlink.modules.projects.repository.ProjectRepository;
import com.mentorlink.modules.users.UserRepository;
import com.mentorlink.modules.users.entity.User;
import com.mentorlink.service.ProjectAccessService;
import com.mentorlink.service.ProjectAccessService.AccessFlags;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectService {

    public static final int MAX_GROUP_MEMBERS = 3;

    private final ProjectRepository projectRepository;
    private final GroupRepository groupRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final UserRepository userRepository;
    private final ProjectAccessService projectAccessService;

    public ProjectResponseDto getById(Long id, String userEmail) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Project not found"));
        projectAccessService.requireProjectAccess(project, userEmail);
        return toResponseDto(project, userEmail);
    }

    private ProjectResponseDto toResponseDto(Project project, String userEmail) {
        AccessFlags flags = projectAccessService.resolveAccess(project, userEmail);
        Group g = project.getGroup();
        return ProjectResponseDto.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .domain(project.getDomain())
                .techStack(project.getTechStack())
                .progress(project.getProgress())
                .groupId(g != null ? g.getId() : null)
                .joinToken(g != null && projectAccessService.includeJoinToken(flags) ? g.getJoinToken() : null)
                .mentorJoinToken(g != null && projectAccessService.includeMentorJoinToken(flags) ? g.getMentorJoinToken() : null)
                .hasMentor(project.getMentor() != null)
                .build();
    }

    /**
     * Update project title. Only group members (students) or admin can change it.
     */
    @Transactional
    public ProjectResponseDto updateTitle(Long projectId, String newTitle, String userEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Project not found"));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "User not found"));

        boolean isAdmin = user.getRoles().contains("ADMIN");
        boolean isGroupMember = project.getGroup() != null
                && project.getGroup().getMembers().stream().anyMatch(m -> m.getId().equals(user.getId()));

        if (!isAdmin && !isGroupMember) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only group members or admin can change the project title");
        }

        if (newTitle == null || newTitle.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Project title cannot be empty");
        }

        project.setTitle(newTitle.trim());
        projectRepository.save(project);
        return toResponseDto(project, userEmail);
    }

    public Project updateProgress(Long projectId, int progress, String facultyEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Project not found"));
        if (project.getMentor() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "NO_MENTOR", "Project has no mentor assigned");
        }
        if (!project.getMentor().getEmail().equals(facultyEmail)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only assigned faculty can update progress");
        }
        project.setProgress(progress);
        return projectRepository.save(project);
    }

    @Transactional
    public ProjectResponseDto createProject(ProjectRequestDto dto, String creatorEmail) {
        User leader = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "User not found"));

        if (!groupRepository.findByMembersContaining(leader).isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ALREADY_IN_GROUP",
                    "You already belong to a group. You cannot create another project/group.");
        }

        Project project = Project.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .domain(dto.getDomain())
                .techStack(dto.getTechStack())
                .build();
        project = projectRepository.save(project);

        // ✅ Auto-create group: creator becomes leader, others can join via joinToken (max 3 members)
        Group group = Group.builder()
                .name("Group: " + dto.getTitle())
                .project(project)
                .leader(leader)
                .joinToken(UUID.randomUUID().toString())
                .mentorJoinToken(UUID.randomUUID().toString())
                .build();
        group.getMembers().add(leader);
        project.setGroup(group);
        group = groupRepository.save(group);

        return ProjectResponseDto.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .domain(project.getDomain())
                .techStack(project.getTechStack())
                .progress(project.getProgress())
                .groupId(group.getId())
                .joinToken(group.getJoinToken())
                .mentorJoinToken(group.getMentorJoinToken())
                .build();
    }
}
