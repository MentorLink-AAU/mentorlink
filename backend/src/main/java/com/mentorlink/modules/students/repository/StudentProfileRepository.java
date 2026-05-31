// src/main/java/com/mentorlink/modules/students/repository/StudentProfileRepository.java
package com.mentorlink.modules.students.repository;

import com.mentorlink.modules.students.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    Optional<StudentProfile> findByUser_Id(Long userId);
    Optional<StudentProfile> findByRollNumber(String rollNumber);
}
