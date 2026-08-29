package com.ex.ducking.controller;


import com.ex.ducking.model.CandidateScore;
import com.ex.ducking.repository.CandidateRepository;
import com.ex.ducking.repository.CandidateScoreRepository;
import com.ex.ducking.service.ScoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/scoring")
public class ScoringController {

    @Autowired
    private ScoringService scoringService;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private CandidateScoreRepository scoreRepository;


    @PostMapping("/score")
    public CandidateScore scoreOne(@RequestParam Long jobId, @RequestParam Long candidateId) {
        return scoringService.scoreCandidate(jobId, candidateId);
    }


    @PostMapping("/score-all/{jobId}")
    public Map<String, Object> scoreAllForJob(@PathVariable Long jobId) {
        List<CandidateScore> scores = scoringService.scoreAllCandidatesForJob(jobId);
        long totalCandidates = candidateRepository.count();

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("scores", scores);
        response.put("totalCandidatesInPool", totalCandidates);
        response.put("candidatesScored", scores.size());

        return response;
    }


    @GetMapping("/job/{jobId}")
    public List<CandidateScore> getScoresForJob(@PathVariable Long jobId) {
        return scoreRepository.findByJobDescriptionIdOrderByScoreDesc(jobId);
    }
}