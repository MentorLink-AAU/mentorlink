package com.mentorlink.modules.meetings.service;

import com.mentorlink.common.exception.ApiException;
import com.mentorlink.modules.groups.entity.Group;
import com.mentorlink.modules.groups.repository.GroupRepository;
import com.mentorlink.modules.meetings.dto.CreateMeetingRequest;
import com.mentorlink.modules.meetings.dto.LastMeetingByGroupDto;
import com.mentorlink.modules.meetings.dto.MeetingDto;
import com.mentorlink.modules.meetings.entity.Meeting;
import com.mentorlink.modules.meetings.repository.MeetingRepository;
import com.mentorlink.modules.projects.entity.Project;
import com.mentorlink.modules.projects.repository.ProjectRepository;
import com.mentorlink.modules.users.UserRepository;
import com.mentorlink.modules.users.entity.User;
import com.mentorlink.modules.faculty.entity.FacultyProfile;
import com.mentorlink.modules.faculty.repository.FacultyProfileRepository;
import com.mentorlink.modules.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Service for past meeting logs: students log meetings, faculty verify, admin views last-by-group. */
@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final ProjectRepository projectRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final NotificationService notificationService;

    /**
     * Student logs a meeting. Only group members of the project can add meetings.
     */
    @Transactional
    public MeetingDto addMeeting(Long projectId, CreateMeetingRequest request, String userEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Project not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "User not found"));

        if (!isGroupMember(project, user)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN",
                    "Only group members can log meetings. You must be a member of this project's group.");
        }

        Meeting meeting = Meeting.builder()
                .project(project)
                .meetingDate(request.getMeetingDate())
                .details(request.getDetails() != null ? request.getDetails().trim() : "")
                .verified(false)
                .loggedBy(user)
                .build();

        meeting = meetingRepository.save(meeting);
        return toDto(meeting);
    }

    /**
     * Faculty mentor verifies a meeting. Only the assigned mentor can verify.
     */
    @Transactional
    public MeetingDto verifyMeeting(Long projectId, Long meetingId, String userEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Project not found"));

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Meeting not found"));

        if (!meeting.getProject().getId().equals(projectId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Meeting does not belong to this project");
        }

        FacultyProfile mentor = facultyProfileRepository.findByUser_Email(userEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "User is not a faculty"));

        if (project.getMentor() == null || !project.getMentor().getId().equals(mentor.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN",
                    "Only the assigned mentor can verify meeting details");
        }

        meeting.setVerified(true);
        meeting.setVerifiedAt(Instant.now());
        meeting = meetingRepository.save(meeting);

        User loggedBy = meeting.getLoggedBy();
        if (loggedBy != null && loggedBy.getEmail() != null && !loggedBy.getEmail().isBlank()) {
            try {
                notificationService.create(loggedBy.getEmail(), "Your meeting for project \"" + project.getTitle() + "\" has been verified by your mentor.");
            } catch (Exception ignored) {}
        }

        return toDto(meeting);
    }

    /**
     * List all meetings for a project (student, faculty mentor, or admin).
     */
    public List<MeetingDto> listByProject(Long projectId, String userEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Project not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "User not found"));

        boolean canView = isGroupMember(project, user)
                || isMentor(project, user)
                || user.getRoles().contains("ADMIN");

        if (!canView) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have access to view meetings for this project");
        }

        return meetingRepository.findByProjectIdOrderByMeetingDateDesc(projectId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get last meeting for a project (used in project summary, faculty dashboard).
     */
    public Optional<MeetingDto> getLastMeeting(Long projectId) {
        return meetingRepository.findFirstByProjectIdOrderByMeetingDateDesc(projectId)
                .map(this::toDto);
    }

    /**
     * Admin: Get last meeting date per group (all projects with groups).
     */
    public List<LastMeetingByGroupDto> getLastMeetingByGroupForAdmin() {
        List<Group> groups = groupRepository.findAll();
        List<LastMeetingByGroupDto> result = new ArrayList<>();

        for (Group g : groups) {
            Project p = g.getProject();
            if (p == null) continue;

            Optional<Meeting> lastMeeting = meetingRepository.findFirstByProjectOrderByMeetingDateDesc(p);
            LastMeetingByGroupDto dto = new LastMeetingByGroupDto();
            dto.setGroupId(g.getId());
            dto.setGroupName(g.getName());
            dto.setProjectId(p.getId());
            dto.setProjectTitle(p.getTitle());
            dto.setLastMeetingDate(lastMeeting.map(Meeting::getMeetingDate).orElse(null));
            dto.setLastMeetingDetails(lastMeeting.map(Meeting::getDetails).orElse(null));
            dto.setVerified(lastMeeting.map(Meeting::isVerified).orElse(false));
            result.add(dto);
        }

        return result;
    }

    private boolean isGroupMember(Project project, User user) {
        Group g = project.getGroup();
        if (g == null) return false;
        return g.getMembers().stream().anyMatch(m -> m.getId().equals(user.getId()));
    }

    private boolean isMentor(Project project, User user) {
        if (project.getMentor() == null) return false;
        return project.getMentor().getEmail().equals(user.getEmail());
    }

    private MeetingDto toDto(Meeting m) {
        return MeetingDto.builder()
                .id(m.getId())
                .projectId(m.getProject().getId())
                .meetingDate(m.getMeetingDate())
                .details(m.getDetails())
                .verified(m.isVerified())
                .verifiedAt(m.getVerifiedAt())
                .loggedById(m.getLoggedBy() != null ? m.getLoggedBy().getId() : null)
                .loggedByName(m.getLoggedBy() != null ? m.getLoggedBy().getFullName() : null)
                .createdAt(m.getCreatedAt())
                .build();
    }
}
