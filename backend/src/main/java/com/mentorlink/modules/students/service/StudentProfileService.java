// src/main/java/com/mentorlink/modules/students/service/StudentProfileService.java
package com.mentorlink.modules.students.service;

import com.mentorlink.modules.students.dto.StudentProfileDTO;
import com.mentorlink.modules.students.entity.StudentProfile;
import com.mentorlink.modules.students.repository.StudentProfileRepository;
import com.mentorlink.modules.users.UserRepository;
import com.mentorlink.modules.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentProfileService {

    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;

    public StudentProfileDTO saveProfile(StudentProfileDTO dto) {
        StudentProfile profile;
        if (dto.getId() != null) {
            profile = studentProfileRepository.findById(dto.getId())
                    .orElse(StudentProfile.builder().build());
        } else {
            profile = StudentProfile.builder().build();
        }

        if (dto.getUserId() != null) {
            User u = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            profile.setUser(u);
        }

        profile.setDepartment(dto.getDepartment());
        profile.setRollNumber(dto.getRollNumber());
        profile.setYearOfStudy(dto.getYearOfStudy());

        StudentProfile saved = studentProfileRepository.save(profile);

        return StudentProfileDTO.builder()
                .id(saved.getId())
                .userId(saved.getUser() != null ? saved.getUser().getId() : null)
                .department(saved.getDepartment())
                .rollNumber(saved.getRollNumber())
                .yearOfStudy(saved.getYearOfStudy())
                .build();
    }

    public StudentProfileDTO getProfile(Long userId) {
        StudentProfile p = studentProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        return StudentProfileDTO.builder()
                .id(p.getId())
                .userId(p.getUser() != null ? p.getUser().getId() : null)
                .department(p.getDepartment())
                .rollNumber(p.getRollNumber())
                .yearOfStudy(p.getYearOfStudy())
                .build();
    }
}
