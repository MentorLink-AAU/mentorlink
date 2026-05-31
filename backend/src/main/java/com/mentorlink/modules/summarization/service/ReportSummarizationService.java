package com.mentorlink.modules.summarization.service;

import com.mentorlink.common.exception.ApiException;
import com.mentorlink.modules.groups.entity.Group;
import com.mentorlink.modules.groups.repository.GroupRepository;
import com.mentorlink.modules.projects.entity.Project;
import com.mentorlink.modules.projects.repository.ProjectRepository;
import com.mentorlink.modules.submissions.entity.Submission;
import com.mentorlink.modules.summarization.dto.ReportSummaryDto;
import com.mentorlink.modules.summarization.entity.ReportSummary;
import com.mentorlink.modules.summarization.entity.ReportSummaryStatus;
import com.mentorlink.modules.summarization.repository.ReportSummaryRepository;
import com.mentorlink.modules.users.UserRepository;
import com.mentorlink.modules.users.entity.User;
import com.mentorlink.util.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportSummarizationService {

    private final ProjectRepository projectRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ReportSummaryRepository reportSummaryRepository;
    private final FileStorageService fileStorageService;
    private final SummarizationAsyncService summarizationAsyncService;

    /**
     * Upload PDF, create PENDING summary, process async, return immediately.
     * Only group members can submit. Faculty and Admin can also trigger.
     */
    @Transactional
    public ReportSummaryDto summarizeReport(Long projectId, MultipartFile file, String submitterEmail, boolean isAdmin) throws IOException {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.NOT_FOUND, "NOT_FOUND", "Project not found"));

        Group group = project.getGroup();
        if (group == null) {
            throw new ApiException(org.springframework.http.HttpStatus.BAD_REQUEST, "NO_GROUP", "Project has no group linked");
        }

        User submitter = userRepository.findByEmail(submitterEmail)
                .orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.NOT_FOUND, "NOT_FOUND", "User not found"));

        boolean isGroupMember = group.getMembers().stream().anyMatch(u -> u.getEmail().equals(submitterEmail));
        boolean isMentor = project.getMentor() != null && project.getMentor().getEmail().equals(submitterEmail);
        if (!isGroupMember && !isMentor && !isAdmin) {
            throw new ApiException(org.springframework.http.HttpStatus.FORBIDDEN, "FORBIDDEN",
                    "Only group members, faculty mentor, or admin can summarize reports");
        }

        if (file == null || file.isEmpty()) {
            throw new ApiException(org.springframework.http.HttpStatus.BAD_REQUEST, "EMPTY_FILE", "No file provided");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new ApiException(org.springframework.http.HttpStatus.BAD_REQUEST, "INVALID_FILE", "File must be a PDF");
        }

        String storedPath = fileStorageService.store(file, "reports/" + projectId);

        ReportSummary entity = ReportSummary.builder()
                .project(project)
                .submittedBy(submitter)
                .reportFilePath(storedPath)
                .originalFilename(filename)
                .status(ReportSummaryStatus.PENDING)
                .build();
        entity = reportSummaryRepository.save(entity);

        summarizationAsyncService.processReportSummary(entity.getId());
        return ReportSummaryDto.from(entity);
    }

    /**
     * Trigger summarization from an existing REPORT submission (e.g. on upload).
     * Runs in a new transaction so failures do not roll back the upload transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void triggerFromSubmission(Submission submission) {
        Project project = submission.getProject();
        if (project == null) return;
        if (!"REPORT".equals(submission.getCategory())) return;

        var path = fileStorageService.resolve(submission.getFilePath());
        if (!Files.exists(path) || !Files.isReadable(path)) {
            log.warn("Submission file not found or not readable: {}", submission.getFilePath());
            return;
        }
        if (submission.getOriginalFilename() != null && !submission.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            log.debug("Skipping summarization for non-PDF submission: {}", submission.getOriginalFilename());
            return;
        }

        ReportSummary entity = ReportSummary.builder()
                .project(project)
                .submittedBy(submission.getSubmittedBy())
                .reportFilePath(submission.getFilePath())
                .originalFilename(submission.getOriginalFilename())
                .status(ReportSummaryStatus.PENDING)
                .build();
        entity = reportSummaryRepository.save(entity);
        summarizationAsyncService.processReportSummary(entity.getId());
    }

    public List<ReportSummaryDto> listByProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.NOT_FOUND, "NOT_FOUND", "Project not found"));
        return reportSummaryRepository.findByProjectOrderByCreatedAtDesc(project).stream()
                .map(ReportSummaryDto::from)
                .collect(Collectors.toList());
    }
}
