package com.mentorlink.service;

import com.mentorlink.modules.deadlines.entity.Deadline;
import com.mentorlink.modules.deadlines.repository.DeadlineRepository;
import com.mentorlink.modules.users.UserRepository;
import com.mentorlink.modules.users.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeadlineReminderScheduler {

    private final DeadlineRepository deadlineRepository;
    private final UserRepository userRepository;
    private final EmailNotificationService emailNotificationService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("MMM d, yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

    /**
     * Runs daily at 9 AM. Sends deadline reminders for deadlines due in 24–48 hours.
     */
    @Scheduled(cron = "${app.deadline-reminder.cron:0 0 9 * * ?}")
    public void sendDeadlineReminders() {
        Instant now = Instant.now();
        Instant start = now.plusSeconds(24 * 60 * 60);  // 24 hours from now
        Instant end = now.plusSeconds(48 * 60 * 60);   // 48 hours from now
        List<Deadline> upcoming = deadlineRepository.findByDueDateBetween(start, end);
        if (upcoming.isEmpty()) {
            return;
        }
        List<User> students = userRepository.findAll().stream()
                .filter(u -> u.getRoles().contains("STUDENT"))
                .toList();
        for (Deadline d : upcoming) {
            String dueDateStr = FORMATTER.format(d.getDueDate());
            for (User student : students) {
                try {
                    emailNotificationService.sendDeadlineReminder(
                            student.getEmail(), d.getName(), dueDateStr);
                } catch (Exception e) {
                    log.warn("Failed to send deadline reminder to {}: {}", student.getEmail(), e.getMessage());
                }
            }
        }
        log.info("Sent deadline reminders for {} deadlines to {} students", upcoming.size(), students.size());
    }
}
