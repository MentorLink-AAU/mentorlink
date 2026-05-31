package com.mentorlink.modules.summarization.repository;

import com.mentorlink.modules.projects.entity.Project;
import com.mentorlink.modules.summarization.entity.ReportSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportSummaryRepository extends JpaRepository<ReportSummary, Long> {
    List<ReportSummary> findByProjectOrderByCreatedAtDesc(Project project);
    Optional<ReportSummary> findByProjectAndReportFilePath(Project project, String reportFilePath);
}
