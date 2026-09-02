package com.ex.ducking.controller;


import com.ex.ducking.model.InterviewPrep;
import com.ex.ducking.repository.InterviewPrepRepository;
import com.ex.ducking.service.InterviewPrepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/interview-prep")
public class InterviewPrepController {

    @Autowired
    private InterviewPrepService prepService;

    @Autowired
    private InterviewPrepRepository prepRepository;

    @PostMapping("/generate/{candidateId}")
    public InterviewPrep generatePrep(@PathVariable Long candidateId, @RequestParam Long jobId) {
        return prepService.generatePrep(candidateId,jobId);
    }


    @GetMapping("/candidate/{candidateId}")
    public List<InterviewPrep> getPrepsForCandidate(@PathVariable Long candidateId) {
        return prepRepository.findByCandidateId(candidateId);
    }
}