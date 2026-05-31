package com.mentorlink.modules.faculty.repository;

import com.mentorlink.modules.faculty.entity.FacultyMentorshipRequest;
import com.mentorlink.modules.faculty.entity.FacultyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FacultyMentorshipRequestRepository extends JpaRepository<FacultyMentorshipRequest, Long> {
    List<FacultyMentorshipRequest> findByFacultyAndStatus(FacultyProfile faculty, FacultyMentorshipRequest.RequestStatus status);
    List<FacultyMentorshipRequest> findByFaculty(FacultyProfile faculty);
    boolean existsByGroup_IdAndFaculty_IdAndStatus(Long groupId, Long facultyId, FacultyMentorshipRequest.RequestStatus status);
}
