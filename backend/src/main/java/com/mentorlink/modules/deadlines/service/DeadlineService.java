package com.mentorlink.modules.deadlines.service;

import com.mentorlink.modules.deadlines.entity.Deadline;
import com.mentorlink.modules.deadlines.entity.DeadlineType;
import com.mentorlink.modules.deadlines.repository.DeadlineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeadlineService {

    private final DeadlineRepository deadlineRepository;

    public Deadline createOrUpdate(String name, Instant dueDate, DeadlineType type) {
        return deadlineRepository.findByType(type)
                .map(d -> {
                    d.setName(name);
                    d.setDueDate(dueDate);
                    return deadlineRepository.save(d);
                })
                .orElseGet(() -> deadlineRepository.save(Deadline.builder()
                        .name(name)
                        .dueDate(dueDate)
                        .type(type)
                        .build()));
    }

    public List<Deadline> getAll() {
        return deadlineRepository.findAllByOrderByDueDateAsc();
    }

    public java.util.Optional<Deadline> getByType(DeadlineType type) {
        return deadlineRepository.findByType(type);
    }

    public boolean isPastDeadline(DeadlineType type) {
        return deadlineRepository.findByType(type)
                .map(d -> Instant.now().isAfter(d.getDueDate()))
                .orElse(false);
    }

    /**
     * Extend (update) a deadline's due date.
     */
    public Deadline extendDeadline(Long id, Instant newDueDate) {
        Deadline d = deadlineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deadline not found: " + id));
        d.setDueDate(newDueDate);
        return deadlineRepository.save(d);
    }
}
