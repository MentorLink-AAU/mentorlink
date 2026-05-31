package com.mentorlink.modules.submissions.repository;

import com.mentorlink.modules.projects.entity.Project;
import com.mentorlink.modules.submissions.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByProject(Project project);
    List<Submission> findByProjectOrderByCategory(Project project);
    Optional<Submission> findByProjectAndCategory(Project project, String category);
    boolean existsByProjectAndCategory(Project project, String category);
}
