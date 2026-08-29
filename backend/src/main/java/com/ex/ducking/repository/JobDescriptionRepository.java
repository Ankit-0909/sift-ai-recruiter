package com.ex.ducking.repository;


import com.ex.ducking.model.JobDescription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobDescriptionRepository extends JpaRepository<JobDescription, Long> {
}