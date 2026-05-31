package com.mentorlink.modules.projects.repository;

import com.mentorlink.modules.faculty.entity.FacultyProfile;
import com.mentorlink.modules.projects.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByMentor(FacultyProfile mentor);
}
