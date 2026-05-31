package com.mentorlink.modules.meetings.entity;

import com.mentorlink.common.auditing.Auditable;
import com.mentorlink.modules.projects.entity.Project;
import com.mentorlink.modules.users.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Meeting log between student group and mentor.
 * Students add meeting details; faculty verifies them.
 */
@Entity
@Table(name = "meetings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Meeting extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /** Date when the meeting took place */
    @Column(name = "meeting_date", nullable = false)
    private LocalDate meetingDate;

    /** Details/notes about the meeting (added by student) */
    @Column(columnDefinition = "TEXT")
    private String details;

    /** Whether the faculty mentor has verified this meeting */
    @Builder.Default
    private boolean verified = false;

    /** When faculty verified (null if not yet verified) */
    @Column(name = "verified_at")
    private Instant verifiedAt;

    /** Student who logged this meeting */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logged_by_id", nullable = false)
    private User loggedBy;
}
