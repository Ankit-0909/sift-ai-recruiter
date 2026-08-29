package com.ex.ducking.controller;


import com.ex.ducking.model.Candidate;
import com.ex.ducking.repository.CandidateRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    @Autowired
    private CandidateRepository candidateRepository;

    @PostMapping
    public Candidate createCandidate(@Valid @RequestBody Candidate candidate) {
        return candidateRepository.save(candidate);
    }

    @GetMapping
    public List<Candidate> getAllCandidates() {
        return candidateRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Candidate> getCandidateById(@PathVariable Long id) {
        return candidateRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCandidate(@PathVariable Long id) {
        if (!candidateRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        candidateRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}