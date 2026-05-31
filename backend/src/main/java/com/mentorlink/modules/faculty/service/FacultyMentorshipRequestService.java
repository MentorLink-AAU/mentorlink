package com.mentorlink.modules.faculty.service;

import com.mentorlink.modules.faculty.dto.MentorshipRequestDto;
import com.mentorlink.modules.faculty.entity.FacultyMentorshipRequest;
import com.mentorlink.modules.faculty.entity.FacultyProfile;
import com.mentorlink.modules.faculty.repository.FacultyMentorshipRequestRepository;
import com.mentorlink.modules.faculty.repository.FacultyProfileRepository;
import com.mentorlink.modules.groups.entity.Group;
import com.mentorlink.modules.groups.repository.GroupRepository;
import com.mentorlink.modules.projects.entity.Project;
import com.mentorlink.modules.projects.repository.ProjectRepository;
import com.mentorlink.modules.users.entity.User;
import com.mentorlink.modules.users.UserRepository;
import com.mentorlink.modules.notifications.service.NotificationService;
import com.mentorlink.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FacultyMentorshipRequestService {

    private final FacultyMentorshipRequestRepository requestRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final GroupRepository groupRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final EmailNotificationService emailNotificationService;
    private final NotificationService notificationService;

    /**
     * Student group requests faculty mentorship with project details.
     */
    @Transactional
    public FacultyMentorshipRequest requestMentorship(Long groupId, Long facultyId, String projectTopic,
                                                     String projectDescription, Long projectId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        FacultyProfile faculty = facultyProfileRepository.findById(facultyId)
                .orElseThrow(() -> new RuntimeException("Faculty not found"));

        if (faculty.getCurrentLoad() >= faculty.getMaxGroups()) {
            throw new RuntimeException("Faculty has reached max group limit");
        }
        if (requestRepository.existsByGroup_IdAndFaculty_IdAndStatus(group.getId(), faculty.getId(), FacultyMentorshipRequest.RequestStatus.PENDING)) {
            throw new RuntimeException("A pending mentorship request to this faculty already exists for this group");
        }

        Project project = group.getProject();
        if (project == null && projectId != null) {
            project = projectRepository.findById(projectId).orElse(null);
        }
        if (project == null) {
            project = Project.builder()
                    .title(projectTopic)
                    .description(projectDescription != null ? projectDescription : "")
                    .domain("General")
                    .techStack("TBD")
                    .progress(0)
                    .build();
            project = projectRepository.save(project);
            group.setProject(project);
            project.setGroup(group);
            groupRepository.save(group);
        } else {
            project.setTitle(projectTopic);
            if (projectDescription != null) project.setDescription(projectDescription);
            projectRepository.save(project);
        }

        FacultyMentorshipRequest req = FacultyMentorshipRequest.builder()
                .group(group)
                .faculty(faculty)
                .project(project)
                .projectTopic(projectTopic)
                .projectDescription(projectDescription)
                .status(FacultyMentorshipRequest.RequestStatus.PENDING)
                .requestedAt(Instant.now())
                .build();
        req = requestRepository.save(req);
        String facultyEmail = faculty.getEmail();
        if (facultyEmail != null && !facultyEmail.isBlank()) {
            try {
                String groupName = group.getName() != null ? group.getName() : "A group";
                notificationService.create(facultyEmail, "New mentorship request from " + groupName + " for project \"" + projectTopic + "\".");
            } catch (Exception ignored) {}
        }
        return req;
    }

    /**
     * Faculty joins a group as mentor using a mentorJoinToken.
     */
    @Transactional
    public Group joinAsMentorByToken(String mentorToken, String facultyEmail) {
        FacultyProfile faculty = facultyProfileRepository.findByUser_Email(facultyEmail)
                .orElseThrow(() -> new RuntimeException("Faculty not found"));
        if (faculty.getCurrentLoad() >= faculty.getMaxGroups()) {
            throw new RuntimeException("Faculty has reached max group limit");
        }

        Group group = groupRepository.findByMentorJoinToken(mentorToken)
                .orElseThrow(() -> new RuntimeException("Invalid mentor token"));

        Project project = group.getProject();
        if (project == null) {
            throw new RuntimeException("Group has no project linked");
        }
        if (project.getMentor() != null) {
            throw new RuntimeException("Project already has a mentor");
        }

        project.setMentor(faculty);
        faculty.setCurrentLoad(faculty.getCurrentLoad() + 1);
        projectRepository.save(project);
        facultyProfileRepository.save(faculty);

        return group;
    }

    @Transactional
    public FacultyMentorshipRequest approve(Long requestId, String facultyEmail) {
        FacultyMentorshipRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        if (req.getStatus() != FacultyMentorshipRequest.RequestStatus.PENDING) {
            throw new RuntimeException("Request already processed");
        }
        FacultyProfile faculty = facultyProfileRepository.findByUser_Email(facultyEmail)
                .orElseThrow(() -> new RuntimeException("Faculty not found"));
        if (!req.getFaculty().getId().equals(faculty.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        if (faculty.getCurrentLoad() >= faculty.getMaxGroups()) {
            throw new RuntimeException("Faculty has reached max group limit");
        }

        req.setStatus(FacultyMentorshipRequest.RequestStatus.APPROVED);
        req.setRespondedAt(Instant.now());
        req.getProject().setMentor(faculty);
        faculty.setCurrentLoad(faculty.getCurrentLoad() + 1);
        requestRepository.save(req);
        projectRepository.save(req.getProject());
        facultyProfileRepository.save(faculty);
        String leaderEmail = req.getGroup().getLeader() != null ? req.getGroup().getLeader().getEmail() : null;
        if (leaderEmail != null && !leaderEmail.isBlank()) {
            emailNotificationService.sendApprovalNotification(leaderEmail, req.getProjectTopic(), true);
            try {
                notificationService.create(leaderEmail, "Your faculty mentorship request for \"" + req.getProjectTopic() + "\" has been approved.");
            } catch (Exception ignored) {}
        }
        return req;
    }

    @Transactional
    public FacultyMentorshipRequest reject(Long requestId, String facultyEmail) {
        FacultyMentorshipRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        if (req.getStatus() != FacultyMentorshipRequest.RequestStatus.PENDING) {
            throw new RuntimeException("Request already processed");
        }
        FacultyProfile faculty = facultyProfileRepository.findByUser_Email(facultyEmail)
                .orElseThrow(() -> new RuntimeException("Faculty not found"));
        if (!req.getFaculty().getId().equals(faculty.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        req.setStatus(FacultyMentorshipRequest.RequestStatus.REJECTED);
        req.setRespondedAt(Instant.now());
        FacultyMentorshipRequest saved = requestRepository.save(req);
        String leaderEmail = req.getGroup().getLeader() != null ? req.getGroup().getLeader().getEmail() : null;
        if (leaderEmail != null && !leaderEmail.isBlank()) {
            emailNotificationService.sendApprovalNotification(leaderEmail, req.getProjectTopic(), false);
            try {
                notificationService.create(leaderEmail, "Your faculty mentorship request for \"" + req.getProjectTopic() + "\" has been rejected.");
            } catch (Exception ignored) {}
        }
        return saved;
    }

    public List<FacultyMentorshipRequest> getPendingForFaculty(String facultyEmail) {
        FacultyProfile faculty = facultyProfileRepository.findByUser_Email(facultyEmail)
                .orElseThrow(() -> new RuntimeException("Faculty not found"));
        return requestRepository.findByFacultyAndStatus(faculty, FacultyMentorshipRequest.RequestStatus.PENDING);
    }

    public MentorshipRequestDto toDto(FacultyMentorshipRequest req) {
        return MentorshipRequestDto.builder()
                .id(req.getId())
                .groupId(req.getGroup().getId())
                .groupName(req.getGroup().getName())
                .facultyId(req.getFaculty().getId())
                .facultyName(req.getFaculty().getName())
                .projectId(req.getProject().getId())
                .projectTopic(req.getProjectTopic())
                .projectDescription(req.getProjectDescription())
                .status(req.getStatus())
                .requestedAt(req.getRequestedAt())
                .leaderEmail(req.getGroup().getLeader() != null ? req.getGroup().getLeader().getEmail() : null)
                .build();
    }
}
