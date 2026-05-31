package com.mentorlink.modules.meetings.repository;

import com.mentorlink.modules.meetings.entity.Meeting;
import com.mentorlink.modules.projects.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** JPA repository for meeting logs (past meetings). */
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    /** All meetings for a project, most recent first. */
    List<Meeting> findByProjectOrderByMeetingDateDesc(Project project);

    /** All meetings for a project by id, most recent first. */
    List<Meeting> findByProjectIdOrderByMeetingDateDesc(Long projectId);

    /** Last (most recent) meeting for a project. */
    Optional<Meeting> findFirstByProjectOrderByMeetingDateDesc(Project project);

    /** Last (most recent) meeting for a project by id. */
    Optional<Meeting> findFirstByProjectIdOrderByMeetingDateDesc(Long projectId);
}
