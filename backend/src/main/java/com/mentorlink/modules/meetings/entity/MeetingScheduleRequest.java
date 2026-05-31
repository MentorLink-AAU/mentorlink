package com.mentorlink.modules.meetings.entity;

import com.mentorlink.common.auditing.Auditable;
import com.mentorlink.modules.projects.entity.Project;
import com.mentorlink.modules.users.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Future meeting schedule request with approval flow.
 * Student proposes → Faculty approves or counter-proposes ("I'm free on X") → Both agree → Scheduled.
 */
@Entity
@Table(name = "meeting_schedule_requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingScheduleRequest extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /** Student who proposed the meeting */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposed_by_id", nullable = false)
    private User proposedBy;

    /** Date proposed by student */
    @Column(name = "proposed_date", nullable = false)
    private LocalDate proposedDate;

    /** Optional notes from student */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Status: PENDING_FACULTY, PENDING_STUDENT, APPROVED */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ScheduleStatus status = ScheduleStatus.PENDING_FACULTY;

    /** When faculty counter-proposes: alternative date ("I'm free on this date") */
    @Column(name = "faculty_counter_date")
    private LocalDate facultyCounterDate;

    /** Faculty's message, e.g. "I'm not free then, I'm free on March 18" */
    @Column(name = "faculty_counter_notes", columnDefinition = "TEXT")
    private String facultyCounterNotes;

    /** Final agreed date when both approve (status = APPROVED) */
    @Column(name = "agreed_date")
    private LocalDate agreedDate;

    public enum ScheduleStatus {
        PENDING_FACULTY,   // Student proposed, waiting for faculty
        PENDING_STUDENT,   // Faculty countered, waiting for student
        APPROVED           // Both agreed, meeting scheduled
    }
}
