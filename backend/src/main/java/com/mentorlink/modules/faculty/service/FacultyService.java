package com.mentorlink.modules.faculty.service;

import com.mentorlink.modules.faculty.dto.FacultyRequest;
import com.mentorlink.modules.faculty.entity.FacultyProfile;
import com.mentorlink.modules.faculty.FacultyRepository;
import com.mentorlink.modules.users.UserRepository;
import com.mentorlink.modules.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final UserRepository userRepository;

    public FacultyProfile createFaculty(FacultyRequest dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        FacultyProfile faculty = FacultyProfile.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .department(dto.getDepartment())
                .expertise(dto.getExpertise())
                .currentLoad(0)
                .maxGroups(dto.getMaxGroups() != null ? dto.getMaxGroups() : 3)
                .user(user)
                .build();

        return facultyRepository.save(faculty);
    }
}
