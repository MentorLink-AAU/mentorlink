// src/main/java/com/mentorlink/modules/faculty/repository/FacultyProfileRepository.java
package com.mentorlink.modules.faculty.repository;

import com.mentorlink.modules.faculty.entity.FacultyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacultyProfileRepository extends JpaRepository<FacultyProfile, Long> {
    Optional<FacultyProfile> findByUser_Id(Long userId);
    Optional<FacultyProfile> findByEmail(String email);
    Optional<FacultyProfile> findByEmailIgnoreCase(String email);
    Optional<FacultyProfile> findByNameIgnoreCase(String name);
    /** Lookup by User email (canonical for auth/JWT). Prefer over findByEmail for logged-in faculty. */
    Optional<FacultyProfile> findByUser_Email(String email);
    List<FacultyProfile> findByDepartment(String department);
}
