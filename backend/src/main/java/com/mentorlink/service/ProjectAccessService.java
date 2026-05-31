package com.mentorlink.service;

import com.mentorlink.common.exception.ApiException;
import com.mentorlink.modules.groups.entity.Group;
import com.mentorlink.modules.projects.entity.Project;
import com.mentorlink.modules.users.UserRepository;
import com.mentorlink.modules.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Centralized project/group access checks and rules for exposing sensitive join tokens.
 */
@Service
@RequiredArgsConstructor
public class ProjectAccessService {

    private final UserRepository userRepository;

    public record AccessFlags(boolean admin, boolean faculty, boolean groupMember, boolean mentor) {}

    public AccessFlags resolveAccess(Project project, String email) {
        if (project == null || email == null || email.isBlank()) {
            return new AccessFlags(false, false, false, false);
        }
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return new AccessFlags(false, false, false, false);
        }
        boolean admin = user.getRoles().contains("ADMIN");
        boolean faculty = user.getRoles().contains("FACULTY");
        Group group = project.getGroup();
        boolean member = group != null
                && group.getMembers().stream().anyMatch(m -> email.equalsIgnoreCase(m.getEmail()));
        boolean mentor = project.getMentor() != null
                && email.equalsIgnoreCase(project.getMentor().getEmail());
        return new AccessFlags(admin, faculty, member, mentor);
    }

    public void requireProjectAccess(Project project, String email) {
        AccessFlags flags = resolveAccess(project, email);
        if (!flags.admin() && !flags.groupMember() && !flags.mentor()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have access to this project");
        }
    }

    public void requireGroupAccess(Group group, String email) {
        if (group == null) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have access to this group");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "User not found"));
        if (user.getRoles().contains("ADMIN")) {
            return;
        }
        if (group.getMembers().stream().anyMatch(m -> email.equalsIgnoreCase(m.getEmail()))) {
            return;
        }
        Project project = group.getProject();
        if (project != null && project.getMentor() != null
                && email.equalsIgnoreCase(project.getMentor().getEmail())) {
            return;
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have access to this group");
    }

    public boolean includeJoinToken(AccessFlags flags) {
        return flags.admin() || flags.groupMember();
    }

    /** Student join token: members and admins only. */
    public boolean includeMentorJoinToken(AccessFlags flags) {
        return flags.admin() || flags.mentor();
    }
}
