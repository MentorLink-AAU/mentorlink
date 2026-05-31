package com.mentorlink.service;

import com.mentorlink.modules.admin.dto.AutoGroupResultDto;
import com.mentorlink.modules.deadlines.entity.DeadlineType;
import com.mentorlink.modules.deadlines.service.DeadlineService;
import com.mentorlink.modules.faculty.entity.FacultyProfile;
import com.mentorlink.modules.faculty.repository.FacultyProfileRepository;
import com.mentorlink.modules.groups.dto.GroupResponseDto;
import com.mentorlink.modules.groups.entity.Group;
import com.mentorlink.modules.groups.repository.GroupRepository;
import com.mentorlink.modules.groups.service.GroupService;
import com.mentorlink.modules.projects.entity.Project;
import com.mentorlink.modules.projects.repository.ProjectRepository;
import com.mentorlink.modules.recommender.dto.RecommenderJobResponseDto;
import com.mentorlink.modules.recommender.dto.RecommenderResultRowDto;
import com.mentorlink.modules.recommender.entity.RecommenderJob;
import com.mentorlink.modules.recommender.entity.RecommenderJobStatus;
import com.mentorlink.modules.recommender.entity.RecommenderProjectGroup;
import com.mentorlink.modules.recommender.repository.RecommenderJobRepository;
import com.mentorlink.modules.recommender.repository.RecommenderProjectGroupRepository;
import com.mentorlink.modules.students.entity.StudentProfile;
import com.mentorlink.modules.students.repository.StudentProfileRepository;
import com.mentorlink.modules.users.entity.User;
import com.mentorlink.modules.users.UserRepository;
import com.mentorlink.util.ExcelProcessor;
import com.mentorlink.util.LeftoverStudentRow;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommenderService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final ProjectRepository projectRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final DeadlineService deadlineService;
    private final ExcelProcessor excelProcessor;
    private final GroupService groupService;
    private final RecommenderJobRepository recommenderJobRepository;
    private final RecommenderProjectGroupRepository recommenderProjectGroupRepository;
    private final ExecutorService recommenderExecutor;

    @Value("${app.recommender.python:python}")
    private String pythonCommand;
    @Value("${app.recommender.matrix-script:D:/RECOMMANDOR SYSTEM/new_recommandor/analysis/matrix_factorization_recommender.py}")
    private String matrixScriptPath;
    @Value("${app.recommender.output:D:/RECOMMANDOR SYSTEM/new_recommandor/outputs/output_matrix_factorization.xlsx}")
    private String matrixOutputPath;
    @Value("${app.recommender.timeout-seconds:300}")
    private long timeoutSeconds;
    @Value("${app.recommender.work-dir:uploads/recommender}")
    private String recommenderWorkDir;

    /**
     * Auto-group ALL leftover students using Matrix Factorization recommender.
     * Run after GROUP_FORMATION deadline. One-click grouping of all students without groups.
     */
    @Transactional
    public AutoGroupResultDto autoGroupFromLeftover() {
        GroupingInput input = buildDatabaseAutoGroupInput();
        String jobId = "sync-" + UUID.randomUUID();
        return runSyncPipeline(jobId, input);
    }

    /**
     * Auto-group leftover students from Excel + optionally faculty Excel.
     * Students file: columns [Email, RollNumber]. Faculty file (optional): columns [Email, FullName, Department, Expertise, MaxGroups].
     * When faculty file is provided, only those faculty are used for assignment in the Matrix Factorization output application stage.
     */
    @Transactional
    public AutoGroupResultDto autoGroupFromExcel(MultipartFile studentsFile, MultipartFile facultyFile) {
        GroupingInput input = buildExcelInput(studentsFile, facultyFile);
        String jobId = "sync-" + UUID.randomUUID();
        return runSyncPipeline(jobId, input);
    }

    public String startMatrixFactorizationJobFromDatabase() {
        GroupingInput input = buildDatabaseAutoGroupInput();
        return submitAsyncJob(input);
    }

    @Transactional(readOnly = true)
    public RecommenderJobResponseDto getJobStatus(String jobId) {
        RecommenderJob job = recommenderJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));
        return RecommenderJobResponseDto.builder()
                .jobId(job.getJobId())
                .status(job.getStatus())
                .errorMessage(job.getErrorMessage())
                .startedAt(job.getStartedAt())
                .finishedAt(job.getFinishedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<RecommenderResultRowDto> getJobResult(String jobId) {
        RecommenderJob job = recommenderJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));
        if (job.getStatus() != RecommenderJobStatus.COMPLETED) {
            throw new IllegalStateException("Job is not completed yet. Current status: " + job.getStatus());
        }
        return recommenderProjectGroupRepository.findByJobIdOrderByIdAsc(jobId).stream()
                .map(row -> RecommenderResultRowDto.builder()
                        .groupId(row.getGroupId())
                        .students(resolveStudentNames(row))
                        .facultyName(row.getFaculty() != null ? row.getFaculty().getName() : null)
                        .similarityScore(row.getSimilarityScore())
                        .algorithmUsed(row.getAlgorithmUsed())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public byte[] downloadJobResultExcel(String jobId) throws Exception {
        RecommenderJob job = recommenderJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));
        if (job.getStatus() != RecommenderJobStatus.COMPLETED) {
            throw new IllegalStateException("Job is not completed yet. Current status: " + job.getStatus());
        }

        List<Map<String, Object>> rows = recommenderProjectGroupRepository.findByJobIdOrderByIdAsc(jobId).stream()
                .map(row -> {
                    Map<String, Object> values = new LinkedHashMap<>();
                    values.put("groupId", row.getGroupId());
                    values.put("student1", row.getStudent1() != null ? row.getStudent1().getFullName() : null);
                    values.put("student2", row.getStudent2() != null ? row.getStudent2().getFullName() : null);
                    values.put("student3", row.getStudent3() != null ? row.getStudent3().getFullName() : null);
                    values.put("facultyName", row.getFaculty() != null ? row.getFaculty().getName() : null);
                    values.put("similarityScore", row.getSimilarityScore());
                    values.put("algorithmUsed", row.getAlgorithmUsed());
                    return values;
                })
                .toList();

        return excelProcessor.generateAutoAllocationGroupsExcel(rows);
    }

    private String submitAsyncJob(GroupingInput input) {
        String jobId = UUID.randomUUID().toString();
        RecommenderJob job = RecommenderJob.builder()
                .jobId(jobId)
                .status(RecommenderJobStatus.RUNNING)
                .outputPath(matrixOutputPath)
                .build();
        recommenderJobRepository.save(job);
        recommenderExecutor.submit(() -> executeAsyncJob(jobId, input));
        return jobId;
    }

    private void executeAsyncJob(String jobId, GroupingInput input) {
        try {
            AutoGroupResultDto result = runSyncPipeline(jobId, input);
            if (result.getErrors() != null && !result.getErrors().isEmpty()) {
                updateJob(jobId, RecommenderJobStatus.FAILED, String.join("; ", result.getErrors()));
            } else {
                updateJob(jobId, RecommenderJobStatus.COMPLETED, null);
            }
        } catch (Exception ex) {
            updateJob(jobId, RecommenderJobStatus.FAILED, ex.getMessage());
        }
    }

    @Transactional
    protected void updateJob(String jobId, RecommenderJobStatus status, String errorMessage) {
        RecommenderJob job = recommenderJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found while updating: " + jobId));
        job.setStatus(status);
        job.setErrorMessage(errorMessage);
        recommenderJobRepository.save(job);
    }

    private GroupingInput buildDatabaseAutoGroupInput() {
        if (!deadlineService.isPastDeadline(DeadlineType.GROUP_FORMATION)) {
            throw new IllegalStateException(
                    "Group formation deadline has not passed. Auto-allocation is only available after the manual group formation deadline. Set the GROUP_FORMATION deadline in Admin > Deadlines.");
        }
        List<User> students = userRepository.findAll().stream()
                .filter(u -> u.getRoles().contains("STUDENT"))
                .filter(s -> groupRepository.findByMembersContaining(s).isEmpty())
                .toList();
        List<FacultyProfile> faculty = facultyProfileRepository.findAll();
        if (students.isEmpty()) {
            throw new IllegalStateException("No leftover students found in the database. Bulk upload students first, then run auto-allocation.");
        }
        if (faculty.isEmpty()) {
            throw new IllegalStateException("No faculty found in the database. Bulk upload faculty first, then run auto-allocation.");
        }
        return GroupingInput.builder()
                .students(students)
                .facultyProfiles(faculty)
                .studentsNotFound(new ArrayList<>())
                .studentsSkipped(new ArrayList<>())
                .errors(new ArrayList<>())
                .build();
    }

    private GroupingInput buildExcelInput(MultipartFile studentsFile, MultipartFile facultyFile) {
        List<String> studentsNotFound = new ArrayList<>();
        List<String> studentsSkipped = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        List<User> toGroup = new ArrayList<>();
        Set<String> facultyEmails = null;
        if (facultyFile != null && !facultyFile.isEmpty()) {
            try {
                List<ExcelProcessor.FacultyUploadRow> facultyRows = excelProcessor.parseFaculty(facultyFile);
                facultyEmails = new HashSet<>();
                for (ExcelProcessor.FacultyUploadRow fr : facultyRows) {
                    if (fr.getEmail() != null && !fr.getEmail().isBlank()) {
                        facultyEmails.add(fr.getEmail().trim().toLowerCase());
                    }
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Faculty Excel parse error: " + e.getMessage());
            }
        }

        try {
            List<LeftoverStudentRow> rows = excelProcessor.parseLeftoverStudents(studentsFile);
            for (LeftoverStudentRow row : rows) {
                User student = resolveStudent(row);
                if (student == null) {
                    String ident = row.getEmail() != null ? row.getEmail() : row.getRollNumber();
                    studentsNotFound.add(ident);
                    continue;
                }
                if (!student.getRoles().contains("STUDENT")) {
                    studentsSkipped.add(student.getEmail() + " (not a student)");
                    continue;
                }
                if (!groupRepository.findByMembersContaining(student).isEmpty()) {
                    studentsSkipped.add(student.getEmail() + " (already in a group)");
                    continue;
                }
                toGroup.add(student);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Excel parse error: " + e.getMessage());
        }

        if (!deadlineService.isPastDeadline(DeadlineType.GROUP_FORMATION)) {
            throw new IllegalStateException(
                    "Group formation deadline has not passed. Auto-allocation is only available after the manual group formation deadline. Set the GROUP_FORMATION deadline in Admin > Deadlines.");
        }

        List<FacultyProfile> allFaculty = facultyProfileRepository.findAll();
        final Set<String> facultyEmailFilter = facultyEmails;
        List<FacultyProfile> faculty = facultyEmailFilter == null || facultyEmailFilter.isEmpty()
                ? allFaculty
                : allFaculty.stream()
                .filter(f -> f.getEmail() != null && facultyEmailFilter.contains(f.getEmail().trim().toLowerCase()))
                .toList();

        return GroupingInput.builder()
                .students(toGroup)
                .facultyProfiles(faculty)
                .studentsNotFound(studentsNotFound)
                .studentsSkipped(studentsSkipped)
                .errors(errors)
                .build();
    }

    @Transactional
    private AutoGroupResultDto runSyncPipeline(String jobId, GroupingInput input) {
        List<String> errors = new ArrayList<>(input.errors);
        List<String> studentsNotFound = new ArrayList<>(input.studentsNotFound);
        List<String> studentsSkipped = new ArrayList<>(input.studentsSkipped);
        List<GroupResponseDto> createdGroups = new ArrayList<>();

        if (input.students.size() < 3) {
            errors.add("Need at least 3 students to form one group. Found: " + input.students.size());
            return AutoGroupResultDto.builder()
                    .groupsCreated(0)
                    .studentsGrouped(0)
                    .facultyAssigned(0)
                    .studentsNotFound(studentsNotFound)
                    .studentsSkipped(studentsSkipped)
                    .errors(errors)
                    .createdGroups(List.of())
                    .build();
        }

        try {
            Path workDir = ensureWorkDir();
            Path studentsPath = workDir.resolve("students-" + jobId + ".xlsx");
            Path facultyPath = workDir.resolve("faculty-" + jobId + ".xlsx");
            Path outputPath = Paths.get(matrixOutputPath);
            if (outputPath.getParent() != null) {
                Files.createDirectories(outputPath.getParent());
            }

            writeStudentsExcel(studentsPath, input.students);
            writeFacultyExcel(facultyPath, input.facultyProfiles);
            executeMatrixFactorization(studentsPath, facultyPath, outputPath);

            recommenderProjectGroupRepository.saveAll(
                    applyOutputToSystem(jobId, outputPath, input.facultyProfiles, createdGroups, studentsSkipped, errors)
            );
        } catch (Exception ex) {
            errors.add(ex.getMessage());
        }

        int studentsGrouped = createdGroups.stream().mapToInt(GroupResponseDto::getMemberCount).sum();
        int facultyAssigned = (int) createdGroups.stream().filter(g -> g.getMentorName() != null && !g.getMentorName().isBlank()).count();

        return AutoGroupResultDto.builder()
                .groupsCreated(createdGroups.size())
                .studentsGrouped(studentsGrouped)
                .facultyAssigned(facultyAssigned)
                .studentsNotFound(studentsNotFound)
                .studentsSkipped(studentsSkipped)
                .errors(errors.isEmpty() ? List.of() : errors)
                .createdGroups(createdGroups)
                .build();
    }

    private List<RecommenderProjectGroup> applyOutputToSystem(
            String jobId,
            Path outputPath,
            List<FacultyProfile> allowedFaculty,
            List<GroupResponseDto> createdGroups,
            List<String> studentsSkipped,
            List<String> errors
    ) throws Exception {
        List<OutputRow> rows = readOutputRows(outputPath);
        List<RecommenderProjectGroup> persistedRows = new ArrayList<>();

        for (OutputRow row : rows) {
            try {
                List<User> members = resolveStudents(row.students());
                if (members.size() < 3) {
                    errors.add("Skipping " + row.groupId() + ": expected 3 valid students.");
                    continue;
                }
                if (members.stream().anyMatch(s -> !groupRepository.findByMembersContaining(s).isEmpty())) {
                    studentsSkipped.add(row.groupId() + " skipped: one or more students already in a group");
                    continue;
                }
                User leader = members.get(0);
                Project project = Project.builder()
                        .title("Auto-Group Project " + row.groupId())
                        .description("Auto-assigned group (Matrix Factorization)")
                        .domain("General")
                        .techStack("TBD")
                        .progress(0)
                        .build();
                project = projectRepository.save(project);

                Group group = Group.builder()
                        .name("Group-" + project.getId())
                        .project(project)
                        .leader(leader)
                        .joinToken(UUID.randomUUID().toString())
                        .mentorJoinToken(UUID.randomUUID().toString())
                        .build();
                group.getMembers().addAll(members);
                project.setGroup(group);
                group.setProject(project);
                Group saved = groupRepository.save(group);

                FacultyProfile faculty = resolveFaculty(row.facultyName(), allowedFaculty);
                if (faculty != null && faculty.getCurrentLoad() < faculty.getMaxGroups()) {
                    project.setMentor(faculty);
                    faculty.setCurrentLoad(faculty.getCurrentLoad() + 1);
                    projectRepository.save(project);
                    facultyProfileRepository.save(faculty);
                }

                createdGroups.add(groupService.getByIdForSystem(saved.getId()));
                persistedRows.add(RecommenderProjectGroup.builder()
                        .jobId(jobId)
                        .groupId(row.groupId())
                        .student1(members.get(0))
                        .student2(members.get(1))
                        .student3(members.get(2))
                        .faculty(faculty)
                        .similarityScore(row.similarityScore())
                        .algorithmUsed(row.algorithmUsed())
                        .build());
            } catch (Exception e) {
                errors.add("Group creation failed for " + row.groupId() + ": " + e.getMessage());
            }
        }
        return persistedRows;
    }

    private FacultyProfile resolveFaculty(String facultyNameOrEmail, List<FacultyProfile> allowedFaculty) {
        if (facultyNameOrEmail == null || facultyNameOrEmail.isBlank()) {
            return null;
        }
        String needle = facultyNameOrEmail.trim().toLowerCase();
        if (needle.contains("@")) {
            Optional<FacultyProfile> byEmail = allowedFaculty.stream()
                    .filter(f -> f.getEmail() != null && f.getEmail().trim().equalsIgnoreCase(needle))
                    .findFirst();
            if (byEmail.isPresent()) return byEmail.get();
            return facultyProfileRepository.findByEmailIgnoreCase(needle).orElse(null);
        }
        Optional<FacultyProfile> byName = allowedFaculty.stream()
                .filter(f -> f.getName() != null && f.getName().trim().equalsIgnoreCase(needle))
                .findFirst();
        if (byName.isPresent()) return byName.get();
        return facultyProfileRepository.findByNameIgnoreCase(needle).orElse(null);
    }

    private List<User> resolveStudents(List<String> studentTokens) {
        List<User> users = new ArrayList<>();
        for (String token : studentTokens) {
            if (token == null || token.isBlank()) {
                continue;
            }
            String trimmed = token.trim();
            Optional<User> resolved = trimmed.contains("@")
                    ? userRepository.findByEmailIgnoreCase(trimmed)
                    : userRepository.findByFullNameIgnoreCase(trimmed);
            resolved.ifPresent(users::add);
        }
        return users;
    }

    private List<OutputRow> readOutputRows(Path outputPath) throws Exception {
        if (!Files.exists(outputPath)) {
            throw new IllegalStateException("Output file not found: " + outputPath);
        }
        List<OutputRow> rows = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(Files.newInputStream(outputPath))) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() < 1) {
                return rows;
            }
            Map<String, Integer> headerIndex = new HashMap<>();
            Row header = sheet.getRow(0);
            for (int i = 0; i < header.getLastCellNum(); i++) {
                if (header.getCell(i) == null) continue;
                headerIndex.put(header.getCell(i).getStringCellValue().trim().toLowerCase(), i);
            }
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String groupId = cellString(row, headerIndex, "group_id");
                if (groupId == null || groupId.isBlank()) continue;
                String student1 = cellString(row, headerIndex, "student1");
                String student2 = cellString(row, headerIndex, "student2");
                String student3 = cellString(row, headerIndex, "student3");
                String faculty = cellString(row, headerIndex, "faculty_name");
                Double score = cellDouble(row, headerIndex, "similarity_score");
                String algorithm = Optional.ofNullable(cellString(row, headerIndex, "algorithm_used"))
                        .filter(s -> !s.isBlank())
                        .orElse("Matrix Factorization");
                rows.add(new OutputRow(groupId, List.of(student1, student2, student3), faculty, score, algorithm));
            }
        }
        return rows;
    }

    private String cellString(Row row, Map<String, Integer> headerIndex, String key) {
        Integer idx = headerIndex.get(key);
        if (idx == null || row.getCell(idx) == null) return null;
        return switch (row.getCell(idx).getCellType()) {
            case STRING -> row.getCell(idx).getStringCellValue();
            case NUMERIC -> String.valueOf((long) row.getCell(idx).getNumericCellValue());
            default -> null;
        };
    }

    private Double cellDouble(Row row, Map<String, Integer> headerIndex, String key) {
        Integer idx = headerIndex.get(key);
        if (idx == null || row.getCell(idx) == null) return null;
        return switch (row.getCell(idx).getCellType()) {
            case NUMERIC -> row.getCell(idx).getNumericCellValue();
            case STRING -> {
                try {
                    yield Double.parseDouble(row.getCell(idx).getStringCellValue());
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
            default -> null;
        };
    }

    private void executeMatrixFactorization(Path studentsPath, Path facultyPath, Path outputPath) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                pythonCommand,
                matrixScriptPath,
                studentsPath.toAbsolutePath().toString(),
                facultyPath.toAbsolutePath().toString(),
                outputPath.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder logs = new StringBuilder();
        Thread logDrainer = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logs.append(line).append(System.lineSeparator());
                }
            } catch (IOException ignored) {
                // logging fallback not required
            }
        });
        logDrainer.start();

        boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            logDrainer.join(3000);
            throw new IllegalStateException("Matrix Factorization recommender timed out after " + timeoutSeconds + " seconds.");
        }
        logDrainer.join(3000);

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            throw new IllegalStateException("Matrix Factorization recommender failed (exit " + exitCode + "): " + logs);
        }
    }

    private Path ensureWorkDir() throws IOException {
        Path path = Paths.get(recommenderWorkDir);
        if (!path.isAbsolute()) {
            path = Paths.get(".").resolve(path).normalize();
        }
        Files.createDirectories(path);
        return path;
    }

    private void writeStudentsExcel(Path filePath, List<User> students) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Students");
            Row header = sheet.createRow(0);
            String[] headers = {"Email", "FullName", "RollNumber", "Department", "YearOfStudy", "Skills"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            int rowNum = 1;
            for (User user : students) {
                StudentProfile sp = studentProfileRepository.findByUser_Id(user.getId()).orElse(null);
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(nonNull(user.getEmail()));
                row.createCell(1).setCellValue(nonNull(user.getFullName()));
                row.createCell(2).setCellValue(sp != null ? nonNull(sp.getRollNumber()) : "");
                row.createCell(3).setCellValue(sp != null ? nonNull(sp.getDepartment()) : "");
                if (sp != null && sp.getYearOfStudy() != null) {
                    row.createCell(4).setCellValue(sp.getYearOfStudy());
                } else {
                    row.createCell(4).setCellValue("");
                }
                row.createCell(5).setCellValue(String.join(", ", normalizeSkills(user.getSkills())));
            }
            workbook.write(out);
            Files.write(filePath, out.toByteArray());
        }
    }

    private void writeFacultyExcel(Path filePath, List<FacultyProfile> faculty) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Faculty");
            Row header = sheet.createRow(0);
            String[] headers = {"Email", "FullName", "Department", "Expertise", "MaxGroups"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            int rowNum = 1;
            for (FacultyProfile f : faculty) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(nonNull(f.getEmail()));
                row.createCell(1).setCellValue(nonNull(f.getName()));
                row.createCell(2).setCellValue(nonNull(f.getDepartment()));
                row.createCell(3).setCellValue(nonNull(f.getExpertise()));
                row.createCell(4).setCellValue(Math.max(1, f.getMaxGroups()));
            }
            workbook.write(out);
            Files.write(filePath, out.toByteArray());
        }
    }

    private Set<String> normalizeSkills(Set<String> skills) {
        if (skills == null) return Set.of();
        return skills.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String nonNull(String value) {
        return value == null ? "" : value;
    }

    private List<String> resolveStudentNames(RecommenderProjectGroup row) {
        List<String> students = new ArrayList<>();
        if (row.getStudent1() != null) students.add(row.getStudent1().getFullName());
        if (row.getStudent2() != null) students.add(row.getStudent2().getFullName());
        if (row.getStudent3() != null) students.add(row.getStudent3().getFullName());
        return students;
    }

    private User resolveStudent(LeftoverStudentRow row) {
        if (row.getEmail() != null && !row.getEmail().isBlank()) {
            return userRepository.findByEmailIgnoreCase(row.getEmail()).orElse(null);
        }
        if (row.getRollNumber() != null && !row.getRollNumber().isBlank()) {
            return studentProfileRepository.findByRollNumber(row.getRollNumber())
                    .map(StudentProfile::getUser)
                    .orElse(null);
        }
        return null;
    }

    private record OutputRow(
            String groupId,
            List<String> students,
            String facultyName,
            Double similarityScore,
            String algorithmUsed
    ) {
    }

    @lombok.Builder
    private static class GroupingInput {
        private List<User> students;
        private List<FacultyProfile> facultyProfiles;
        private List<String> studentsNotFound;
        private List<String> studentsSkipped;
        private List<String> errors;
    }
}
