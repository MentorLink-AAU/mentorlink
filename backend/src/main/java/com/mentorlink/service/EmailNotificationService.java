package com.mentorlink.service;

import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/** Sends transactional HTML emails (Thymeleaf + MIME) asynchronously where annotated. */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final EmailTemplateService emailTemplateService;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @PostConstruct
    public void logMailConfig() {
        if (fromEmail == null || fromEmail.isBlank()) {
            log.warn(
                    "Email not configured: spring.mail.username is empty. Outbound mail is disabled until MAIL_USERNAME (and MAIL_PASSWORD) are set.");
        } else if (mailPassword == null || mailPassword.isBlank()) {
            log.warn("MAIL_PASSWORD is not set. SMTP authentication will fail until you set it (e.g. Gmail App Password).");
        } else {
            log.info("Email configured for outbound mail from: {}", fromEmail);
        }
    }

    private boolean isMailReady() {
        return fromEmail != null && !fromEmail.isBlank() && mailPassword != null && !mailPassword.isBlank();
    }

    private void sendHtml(String to, String subject, String htmlBody) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(message);
    }

    /** Sends welcome email to the user's registered email. */
    @Async
    public void sendRegistrationWelcome(String toEmail, String role) {
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("Cannot send welcome email: recipient email is blank");
            return;
        }
        if (!isMailReady()) {
            log.warn(
                    "Mail not configured. Welcome email to {} skipped. Set MAIL_USERNAME and MAIL_PASSWORD (mapped to spring.mail.*).",
                    toEmail);
            return;
        }
        try {
            String html = emailTemplateService.renderWelcomeEmail(role);
            sendHtml(toEmail.trim(), "Welcome to MentorLink", html);
            log.info("Welcome email sent to {} (role: {})", toEmail, role);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Async
    public void sendDeadlineReminder(String toEmail, String deadlineName, String dueDate) {
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("Cannot send deadline reminder: recipient email is blank");
            return;
        }
        if (!isMailReady()) {
            log.warn("Mail not configured, skipping deadline reminder to {}", toEmail);
            return;
        }
        try {
            String html = emailTemplateService.renderDeadlineReminder(deadlineName, dueDate);
            sendHtml(toEmail.trim(), "MentorLink: Deadline Reminder — " + deadlineName, html);
            log.info("Deadline reminder sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send deadline reminder to {}", toEmail, e);
        }
    }

    /** Sends password reset link (does not block forgot-password response). */
    @Async
    public void sendPasswordReset(String toEmail, String resetLink) {
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("Cannot send password reset email: recipient email is blank");
            return;
        }
        if (!isMailReady()) {
            log.warn(
                    "Mail not configured. Password reset email to {} skipped. Set MAIL_USERNAME and MAIL_PASSWORD.",
                    toEmail);
            return;
        }
        try {
            String html = emailTemplateService.renderPasswordReset(resetLink);
            sendHtml(toEmail.trim(), "MentorLink: Reset your password", html);
            log.info("Password reset email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Async
    public void sendApprovalNotification(String toEmail, String projectTitle, boolean approved) {
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("Cannot send approval notification: recipient email is blank");
            return;
        }
        if (!isMailReady()) {
            log.warn("Mail not configured, skipping approval notification to {}", toEmail);
            return;
        }
        try {
            String status = approved ? "Approved" : "Rejected";
            String html = emailTemplateService.renderApprovalNotification(projectTitle, status);
            sendHtml(toEmail.trim(), "MentorLink: Faculty Request " + status, html);
            log.info("Faculty request {} email sent to {}", status.toLowerCase(), toEmail);
        } catch (Exception e) {
            log.error("Failed to send approval notification to {}", toEmail, e);
        }
    }
}
