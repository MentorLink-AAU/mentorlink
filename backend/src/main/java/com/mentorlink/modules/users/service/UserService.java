package com.mentorlink.modules.users.service;

import com.mentorlink.modules.users.dto.UserResponseDto;
import com.mentorlink.modules.users.dto.UserUpdateDto;
import com.mentorlink.modules.users.entity.User;
import com.mentorlink.modules.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** User profile CRUD, current user lookup, and update from auth context. */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** Returns the authenticated user as DTO. */
    public UserResponseDto getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toDto(user);
    }

    /** Updates the authenticated user's profile (name, password, skills). */
    public UserResponseDto updateUser(Authentication authentication, UserUpdateDto update) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (update.getFullName() != null && !update.getFullName().isBlank()) {
            user.setFullName(update.getFullName());
        }
        if (update.getPassword() != null && !update.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(update.getPassword()));
        }
        if (update.getSkills() != null) {
            user.setSkills(new java.util.HashSet<>(update.getSkills()));
        }

        user = userRepository.save(user);
        return toDto(user);
    }

    /** Find user by ID, returns empty if not found. */
    public Optional<UserResponseDto> getUserById(Long id) {
        return userRepository.findById(id).map(this::toDto);
    }

    /** Get all users (admin use). */
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /** Delete user by ID. */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    private UserResponseDto toDto(User user) {
        String role = extractRole(user);

        UserResponseDto.UserResponseDtoBuilder builder = UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(role)
                .skills(user.getSkills() != null ? List.copyOf(user.getSkills()) : List.of())
                .achievements(user.getAchievements() != null ? List.copyOf(user.getAchievements()) : List.of())
                .requiresPasswordChange(user.isFirstLogin());

        // 🔹 If Student → pull from StudentProfile
        if ("STUDENT".equals(role) && user.getStudentProfile() != null) {
            builder.rollNumber(user.getStudentProfile().getRollNumber());
            builder.department(user.getStudentProfile().getDepartment());
            builder.yearOfStudy(user.getStudentProfile().getYearOfStudy());
        }

        // 🔹 If Faculty → pull from FacultyProfile
        if ("FACULTY".equals(role) && user.getFacultyProfile() != null) {
            builder.department(user.getFacultyProfile().getDepartment());
            builder.expertise(user.getFacultyProfile().getExpertise());
            builder.maxGroups(user.getFacultyProfile().getMaxGroups());
            builder.currentLoad(user.getFacultyProfile().getCurrentLoad());
        }

        // 🔹 If Admin → no extra details
        return builder.build();
    }


    // ✅ Extract role safely
    private String extractRole(User user) {
        try {
            if (user.getRoles() == null || user.getRoles().isEmpty()) return "STUDENT";
            Iterator<?> it = user.getRoles().iterator();
            Object first = it.next();
            if (first instanceof String) return (String) first;
            try {
                Method m = first.getClass().getMethod("getName");
                Object name = m.invoke(first);
                if (name != null) return name.toString();
            } catch (NoSuchMethodException ignored) {}
            return first.toString();
        } catch (Exception e) {
            return "STUDENT";
        }
    }
}
