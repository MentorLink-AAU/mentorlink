package com.mentorlink.modules.admin.service;

import com.mentorlink.modules.faculty.repository.FacultyProfileRepository;
import com.mentorlink.modules.groups.repository.GroupRepository;
import com.mentorlink.modules.meetings.dto.LastMeetingByGroupDto;
import com.mentorlink.modules.meetings.service.MeetingService;
import com.mentorlink.modules.projects.repository.ProjectRepository;
import com.mentorlink.modules.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final ProjectRepository projectRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final MeetingService meetingService;

    public Map<String, Object> getDashboard() {
        Map<String, Object> m = new HashMap<>();
        long totalUsers = userRepository.count();
        long totalGroups = groupRepository.count();
        long totalProjects = projectRepository.count();
        long totalFaculty = facultyProfileRepository.count();

        long studentsWithGroups = userRepository.findAll().stream()
                .filter(u -> u.getRoles().contains("STUDENT"))
                .filter(s -> !groupRepository.findByMembersContaining(s).isEmpty())
                .count();
        long studentsWithoutGroups = userRepository.findAll().stream()
                .filter(u -> u.getRoles().contains("STUDENT"))
                .filter(s -> groupRepository.findByMembersContaining(s).isEmpty())
                .count();

        long projectsWithMentor = projectRepository.findAll().stream()
                .filter(p -> p.getMentor() != null)
                .count();

        m.put("totalUsers", totalUsers);
        m.put("totalGroups", totalGroups);
        m.put("totalProjects", totalProjects);
        m.put("totalFaculty", totalFaculty);
        m.put("studentsWithGroups", studentsWithGroups);
        m.put("studentsWithoutGroups", studentsWithoutGroups);
        m.put("projectsWithMentor", projectsWithMentor);
        m.put("projectsWithoutMentor", totalProjects - projectsWithMentor);

        List<LastMeetingByGroupDto> lastMeetingByGroup = meetingService.getLastMeetingByGroupForAdmin();
        m.put("lastMeetingByGroup", lastMeetingByGroup);

        return m;
    }
}
