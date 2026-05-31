package com.mentorlink.modules.recommender.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "recommender_jobs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommenderJob {

    @Id
    @Column(name = "job_id", nullable = false, length = 64)
    private String jobId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RecommenderJobStatus status;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "output_path", length = 1000)
    private String outputPath;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @PrePersist
    void onCreate() {
        if (startedAt == null) {
            startedAt = Instant.now();
        }
    }

    @PreUpdate
    void onUpdate() {
        if (status == RecommenderJobStatus.COMPLETED || status == RecommenderJobStatus.FAILED) {
            finishedAt = Instant.now();
        }
    }
}
