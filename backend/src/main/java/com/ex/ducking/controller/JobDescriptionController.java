package com.ex.ducking.controller;


import com.ex.ducking.model.CandidateScore;
import com.ex.ducking.model.JobDescription;
import com.ex.ducking.repository.CandidateScoreRepository;
import com.ex.ducking.repository.JobDescriptionRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/job-descriptions")
public class JobDescriptionController {

    @Autowired
    private JobDescriptionRepository jobRepository;

    @PostMapping
    public JobDescription createJob(@Valid @RequestBody JobDescription job) {
        return jobRepository.save(job);
    }


    @GetMapping
    public List<JobDescription> getAllJobs() {
        return jobRepository.findAll();
    }


    @GetMapping("/{id}")
    public ResponseEntity<JobDescription> getJobById(@PathVariable Long id) {
        return jobRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/{id}")
    public ResponseEntity<JobDescription> updateJob(@PathVariable Long id, @Valid @RequestBody JobDescription updatedJob) {
        return jobRepository.findById(id)
                .map(job -> {
                    job.setTitle(updatedJob.getTitle());
                    job.setDescription(updatedJob.getDescription());
                    job.setRequirements(updatedJob.getRequirements());
                    return ResponseEntity.ok(jobRepository.save(job));
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @Autowired
    private CandidateScoreRepository scoreRepository;

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        if (!jobRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }


        List<CandidateScore> relatedScores = scoreRepository.findByJobDescriptionIdOrderByScoreDesc(id);
        scoreRepository.deleteAll(relatedScores);


        jobRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}