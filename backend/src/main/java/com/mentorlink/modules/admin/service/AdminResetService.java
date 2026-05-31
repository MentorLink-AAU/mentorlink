package com.mentorlink.modules.admin.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Service
public class AdminResetService {

    private final JdbcTemplate jdbcTemplate;

    public AdminResetService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void resetYearlyData() {
        List<Long> adminIds = jdbcTemplate.queryForList(
                "SELECT DISTINCT user_id FROM user_roles WHERE role = 'ADMIN'",
                Long.class
        );

        if (adminIds.isEmpty()) {
            throw new IllegalStateException("No admin accounts found. Reset is blocked to avoid locking everyone out.");
        }

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
        try {
            deleteAll("chat_messages");
            deleteAll("faculty_mentorship_requests");
            deleteAll("meeting_schedule_requests");
            deleteAll("meetings");
            deleteAll("notifications");
            deleteAll("password_reset_tokens");
            deleteAll("report_summaries");
            deleteAll("submissions");
            deleteAll("recommender_project_groups");
            deleteAll("recommender_jobs");
            deleteAll("group_members");
            deleteAll("project_groups");
            deleteAll("projects");
            deleteAll("deadlines");
            deleteAll("student_profiles");
            deleteAll("faculty_profiles");

            deleteNonAdminRows("user_interests", adminIds);
            deleteNonAdminRows("user_skills", adminIds);
            deleteNonAdminRows("user_achievements", adminIds);
            deleteNonAdminRows("user_roles", adminIds);
            deleteUsersExceptAdmins(adminIds);
        } finally {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");
        }

        deleteDirectoryContents(Paths.get("uploads"));
        deleteDirectoryContents(Paths.get("backend", "uploads"));
        deleteDirectoryContents(Paths.get("backend", "python-recommender", "outputs"));
    }

    private void deleteAll(String tableName) {
        jdbcTemplate.execute("DELETE FROM " + tableName);
    }

    private void deleteNonAdminRows(String tableName, List<Long> adminIds) {
        String placeholders = String.join(",", adminIds.stream().map(id -> "?").toList());
        jdbcTemplate.update("DELETE FROM " + tableName + " WHERE user_id NOT IN (" + placeholders + ")", adminIds.toArray());
    }

    private void deleteUsersExceptAdmins(List<Long> adminIds) {
        String placeholders = String.join(",", adminIds.stream().map(id -> "?").toList());
        jdbcTemplate.update("DELETE FROM users WHERE id NOT IN (" + placeholders + ")", adminIds.toArray());
    }

    private void deleteDirectoryContents(Path directory) {
        try {
            if (!Files.exists(directory) || !Files.isDirectory(directory)) {
                return;
            }
            try (Stream<Path> paths = Files.walk(directory)) {
                paths.sorted((a, b) -> b.getNameCount() - a.getNameCount())
                        .filter(path -> !path.equals(directory))
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException ignored) {
                                // Best effort cleanup; DB reset has already succeeded.
                            }
                        });
            }
        } catch (IOException ignored) {
            // Best effort cleanup; DB reset has already succeeded.
        }
    }
}
