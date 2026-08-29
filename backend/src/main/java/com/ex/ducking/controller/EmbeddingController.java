package com.ex.ducking.controller;


import com.ex.ducking.service.CandidateEmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/embeddings")
public class EmbeddingController {

    @Autowired
    private CandidateEmbeddingService embeddingService;

    @PostMapping("/embed-candidates")
    public String embedCandidates() {
        return embeddingService.embedAllCandidates();
    }

    @GetMapping("/match")
    public List<Long> findMatches(@RequestParam String jobDescription,
                                  @RequestParam(defaultValue = "5") int topN,
                                  @RequestParam(defaultValue = "0.5") double minScore) {
        return embeddingService.findTopMatchingCandidateIds(jobDescription, topN, minScore);
    }
    @GetMapping("/match-debug")
    public List<String> findMatchesWithScores(@RequestParam String jobDescription) {
        return embeddingService.findMatchesWithScores(jobDescription);
    }
}