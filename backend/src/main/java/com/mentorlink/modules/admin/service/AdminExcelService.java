package com.mentorlink.modules.admin.service;

import com.mentorlink.modules.faculty.entity.FacultyProfile;
import com.mentorlink.modules.faculty.repository.FacultyProfileRepository;
import com.mentorlink.modules.students.entity.StudentProfile;
import com.mentorlink.modules.students.repository.StudentProfileRepository;
import com.mentorlink.modules.users.entity.User;
import com.mentorlink.modules.users.UserRepository;
import com.mentorlink.util.ExcelProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminExcelService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final ExcelProcessor excelProcessor;
    private final PasswordEncoder passwordEncoder;

    public ExcelUploadResult uploadStudents(MultipartFile file) {
        List<String> created = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try {
            List<ExcelProcessor.StudentUploadRow> rows = excelProcessor.parseStudents(file);
            for (ExcelProcessor.StudentUploadRow row : rows) {
                try {
                    Optional<User> existing = userRepository.findByEmail(row.getEmail());
                    if (existing.isPresent()) {
                        skipped.add(row.getEmail() + " (already exists)");
                        continue;
                    }
                    String plainPassword = (row.getPassword() != null && !row.getPassword().isBlank())
                            ? row.getPassword()
                            : "oauth2-" + UUID.randomUUID();
                    User user = new User();
                    user.setEmail(row.getEmail());
                    user.setFullName(row.getFullName() != null ? row.getFullName() : row.getEmail());
                    user.setPassword(passwordEncoder.encode(plainPassword));
                    user.setFirstLogin(true);  // default password: must change on first login
                    if (row.getSkills() != null && !row.getSkills().isEmpty()) {
                        user.setSkills(row.getSkills());
                    }
                    user.getRoles().add("STUDENT");
                    user = userRepository.save(user);

                    StudentProfile profile = StudentProfile.builder()
                            .user(user)
                            .rollNumber(row.getRollNumber())
                            .department(row.getDepartment())
                            .yearOfStudy(row.getYearOfStudy())
                            .build();
                    studentProfileRepository.save(profile);
                    created.add(row.getEmail());
                } catch (Exception e) {
                    errors.add(row.getEmail() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errors.add("Parse error: " + e.getMessage());
        }

        return new ExcelUploadResult(created.size(), skipped.size(), errors, created, skipped);
    }

    public ExcelUploadResult uploadFaculty(MultipartFile file) {
        List<String> created = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try {
            List<ExcelProcessor.FacultyUploadRow> rows = excelProcessor.parseFaculty(file);
            for (ExcelProcessor.FacultyUploadRow row : rows) {
                try {
                    Optional<User> existing = userRepository.findByEmail(row.getEmail());
                    if (existing.isPresent()) {
                        skipped.add(row.getEmail() + " (already exists)");
                        continue;
                    }
                    String plainPassword = (row.getPassword() != null && !row.getPassword().isBlank())
                            ? row.getPassword()
                            : "oauth2-" + UUID.randomUUID();
                    User user = new User();
                    user.setEmail(row.getEmail());
                    user.setFullName(row.getFullName() != null ? row.getFullName() : row.getEmail());
                    user.setPassword(passwordEncoder.encode(plainPassword));
                    user.setFirstLogin(true);  // default password: must change on first login
                    user.getRoles().add("FACULTY");
                    user = userRepository.save(user);

                    FacultyProfile profile = FacultyProfile.builder()
                            .user(user)
                            .name(row.getFullName() != null ? row.getFullName() : row.getEmail())
                            .email(row.getEmail())
                            .department(row.getDepartment())
                            .expertise(row.getExpertise())
                            .maxGroups(row.getMaxGroups())
                            .currentLoad(0)
                            .build();
                    facultyProfileRepository.save(profile);
                    created.add(row.getEmail());
                } catch (Exception e) {
                    errors.add(row.getEmail() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errors.add("Parse error: " + e.getMessage());
        }

        return new ExcelUploadResult(created.size(), skipped.size(), errors, created, skipped);
    }

    public record ExcelUploadResult(int created, int skipped, List<String> errors, List<String> createdEmails, List<String> skippedEmails) {}
}
