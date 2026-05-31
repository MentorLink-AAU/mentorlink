package com.mentorlink.modules.faculty.entity;

import com.mentorlink.modules.groups.entity.Group;
import com.mentorlink.modules.projects.entity.Project;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/** Faculty mentorship request: group invites faculty for a project; PENDING/APPROVED/REJECTED. */
@Entity
@Table(name = "faculty_mentorship_requests")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class FacultyMentorshipRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne(optional = false)
    @JoinColumn(name = "faculty_id")
    private FacultyProfile faculty;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(nullable = false)
    private String projectTopic;

    @Column(columnDefinition = "TEXT")
    private String projectDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    private Instant requestedAt;
    private Instant respondedAt;

    public enum RequestStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}
