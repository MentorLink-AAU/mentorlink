package com.mentorlink.modules.submissions.service;

import com.mentorlink.common.exception.ApiException;
import com.mentorlink.modules.groups.entity.Group;
import com.mentorlink.modules.groups.repository.GroupRepository;
import com.mentorlink.modules.projects.entity.Project;
import com.mentorlink.modules.projects.repository.ProjectRepository;
import com.mentorlink.modules.submissions.dto.SubmissionResponseDto;
import com.mentorlink.modules.summarization.service.ReportSummarizationService;
import com.mentorlink.modules.submissions.entity.Submission;
import com.mentorlink.modules.submissions.entity.SubmissionCategory;
import com.mentorlink.modules.submissions.repository.SubmissionRepository;
import com.mentorlink.modules.users.UserRepository;
import com.mentorlink.modules.users.entity.User;
import com.mentorlink.util.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;

import java.nio.file.Path;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    public static final Set<String> ALLOWED_CATEGORIES = Set.of("REPORT", "RESEARCH_PAPER", "PPT");

    private final SubmissionRepository submissionRepository;
    private final ProjectRepository projectRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final ReportSummarizationService reportSummarizationService;

    /**
     * Student (group member) submits a file. One submission per category per project.
     * Categories: REPORT, RESEARCH_PAPER, PPT
     */
    @Transactional
    public SubmissionResponseDto upload(Long projectId, MultipartFile file, String categoryStr, String submitterEmail) throws IOException {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Project not found"));

        Group group = project.getGroup();
        if (group == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "NO_GROUP", "Project has no group linked");
        }

        User submitter = userRepository.findByEmail(submitterEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "User not found"));

        if (!group.getMembers().contains(submitter)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_MEMBER", "Only group members can submit");
        }

        String category = SubmissionCategory.fromString(categoryStr) != null
                ? SubmissionCategory.fromString(categoryStr).name()
                : null;
        if (category == null || !ALLOWED_CATEGORIES.contains(category)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CATEGORY",
                    "Category must be one of: REPORT, RESEARCH_PAPER, PPT");
        }

        if (submissionRepository.existsByProjectAndCategory(project, category)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ALREADY_SUBMITTED",
                    "A " + category + " has already been submitted for this project. Faculty or Admin can remove it to allow re-upload.");
        }

        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "EMPTY_FILE", "No file provided");
        }

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload";
        String path = fileStorageService.store(file, "submissions/" + projectId);

        Submission submission = Submission.builder()
                .project(project)
                .filePath(path)
                .category(category)
                .originalFilename(originalFilename)
                .submittedBy(submitter)
                .build();
        submission = submissionRepository.save(submission);

        if ("REPORT".equals(category)) {
            try {
                reportSummarizationService.triggerFromSubmission(submission);
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(SubmissionService.class)
                        .warn("Auto-summarization failed for REPORT upload (project={}): {}", projectId, e.getMessage());
            }
        }

        return SubmissionResponseDto.from(submission);
    }

    /**
     * Only Faculty (group mentor) or Admin can delete submissions.
     */
    @Transactional
    public void delete(Long submissionId, String requesterEmail, boolean isAdmin) throws IOException {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Submission not found"));

        if (isAdmin) {
            // Admin can delete any submission
        } else {
            // Must be the mentor of the project's group
            Project project = submission.getProject();
            if (project.getMentor() == null) {
                throw new ApiException(HttpStatus.FORBIDDEN, "NO_MENTOR", "Project has no mentor assigned");
            }
            if (!project.getMentor().getEmail().equals(requesterEmail)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN",
                        "Only the group mentor or admin can remove submissions");
            }
        }

        fileStorageService.delete(submission.getFilePath());
        submissionRepository.delete(submission);
    }

    public List<SubmissionResponseDto> listByProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Project not found"));
        return submissionRepository.findByProjectOrderByCategory(project).stream()
                .map(SubmissionResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<SubmissionResponseDto> listByGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Group not found"));
        Project project = group.getProject();
        if (project == null) return List.of();
        return listByProject(project.getId());
    }

    /**
     * Get file for download. Group members, mentor, and admin can download.
     */
    public FileDownload getFile(Long submissionId, String requesterEmail, boolean isAdmin) throws IOException {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Submission not found"));

        Project project = submission.getProject();
        Group group = project.getGroup();

        if (isAdmin) {
            // Admin can download any
        } else if (project.getMentor() != null && project.getMentor().getEmail().equals(requesterEmail)) {
            // Mentor can download
        } else if (group != null && group.getMembers().stream().anyMatch(u -> u.getEmail().equals(requesterEmail))) {
            // Group member can download
        } else {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have access to this file");
        }

        Path path = fileStorageService.resolve(submission.getFilePath());
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "File not found or not readable");
        }
        String filename = submission.getOriginalFilename() != null ? submission.getOriginalFilename() : "submission";
        return new FileDownload(resource, filename);
    }

    public record FileDownload(Resource resource, String filename) {}
}
