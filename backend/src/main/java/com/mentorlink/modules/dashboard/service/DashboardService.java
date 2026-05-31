package com.mentorlink.modules.dashboard.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mentorlink.modules.admin.service.AnalyticsService;
import com.mentorlink.modules.dashboard.dto.AdminDashboardDto;
import com.mentorlink.modules.dashboard.dto.AdminProfileDto;
import com.mentorlink.modules.dashboard.dto.FacultyDashboardDto;
import com.mentorlink.modules.dashboard.dto.FacultyProfileDto;
import com.mentorlink.modules.dashboard.dto.GroupSummaryDto;
import com.mentorlink.modules.dashboard.dto.MemberSummaryDto;
import com.mentorlink.modules.dashboard.dto.MentorSummaryDto;
import com.mentorlink.modules.dashboard.dto.ProjectSummaryDto;
import com.mentorlink.modules.dashboard.dto.StudentDashboardDto;
import com.mentorlink.modules.dashboard.dto.StudentProfileDto;
import com.mentorlink.modules.dashboard.dto.SubmissionSummaryDto;
import com.mentorlink.modules.faculty.entity.FacultyProfile;
import com.mentorlink.modules.faculty.repository.FacultyProfileRepository;
import com.mentorlink.modules.groups.entity.Group;
import com.mentorlink.modules.groups.repository.GroupRepository;
import com.mentorlink.modules.meetings.entity.Meeting;
import com.mentorlink.modules.meetings.repository.MeetingRepository;
import com.mentorlink.modules.notifications.service.NotificationService;
import com.mentorlink.modules.projects.entity.Project;
import com.mentorlink.modules.projects.repository.ProjectRepository;
import com.mentorlink.modules.submissions.entity.Submission;
import com.mentorlink.modules.submissions.repository.SubmissionRepository;
import com.mentorlink.modules.users.UserRepository;
import com.mentorlink.modules.users.entity.User;

import lombok.RequiredArgsConstructor;

/** Role-specific dashboard data: student, faculty, admin. */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final ProjectRepository projectRepository;
    private final SubmissionRepository submissionRepository;
    private final NotificationService notificationService;
    private final AnalyticsService analyticsService;
    private final ProfileService profileService;
    private final FacultyProfileRepository facultyProfileRepository;
    private final MeetingRepository meetingRepository;

    public StudentDashboardDto getStudentDashboard(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        StudentProfileDto profile = profileService.getStudentProfile(email);

        ProjectSummaryDto assignedProject = null;
        GroupSummaryDto groupSummary = null;
        List<SubmissionSummaryDto> mySubmissions = new ArrayList<>();

        List<Group> myGroups = groupRepository.findByMembersContaining(user);
        if (!myGroups.isEmpty()) {
            Group g = myGroups.get(0);
            Project p = g.getProject();
            if (p != null) {
                assignedProject = toProjectSummary(p);
                List<Submission> subs = submissionRepository.findByProjectOrderByCategory(p);
                mySubmissions = subs.stream()
                        .map(s -> SubmissionSummaryDto.builder()
                                .id(s.getId())
                                .projectId(p.getId())
                                .category(s.getCategory())
                                .originalFilename(s.getOriginalFilename())
                                .submittedAt(s.getCreatedAt())
                                .build())
                        .collect(Collectors.toList());
            }
            groupSummary = profile.getGroup();
        }

        long unread = notificationService.getUnreadCount(email);
        return StudentDashboardDto.builder()
                .profile(profile)
                .assignedProject(assignedProject)
                .group(groupSummary)
                .availableProjects(List.of())
                .mySubmissions(mySubmissions)
                .unreadNotificationCount(unread)
                .build();
    }

    public FacultyDashboardDto getFacultyDashboard(String email) {
        FacultyProfileDto profile = profileService.getFacultyProfile(email);
        List<ProjectSummaryDto> supervisedProjects = new ArrayList<>();
        List<GroupSummaryDto> assignedGroups = new ArrayList<>();

        FacultyProfile faculty = facultyProfileRepository.findByUser_Email(email).orElse(null);
        if (faculty != null) {
            List<Project> projects = projectRepository.findByMentor(faculty);
            for (Project p : projects) {
                supervisedProjects.add(toProjectSummary(p));
                if (p.getGroup() != null) {
                    assignedGroups.add(toGroupSummary(p.getGroup()));
                }
            }
        }

        long unread = notificationService.getUnreadCount(email);
        return FacultyDashboardDto.builder()
                .profile(profile)
                .supervisedProjects(supervisedProjects)
                .assignedGroups(assignedGroups)
                .unreadNotificationCount(unread)
                .build();
    }

    public AdminDashboardDto getAdminDashboard(String email) {
        AdminProfileDto profile = profileService.getAdminProfile(email);
        var analytics = analyticsService.getDashboard();
        long unread = notificationService.getUnreadCount(email);
        return AdminDashboardDto.builder()
                .profile(profile)
                .analytics(analytics)
                .unreadNotificationCount(unread)
                .build();
    }

    private ProjectSummaryDto toProjectSummary(Project p) {
        MentorSummaryDto mentor = null;
        if (p.getMentor() != null) {
            FacultyProfile m = p.getMentor();
            mentor = MentorSummaryDto.builder()
                    .facultyId(m.getId())
                    .name(m.getName())
                    .email(m.getEmail())
                    .department(m.getDepartment())
                    .expertise(m.getExpertise())
                    .build();
        }
        Meeting lastMeeting = meetingRepository.findFirstByProjectOrderByMeetingDateDesc(p).orElse(null);
        return ProjectSummaryDto.builder()
                .projectId(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .domain(p.getDomain())
                .progress(p.getProgress())
                .mentor(mentor)
                .groupId(p.getGroup() != null ? p.getGroup().getId() : null)
                .lastMeetingDate(lastMeeting != null ? lastMeeting.getMeetingDate() : null)
                .lastMeetingVerified(lastMeeting != null ? lastMeeting.isVerified() : null)
                .build();
    }

    private GroupSummaryDto toGroupSummary(Group g) {
        Project p = g.getProject();
        List<MemberSummaryDto> members = g.getMembers().stream()
                .map(m -> MemberSummaryDto.builder()
                        .userId(m.getId())
                        .fullName(m.getFullName())
                        .email(m.getEmail())
                        .isLeader(g.getLeader() != null && g.getLeader().getId().equals(m.getId()))
                        .build())
                .collect(Collectors.toList());
        return GroupSummaryDto.builder()
                .groupId(g.getId())
                .name(g.getName())
                .projectId(p != null ? p.getId() : null)
                .projectTitle(p != null ? p.getTitle() : null)
                .leaderId(g.getLeader() != null ? g.getLeader().getId() : null)
                .leaderName(g.getLeader() != null ? g.getLeader().getFullName() : null)
                .members(members)
                .memberCount(members.size())
                .build();
    }
}
