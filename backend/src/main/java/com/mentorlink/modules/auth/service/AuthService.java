package com.mentorlink.modules.auth.service;

import com.mentorlink.common.dto.ApiResponse;
import com.mentorlink.modules.auth.dto.ChangePasswordRequest;
import com.mentorlink.modules.auth.dto.LoginRequest;
import com.mentorlink.modules.auth.dto.RegisterAdminRequest;
import com.mentorlink.modules.auth.dto.RegisterFacultyRequest;
import com.mentorlink.modules.auth.dto.RegisterStudentRequest;
import com.mentorlink.modules.users.dto.UserResponseDto;
import com.mentorlink.modules.users.entity.User;
import com.mentorlink.modules.users.UserRepository;
import com.mentorlink.modules.faculty.entity.FacultyProfile;
import com.mentorlink.modules.faculty.repository.FacultyProfileRepository;
import com.mentorlink.modules.students.entity.StudentProfile;
import com.mentorlink.modules.students.repository.StudentProfileRepository;
import com.mentorlink.service.EmailNotificationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/** Registration (student/faculty/admin) and login logic. */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailNotificationService emailNotificationService;

    public AuthService(UserRepository userRepository,
                       FacultyProfileRepository facultyProfileRepository,
                       StudentProfileRepository studentProfileRepository,
                       PasswordEncoder passwordEncoder,
                       EmailNotificationService emailNotificationService) {
        this.userRepository = userRepository;
        this.facultyProfileRepository = facultyProfileRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailNotificationService = emailNotificationService;
    }

    // ✅ Register Student
    public ApiResponse<UserResponseDto> registerStudent(RegisterStudentRequest dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists with email: " + dto.getEmail());
        }

        User user = User.builder()
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .password(passwordEncoder.encode(dto.getPassword()))
                .firstLogin(false)  // self-registration: user chose password, no forced change
                .build();
        user.getRoles().add("STUDENT");
        user = userRepository.save(user);

        StudentProfile profile = StudentProfile.builder()
                .user(user)
                .rollNumber(dto.getRollNumber())
                .department(dto.getDepartment())
                .yearOfStudy(dto.getYearOfStudy())
                .build();
        profile = Objects.requireNonNull(studentProfileRepository.save(profile), "saved student profile");

        // Non-blocking welcome email
        emailNotificationService.sendRegistrationWelcome(user.getEmail(), "STUDENT");

        return ApiResponse.success(UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role("STUDENT")
                .rollNumber(profile.getRollNumber())
                .department(profile.getDepartment())
                .yearOfStudy(profile.getYearOfStudy())
                .skills(user.getSkills() != null ? List.copyOf(user.getSkills()) : List.of())
                .achievements(user.getAchievements() != null ? List.copyOf(user.getAchievements()) : List.of())
                .build());
    }

    // ✅ Register Faculty
    public ApiResponse<UserResponseDto> registerFaculty(RegisterFacultyRequest dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists with email: " + dto.getEmail());
        }

        User user = User.builder()
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .password(passwordEncoder.encode(dto.getPassword()))
                .firstLogin(false)  // self-registration
                .build();
        user.getRoles().add("FACULTY");
        user = userRepository.save(user);

        FacultyProfile profile = FacultyProfile.builder()
                .user(user)
                .name(dto.getFullName())
                .email(dto.getEmail())
                .department(dto.getDepartment())
                .expertise(dto.getExpertise())
                .maxGroups(dto.getMaxGroups())
                .build();
        profile = Objects.requireNonNull(facultyProfileRepository.save(profile), "saved faculty profile");

        // Non-blocking welcome email
        emailNotificationService.sendRegistrationWelcome(user.getEmail(), "FACULTY");

        return ApiResponse.success(UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role("FACULTY")
                .department(profile.getDepartment())
                .skills(user.getSkills() != null ? List.copyOf(user.getSkills()) : List.of())
                .achievements(user.getAchievements() != null ? List.copyOf(user.getAchievements()) : List.of())
                .build());
    }

    // ✅ Register Admin
    public ApiResponse<UserResponseDto> registerAdmin(RegisterAdminRequest dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists with email: " + dto.getEmail());
        }

        User user = User.builder()
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .password(passwordEncoder.encode(dto.getPassword()))
                .firstLogin(false)  // self-registration
                .build();
        user.getRoles().add("ADMIN");
        user = userRepository.save(user);

        // Non-blocking welcome email
        emailNotificationService.sendRegistrationWelcome(user.getEmail(), "ADMIN");

        return ApiResponse.success(UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role("ADMIN")
                .skills(user.getSkills() != null ? List.copyOf(user.getSkills()) : List.of())
                .achievements(user.getAchievements() != null ? List.copyOf(user.getAchievements()) : List.of())
                .build());
    }

    // ✅ Login User (legacy; controller uses AuthenticationManager and returns LoginResponse)
    public ApiResponse<String> login(LoginRequest dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        return ApiResponse.success("dummy-jwt-token-for-" + user.getEmail());
    }

    /**
     * Change password (first-time or forced). Validates current password, then sets new password
     * and sets firstLogin = false so user can access the app.
     */
    public void changePassword(String email, ChangePasswordRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }
        if (req.getNewPassword().length() < 8) {
            throw new RuntimeException("New password must be at least 8 characters");
        }
        if (!req.getNewPassword().matches(".*[A-Za-z].*") || !req.getNewPassword().matches(".*\\d.*")) {
            throw new RuntimeException("New password must contain at least one letter and one number");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setFirstLogin(false);
        userRepository.save(user);
    }
}
