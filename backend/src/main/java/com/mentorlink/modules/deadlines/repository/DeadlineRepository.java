package com.mentorlink.modules.deadlines.repository;

import com.mentorlink.modules.deadlines.entity.Deadline;
import com.mentorlink.modules.deadlines.entity.DeadlineType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DeadlineRepository extends JpaRepository<Deadline, Long> {
    List<Deadline> findAllByOrderByDueDateAsc();
    Optional<Deadline> findByType(DeadlineType type);
    List<Deadline> findByDueDateBetween(Instant start, Instant end);
}
