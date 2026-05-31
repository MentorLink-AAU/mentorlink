package com.mentorlink.modules.meetings.service;

import com.mentorlink.common.exception.ApiException;
import com.mentorlink.modules.faculty.entity.FacultyProfile;
import com.mentorlink.modules.faculty.repository.FacultyProfileRepository;
import com.mentorlink.modules.groups.entity.Group;
import com.mentorlink.modules.meetings.dto.CounterProposeRequest;
import com.mentorlink.modules.meetings.dto.MeetingScheduleRequestDto;
import com.mentorlink.modules.meetings.dto.ProposeMeetingRequest;
import com.mentorlink.modules.meetings.entity.MeetingScheduleRequest;
import com.mentorlink.modules.meetings.entity.MeetingScheduleRequest.ScheduleStatus;
import com.mentorlink.modules.meetings.repository.MeetingScheduleRequestRepository;
import com.mentorlink.modules.notifications.service.NotificationService;
import com.mentorlink.modules.projects.entity.Project;
import com.mentorlink.modules.projects.repository.ProjectRepository;
import com.mentorlink.modules.users.UserRepository;
import com.mentorlink.modules.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/** Service for future meeting schedule flow: propose, approve, counter-propose, accept. */
@Service
@RequiredArgsConstructor
public class MeetingScheduleService {

    private final MeetingScheduleRequestRepository scheduleRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final NotificationService notificationService;

    /** Student: Propose a future meeting date */
    @Transactional
    public MeetingScheduleRequestDto propose(Long projectId, ProposeMeetingRequest request, String userEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Project not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "User not found"));

        if (!isGroupMember(project, user)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN",
                    "Only group members can propose meeting schedules");
        }

        MeetingScheduleRequest req = MeetingScheduleRequest.builder()
                .project(project)
                .proposedBy(user)
                .proposedDate(request.getProposedDate())
                .notes(request.getNotes() != null ? request.getNotes().trim() : null)
                .status(ScheduleStatus.PENDING_FACULTY)
                .build();

        req = scheduleRepository.save(req);
        notifyMentor(project, "A student has proposed a meeting date: " + request.getProposedDate() + ". Please approve or suggest an alternative.");
        return toDto(req);
    }

    /** Faculty: Approve the proposed date (or accept student's acceptance of faculty counter) */
    @Transactional
    public MeetingScheduleRequestDto approve(Long projectId, Long scheduleId, String userEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Project not found"));

        MeetingScheduleRequest req = scheduleRepository.findByIdAndProjectId(scheduleId, projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Schedule request not found"));

        if (!isMentor(project, userEmail)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only the assigned mentor can approve");
        }

        if (req.getStatus() != ScheduleStatus.PENDING_FACULTY) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                    "Can only approve when status is PENDING_FACULTY. Current: " + req.getStatus());
        }

        req.setStatus(ScheduleStatus.APPROVED);
        req.setAgreedDate(req.getProposedDate());
        req = scheduleRepository.save(req);

        notifyStudent(req, "Your mentor has approved the meeting date: " + req.getAgreedDate() + ". Meeting scheduled!");
        return toDto(req);
    }

    /** Faculty: Counter-propose ("I'm not free, I'm free on X") */
    @Transactional
    public MeetingScheduleRequestDto counterPropose(Long projectId, Long scheduleId,
            CounterProposeRequest request, String userEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Project not found"));

        MeetingScheduleRequest req = scheduleRepository.findByIdAndProjectId(scheduleId, projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Schedule request not found"));

        if (!isMentor(project, userEmail)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only the assigned mentor can counter-propose");
        }

        if (req.getStatus() != ScheduleStatus.PENDING_FACULTY) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                    "Can only counter when status is PENDING_FACULTY");
        }

        req.setFacultyCounterDate(request.getFacultyCounterDate());
        req.setFacultyCounterNotes(request.getFacultyCounterNotes() != null ? request.getFacultyCounterNotes().trim() : null);
        req.setStatus(ScheduleStatus.PENDING_STUDENT);
        req = scheduleRepository.save(req);

        String msg = "Your mentor suggests meeting on " + request.getFacultyCounterDate();
        if (request.getFacultyCounterNotes() != null && !request.getFacultyCounterNotes().isBlank()) {
            msg += ": " + request.getFacultyCounterNotes();
        }
        msg += ". Please accept or propose a different date.";
        notifyStudent(req, msg);
        return toDto(req);
    }

    /** Student: Accept faculty's counter-proposal */
    @Transactional
    public MeetingScheduleRequestDto acceptCounter(Long projectId, Long scheduleId, String userEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Project not found"));

        MeetingScheduleRequest req = scheduleRepository.findByIdAndProjectId(scheduleId, projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Schedule request not found"));

        if (!isGroupMember(project, userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "User not found")))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only group members can accept");
        }

        if (req.getStatus() != ScheduleStatus.PENDING_STUDENT || req.getFacultyCounterDate() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                    "Can only accept when faculty has counter-proposed");
        }

        req.setStatus(ScheduleStatus.APPROVED);
        req.setAgreedDate(req.getFacultyCounterDate());
        req = scheduleRepository.save(req);

        notifyMentor(project, "Student accepted your suggested date: " + req.getAgreedDate() + ". Meeting scheduled!");
        return toDto(req);
    }

    /** Student: Propose new date (after faculty counter) */
    @Transactional
    public MeetingScheduleRequestDto proposeNew(Long projectId, Long scheduleId,
            ProposeMeetingRequest request, String userEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Project not found"));

        MeetingScheduleRequest req = scheduleRepository.findByIdAndProjectId(scheduleId, projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Schedule request not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "User not found"));
        if (!isGroupMember(project, user)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only group members can propose");
        }

        if (req.getStatus() != ScheduleStatus.PENDING_STUDENT) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                    "Can only propose new date when waiting for student response");
        }

        req.setProposedDate(request.getProposedDate());
        req.setNotes(request.getNotes() != null ? request.getNotes().trim() : req.getNotes());
        req.setFacultyCounterDate(null);
        req.setFacultyCounterNotes(null);
        req.setStatus(ScheduleStatus.PENDING_FACULTY);
        req = scheduleRepository.save(req);

        notifyMentor(project, "Student proposed a new meeting date: " + request.getProposedDate() + ". Please approve or suggest an alternative.");
        return toDto(req);
    }

    /** List schedule requests for a project (student, faculty, admin) */
    public List<MeetingScheduleRequestDto> listByProject(Long projectId, String userEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Project not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "User not found"));

        boolean canView = isGroupMember(project, user) || isMentor(project, userEmail)
                || user.getRoles() != null && user.getRoles().contains("ADMIN");
        if (!canView) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have access to this project's schedules");
        }

        return scheduleRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /** Admin: List all approved/scheduled future meetings */
    public List<MeetingScheduleRequestDto> listScheduledForAdmin() {
        return scheduleRepository.findByStatusOrderByAgreedDateAsc(ScheduleStatus.APPROVED).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private boolean isGroupMember(Project project, User user) {
        Group g = project.getGroup();
        if (g == null) return false;
        return g.getMembers().stream().anyMatch(m -> m.getId().equals(user.getId()));
    }

    private boolean isMentor(Project project, String userEmail) {
        if (project.getMentor() == null) return false;
        FacultyProfile mentor = project.getMentor();
        if (mentor.getUser() != null && userEmail.equals(mentor.getUser().getEmail())) return true;
        return userEmail.equals(mentor.getEmail());
    }

    private void notifyMentor(Project project, String message) {
        if (project.getMentor() == null) return;
        FacultyProfile m = project.getMentor();
        String email = m.getUser() != null ? m.getUser().getEmail() : m.getEmail();
        if (email == null || email.isBlank()) return;
        try {
            notificationService.create(email, message);
        } catch (Exception ignored) {}
    }

    private void notifyStudent(MeetingScheduleRequest req, String message) {
        if (req.getProposedBy() == null || req.getProposedBy().getEmail() == null) return;
        try {
            notificationService.create(req.getProposedBy().getEmail(), message);
        } catch (Exception ignored) {}
    }

    private MeetingScheduleRequestDto toDto(MeetingScheduleRequest r) {
        return MeetingScheduleRequestDto.builder()
                .id(r.getId())
                .projectId(r.getProject().getId())
                .projectTitle(r.getProject().getTitle())
                .proposedById(r.getProposedBy() != null ? r.getProposedBy().getId() : null)
                .proposedByName(r.getProposedBy() != null ? r.getProposedBy().getFullName() : null)
                .proposedDate(r.getProposedDate())
                .notes(r.getNotes())
                .status(r.getStatus())
                .facultyCounterDate(r.getFacultyCounterDate())
                .facultyCounterNotes(r.getFacultyCounterNotes())
                .agreedDate(r.getAgreedDate())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
