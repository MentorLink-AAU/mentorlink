package com.mentorlink.modules.summarization.service;

import com.mentorlink.modules.notifications.service.NotificationService;
import com.mentorlink.modules.summarization.entity.ReportSummary;
import com.mentorlink.modules.summarization.entity.ReportSummaryStatus;
import com.mentorlink.modules.summarization.repository.ReportSummaryRepository;
import com.mentorlink.util.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SummarizationAsyncService {

    private final ReportSummaryRepository reportSummaryRepository;
    private final FileStorageService fileStorageService;
    private final RestTemplate restTemplate;
    private final NotificationService notificationService;

    @Value("${app.nlp.summarization.url:http://localhost:5001}")
    private String nlpServiceUrl;

    @Async
    @Transactional
    public void processReportSummary(Long summaryId) {
        ReportSummary summary = reportSummaryRepository.findById(summaryId)
                .orElse(null);
        if (summary == null) {
            log.warn("ReportSummary {} not found for async processing", summaryId);
            return;
        }
        if (summary.getStatus() != ReportSummaryStatus.PENDING) {
            log.debug("ReportSummary {} already processed (status={})", summaryId, summary.getStatus());
            return;
        }

        summary.setStatus(ReportSummaryStatus.PROCESSING);
        reportSummaryRepository.save(summary);

        Path fullPath = fileStorageService.resolve(summary.getReportFilePath());
        String submitterEmail = summary.getSubmittedBy() != null ? summary.getSubmittedBy().getEmail() : null;

        try {
            String summaryText = callNlpSummarizationService(fullPath);
            if (summaryText == null || summaryText.isBlank()) {
                throw new RuntimeException("NLP service returned empty summary");
            }
            summary.setGeneratedSummary(summaryText);
            summary.setStatus(ReportSummaryStatus.DONE);
            summary.setErrorMessage(null);
            reportSummaryRepository.save(summary);

            if (submitterEmail != null) {
                String projectTitle = summary.getProject().getTitle();
                notificationService.create(submitterEmail,
                        "Report summary for project \"" + projectTitle + "\" is ready.");
            }
        } catch (Exception e) {
            log.error("Summarization failed for ReportSummary {}: {}", summaryId, e.getMessage());
            summary.setStatus(ReportSummaryStatus.FAILED);
            summary.setErrorMessage(e.getMessage() != null ? e.getMessage().substring(0, Math.min(1000, e.getMessage().length())) : "Unknown error");
            reportSummaryRepository.save(summary);

            if (submitterEmail != null) {
                String projectTitle = summary.getProject().getTitle();
                notificationService.create(submitterEmail,
                        "Report summary for project \"" + projectTitle + "\" failed: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
            }
        }
    }

    private String callNlpSummarizationService(Path pdfPath) {
        String url = nlpServiceUrl.trim().replaceAll("/$", "") + "/summarize";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(pdfPath.toFile()));
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<NlpSummarizeResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, request, NlpSummarizeResponse.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            if (response.getBody().getError() != null) {
                throw new RuntimeException("NLP service error: " + response.getBody().getError());
            }
            return response.getBody().getSummary();
        }
        throw new RuntimeException("NLP service returned non-2xx");
    }

    private static class NlpSummarizeResponse {
        private String summary;
        private String error;
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
