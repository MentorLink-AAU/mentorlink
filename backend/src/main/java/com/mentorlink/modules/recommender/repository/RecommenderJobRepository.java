package com.mentorlink.modules.recommender.repository;

import com.mentorlink.modules.recommender.entity.RecommenderJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommenderJobRepository extends JpaRepository<RecommenderJob, String> {
}
