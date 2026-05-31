package com.mentorlink.modules.recommender.entity;

import com.mentorlink.modules.faculty.entity.FacultyProfile;
import com.mentorlink.modules.users.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "recommender_project_groups")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommenderProjectGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false, length = 64)
    private String jobId;

    @Column(name = "group_id", nullable = false, length = 64)
    private String groupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student1_id")
    private User student1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student2_id")
    private User student2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student3_id")
    private User student3;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id")
    private FacultyProfile faculty;

    @Column(name = "similarity_score")
    private Double similarityScore;

    @Column(name = "algorithm_used", length = 120)
    private String algorithmUsed;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
