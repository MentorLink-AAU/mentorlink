package com.mentorlink.modules.admin;

import com.mentorlink.common.dto.ApiResponse;
import com.mentorlink.modules.admin.service.AdminExcelService;
import com.mentorlink.modules.admin.service.AdminResetService;
import com.mentorlink.modules.admin.service.AnalyticsService;
import com.mentorlink.modules.deadlines.entity.Deadline;
import com.mentorlink.modules.deadlines.entity.DeadlineType;
import com.mentorlink.modules.deadlines.service.DeadlineService;
import com.mentorlink.modules.faculty.entity.FacultyProfile;
import com.mentorlink.modules.faculty.repository.FacultyProfileRepository;
import com.mentorlink.modules.admin.dto.AdminGroupProgressDto;
import com.mentorlink.modules.admin.dto.AutoGroupResultDto;
import com.mentorlink.modules.groups.entity.Group;
import com.mentorlink.modules.groups.repository.GroupRepository;
import com.mentorlink.modules.meetings.dto.LastMeetingByGroupDto;
import com.mentorlink.modules.meetings.service.MeetingService;
import com.mentorlink.modules.projects.entity.Project;
import com.mentorlink.modules.projects.repository.ProjectRepository;
import com.mentorlink.service.RecommenderService;
import com.mentorlink.modules.users.UserRepository;
import com.mentorlink.modules.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mentorlink.util.ExcelProcessor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProjectRepository projectRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final AdminExcelService adminExcelService;
    private final AdminResetService adminResetService;
    private final DeadlineService deadlineService;
    private final RecommenderService recommenderService;
    private final AnalyticsService analyticsService;
    private final MeetingService meetingService;
    private final ExcelProcessor excelProcessor;

    // ========== Excel Upload ==========
    @PostMapping("/upload/students")
    public ResponseEntity<ApiResponse<AdminExcelService.ExcelUploadResult>> uploadStudents(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(adminExcelService.uploadStudents(file)));
    }

    @PostMapping("/upload/faculty")
    public ResponseEntity<ApiResponse<AdminExcelService.ExcelUploadResult>> uploadFaculty(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(adminExcelService.uploadFaculty(file)));
    }

    @DeleteMapping("/reset-yearly-data")
    public ResponseEntity<ApiResponse<String>> resetYearlyData() {
        adminResetService.resetYearlyData();
        return ResponseEntity.ok(ApiResponse.success("Yearly data reset successfully. Admin accounts were preserved."));
    }

    // ========== Deadline Management ==========
    @PostMapping("/deadlines")
    public ResponseEntity<ApiResponse<Object>> setDeadline(
            @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String typeStr = (String) body.get("type");
        String dueDateStr = (String) body.get("dueDate");
        DeadlineType type = DeadlineType.valueOf(typeStr);
        Instant dueDate = Instant.parse(dueDateStr);
        return ResponseEntity.ok(ApiResponse.success(deadlineService.createOrUpdate(name, dueDate, type)));
    }

    @GetMapping("/deadlines")
    public ResponseEntity<ApiResponse<List<Deadline>>> getDeadlines() {
        return ResponseEntity.ok(ApiResponse.success(deadlineService.getAll()));
    }

    @PutMapping("/deadlines/{id}/extend")
    public ResponseEntity<ApiResponse<Deadline>> extendDeadline(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Instant newDueDate = Instant.parse(body.get("dueDate"));
        return ResponseEntity.ok(ApiResponse.success(deadlineService.extendDeadline(id, newDueDate)));
    }

    // ========== Leftover Students (before auto-group) ==========
    @GetMapping("/students/without-group/export")
    public ResponseEntity<byte[]> exportLeftoverStudentsExcel() throws Exception {
        List<Map<String, Object>> students = userRepository.findAll().stream()
                .filter(u -> u.getRoles().contains("STUDENT"))
                .filter(s -> groupRepository.findByMembersContaining(s).isEmpty())
                .map(u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("email", u.getEmail());
                    m.put("fullName", u.getFullName());
                    m.put("skills", u.getSkills());
                    var sp = u.getStudentProfile();
                    m.put("rollNumber", sp != null ? sp.getRollNumber() : null);
                    m.put("department", sp != null ? sp.getDepartment() : null);
                    m.put("yearOfStudy", sp != null ? sp.getYearOfStudy() : null);
                    return m;
                })
                .toList();
        byte[] bytes = excelProcessor.generateLeftoverStudentsExcel(students);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"leftover-students.xlsx\"")
                .body(bytes);
    }

    @GetMapping("/faculty/export")
    public ResponseEntity<byte[]> exportFacultyExcel() throws Exception {
        List<Map<String, Object>> faculty = facultyProfileRepository.findAll().stream()
                .map(f -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("email", f.getEmail());
                    m.put("fullName", f.getName());
                    m.put("department", f.getDepartment());
                    m.put("expertise", f.getExpertise());
                    m.put("maxGroups", f.getMaxGroups());
                    m.put("currentLoad", f.getCurrentLoad());
                    m.put("availableSlots", Math.max(0, f.getMaxGroups() - f.getCurrentLoad()));
                    return m;
                })
                .toList();
        byte[] bytes = excelProcessor.generateFacultyExcel(faculty);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"faculty-list.xlsx\"")
                .body(bytes);
    }

    @GetMapping("/students/without-group")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> studentsWithoutGroup() {
        List<User> students = userRepository.findAll().stream()
                .filter(u -> u.getRoles().contains("STUDENT"))
                .filter(s -> groupRepository.findByMembersContaining(s).isEmpty())
                .toList();
        List<Map<String, Object>> list = students.stream()
                .map(u -> {
                    Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", u.getId());
                    m.put("email", u.getEmail());
                    m.put("fullName", u.getFullName());
                    m.put("skills", u.getSkills());
                    var sp = u.getStudentProfile();
                    m.put("rollNumber", sp != null ? sp.getRollNumber() : null);
                    m.put("department", sp != null ? sp.getDepartment() : null);
                    m.put("yearOfStudy", sp != null ? sp.getYearOfStudy() : null);
                    return m;
                })
                .toList();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    // ========== Auto Group Formation (after GROUP_FORMATION deadline) ==========
    /**
     * One-click: Auto-group ALL leftover students and assign faculty.
     * Uses cosine similarity for student clustering and faculty matching.
     */
    @PostMapping("/auto-group/from-leftover")
    public ResponseEntity<ApiResponse<AutoGroupResultDto>> autoGroupFromLeftover() {
        return ResponseEntity.ok(ApiResponse.success(recommenderService.autoGroupFromLeftover()));
    }

    /**
     * Auto-group from Excel. Students file (required), faculty file (optional).
     * Students: [Email, RollNumber]. Faculty: [Email, FullName, Department, Expertise, MaxGroups].
     */
    @PostMapping("/auto-group/from-excel")
    public ResponseEntity<ApiResponse<AutoGroupResultDto>> autoGroupFromExcel(
            @RequestParam("studentsFile") MultipartFile studentsFile,
            @RequestParam(value = "facultyFile", required = false) MultipartFile facultyFile) {
        return ResponseEntity.ok(ApiResponse.success(recommenderService.autoGroupFromExcel(studentsFile, facultyFile)));
    }

    // ========== Manual Assignment ==========
    @PostMapping("/projects/{projectId}/assign/{facultyId}")
    public ResponseEntity<ApiResponse<String>> assignFacultyToProject(
            @PathVariable Long projectId,
            @PathVariable Long facultyId
    ) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User facultyUser = userRepository.findById(facultyId)
                .orElseThrow(() -> new RuntimeException("Faculty not found"));

        if (!facultyUser.getRoles().contains("FACULTY")) {
            throw new RuntimeException("User is not a faculty");
        }

        FacultyProfile profile = facultyUser.getFacultyProfile();
        if (profile == null) {
            throw new RuntimeException("Faculty profile not created");
        }

        if (profile.getCurrentLoad() >= profile.getMaxGroups()) {
            throw new RuntimeException("Faculty slots are full (" + profile.getCurrentLoad() + "/" + profile.getMaxGroups() + "). No access to assign this faculty.");
        }

        // ✅ assign faculty as project mentor
        project.setMentor(profile);
        projectRepository.save(project);

        // update faculty load
        profile.setCurrentLoad(profile.getCurrentLoad() + 1);
        facultyProfileRepository.save(profile);

        return ResponseEntity.ok(ApiResponse.success(
                "Faculty assigned successfully to project: " + project.getTitle()
        ));
    }

    @PostMapping("/projects/{projectId}/unassign")
    public ResponseEntity<ApiResponse<String>> unassignFaculty(@PathVariable Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (project.getMentor() == null) {
            throw new RuntimeException("Project has no mentor");
        }
        var mentor = project.getMentor();
        mentor.setCurrentLoad(Math.max(0, mentor.getCurrentLoad() - 1));
        project.setMentor(null);
        projectRepository.save(project);
        facultyProfileRepository.save(mentor);
        return ResponseEntity.ok(ApiResponse.success("Faculty unassigned"));
    }

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> analytics() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getDashboard()));
    }

    /** Admin: Get single group with progress, members, mentor, last meeting. */
    @GetMapping("/groups/{groupId}")
    public ResponseEntity<ApiResponse<AdminGroupProgressDto>> getGroupDetail(@PathVariable Long groupId) {
        Group g = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        List<LastMeetingByGroupDto> lastMeetings = meetingService.getLastMeetingByGroupForAdmin();
        LastMeetingByGroupDto lastMeeting = lastMeetings.stream()
                .filter(m -> m.getGroupId().equals(groupId))
                .findFirst()
                .orElse(null);
        return ResponseEntity.ok(ApiResponse.success(toAdminGroupProgressDto(g, lastMeeting)));
    }

    /** Admin: List all groups with progress, members, mentor, and last meeting. Optional search by name/project/member. */
    @GetMapping("/groups")
    public ResponseEntity<ApiResponse<List<AdminGroupProgressDto>>> getAllGroupsWithProgress(
            @RequestParam(required = false) String search) {
        List<Group> groups = groupRepository.findAll();
        List<LastMeetingByGroupDto> lastMeetings = meetingService.getLastMeetingByGroupForAdmin();
        Map<Long, LastMeetingByGroupDto> meetingByGroup = new HashMap<>();
        for (LastMeetingByGroupDto m : lastMeetings) {
            meetingByGroup.put(m.getGroupId(), m);
        }
        String searchLower = (search != null && !search.isBlank()) ? search.trim().toLowerCase() : null;
        List<AdminGroupProgressDto> result = groups.stream()
                .map(g -> toAdminGroupProgressDto(g, meetingByGroup.get(g.getId())))
                .filter(dto -> searchLower == null || matchesSearch(dto, searchLower))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    private AdminGroupProgressDto toAdminGroupProgressDto(Group g, LastMeetingByGroupDto lastMeeting) {
        Project p = g.getProject();
        int progress = (p != null) ? p.getProgress() : 0;
        String projectTitle = (p != null) ? p.getTitle() : null;
        String projectDesc = (p != null) ? p.getDescription() : null;
        String mentorName = (p != null && p.getMentor() != null) ? p.getMentor().getName() : null;
        String mentorEmail = (p != null && p.getMentor() != null) ? p.getMentor().getEmail() : null;
        List<AdminGroupProgressDto.MemberInfo> members = g.getMembers().stream()
                .map(m -> AdminGroupProgressDto.MemberInfo.builder()
                        .userId(m.getId())
                        .fullName(m.getFullName())
                        .email(m.getEmail())
                        .isLeader(g.getLeader() != null && g.getLeader().getId().equals(m.getId()))
                        .build())
                .toList();
        return AdminGroupProgressDto.builder()
                .groupId(g.getId())
                .groupName(g.getName())
                .projectId(p != null ? p.getId() : null)
                .projectTitle(projectTitle)
                .projectDescription(projectDesc)
                .progress(progress)
                .memberCount(members.size())
                .members(members)
                .mentorName(mentorName)
                .mentorEmail(mentorEmail)
                .lastMeetingDate(lastMeeting != null ? lastMeeting.getLastMeetingDate() : null)
                .lastMeetingDetails(lastMeeting != null ? lastMeeting.getLastMeetingDetails() : null)
                .lastMeetingVerified(lastMeeting != null ? lastMeeting.isVerified() : null)
                .build();
    }

    private boolean matchesSearch(AdminGroupProgressDto dto, String search) {
        if (dto.getGroupName() != null && dto.getGroupName().toLowerCase().contains(search)) return true;
        if (dto.getProjectTitle() != null && dto.getProjectTitle().toLowerCase().contains(search)) return true;
        if (dto.getMentorName() != null && dto.getMentorName().toLowerCase().contains(search)) return true;
        if (dto.getMembers() != null) {
            for (AdminGroupProgressDto.MemberInfo m : dto.getMembers()) {
                if ((m.getFullName() != null && m.getFullName().toLowerCase().contains(search))
                        || (m.getEmail() != null && m.getEmail().toLowerCase().contains(search)))
                    return true;
            }
        }
        return false;
    }

    @PutMapping("/faculty/{facultyId}/max-groups")
    public ResponseEntity<ApiResponse<FacultyProfile>> setFacultyMaxGroups(
            @PathVariable Long facultyId,
            @RequestBody Map<String, Integer> body) {
        FacultyProfile faculty = facultyProfileRepository.findById(facultyId)
                .orElseThrow(() -> new RuntimeException("Faculty not found"));
        int max = body.getOrDefault("maxGroups", 3);
        faculty.setMaxGroups(Math.max(1, Math.min(10, max)));
        return ResponseEntity.ok(ApiResponse.success(facultyProfileRepository.save(faculty)));
    }
}
