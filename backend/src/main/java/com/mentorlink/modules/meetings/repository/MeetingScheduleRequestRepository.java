package com.mentorlink.modules.meetings.repository;

import com.mentorlink.modules.meetings.entity.MeetingScheduleRequest;
import com.mentorlink.modules.meetings.entity.MeetingScheduleRequest.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** JPA repository for future meeting schedule requests. */
public interface MeetingScheduleRequestRepository extends JpaRepository<MeetingScheduleRequest, Long> {

    /** All schedule requests for a project, newest first. */
    List<MeetingScheduleRequest> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    /** Approved requests ordered by agreed date (for admin listing). */
    List<MeetingScheduleRequest> findByStatusOrderByAgreedDateAsc(ScheduleStatus status);

    /** Find by id scoped to project (for access control). */
    Optional<MeetingScheduleRequest> findByIdAndProjectId(Long id, Long projectId);
}
