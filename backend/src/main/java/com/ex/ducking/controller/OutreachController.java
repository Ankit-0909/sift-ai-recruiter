package com.ex.ducking.controller;

import com.ex.ducking.model.CandidateScore;
import com.ex.ducking.repository.CandidateScoreRepository;
import com.ex.ducking.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/outreach")
public class OutreachController {

    @Autowired
    private CandidateScoreRepository scoreRepository;

    @Autowired
    private EmailService emailService;


    @GetMapping("/shortlisted/{jobId}")
    public List<CandidateScore> getShortlisted(@PathVariable Long jobId,
                                                 @RequestParam(defaultValue = "75") int threshold) {
        return scoreRepository.findByJobDescriptionIdAndScoreGreaterThanEqual(jobId, threshold);
    }


    @PostMapping("/send/{scoreId}")
    public CandidateScore sendOutreach(@PathVariable Long scoreId) {
        CandidateScore score = scoreRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("Score record not found"));

        if (score.isContacted()) {
            throw new RuntimeException("Already contacted this candidate");
        }

        emailService.sendOutreachEmail(score);

        score.setContacted(true);
        score.setContactedAt(java.time.LocalDateTime.now().toString());
        return scoreRepository.save(score);
    }
}