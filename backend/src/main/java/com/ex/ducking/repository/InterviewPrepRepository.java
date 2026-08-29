package com.ex.ducking.repository;

import com.ex.ducking.model.InterviewPrep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewPrepRepository extends JpaRepository<InterviewPrep, Long> {
    List<InterviewPrep> findByCandidateId(Long candidateId);
}