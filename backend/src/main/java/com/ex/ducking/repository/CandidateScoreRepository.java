package com.ex.ducking.repository;

import com.ex.ducking.model.CandidateScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidateScoreRepository extends JpaRepository<CandidateScore, Long> {

    List<CandidateScore> findByJobDescriptionIdOrderByScoreDesc(Long jobId);

    List<CandidateScore> findByJobDescriptionIdAndCandidateId(Long jobId, Long candidateId);

    List<CandidateScore> findByJobDescriptionIdAndScoreGreaterThanEqual(Long jobId, Integer minScore);

}