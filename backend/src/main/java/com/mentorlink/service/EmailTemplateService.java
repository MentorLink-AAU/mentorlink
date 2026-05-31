package com.mentorlink.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Renders HTML email bodies from Thymeleaf templates under {@code templates/email/}.
 */
@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    /** Provided by Spring Boot auto-configuration (implements {@link ITemplateEngine}). */
    private final ITemplateEngine templateEngine;

    public String renderPasswordReset(String resetLink) {
        Context ctx = new Context();
        ctx.setVariable("resetLink", resetLink);
        return templateEngine.process("email/password-reset", ctx);
    }

    public String renderWelcomeEmail(String role) {
        Context ctx = new Context();
        ctx.setVariable("role", role);
        return templateEngine.process("email/welcome-email", ctx);
    }

    public String renderDeadlineReminder(String deadlineName, String dueDate) {
        Context ctx = new Context();
        ctx.setVariable("deadlineName", deadlineName);
        ctx.setVariable("dueDate", dueDate);
        return templateEngine.process("email/deadline-reminder", ctx);
    }

    public String renderApprovalNotification(String projectTitle, String status) {
        Context ctx = new Context();
        ctx.setVariable("projectTitle", projectTitle);
        ctx.setVariable("status", status);
        return templateEngine.process("email/approval-notification", ctx);
    }
}
