package com.mentorlink.modules.faculty;

import com.mentorlink.modules.faculty.entity.FacultyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FacultyRepository extends JpaRepository<FacultyProfile, Long> {
    Optional<FacultyProfile> findByEmail(String email);
}
