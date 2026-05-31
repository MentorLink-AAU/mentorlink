package com.mentorlink.modules.recommender.repository;

import com.mentorlink.modules.recommender.entity.RecommenderProjectGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommenderProjectGroupRepository extends JpaRepository<RecommenderProjectGroup, Long> {
    List<RecommenderProjectGroup> findByJobIdOrderByIdAsc(String jobId);
}
