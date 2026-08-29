package com.ex.ducking.service;

import com.ex.ducking.model.Candidate;
import com.ex.ducking.model.CandidateScore;
import com.ex.ducking.model.JobDescription;
import com.ex.ducking.repository.CandidateRepository;
import com.ex.ducking.repository.CandidateScoreRepository;
import com.ex.ducking.repository.JobDescriptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ScoringService {

    @Autowired
    private LlmService llmService;

    @Autowired
    private JobDescriptionRepository jobRepository;

    @Autowired
    private CandidateEmbeddingService embeddingService;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private CandidateScoreRepository scoreRepository;

    public CandidateScore scoreCandidate(Long jobId, Long candidateId) {
        JobDescription job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        String prompt = "You are scoring a candidate against a job description. " +
                "The candidate profile is brief and may not mention every requirement — " +
                "score based on how well the AVAILABLE information aligns with the role, not on missing information alone. " +
                "Job Title: " + job.getTitle() + "\n" +
                "Job Description: " + job.getDescription() + "\n\n" +
                "Candidate Name: " + candidate.getName() + "\n" +
                "Candidate Skills: " + candidate.getSkills() + "\n" +
                "Candidate Experience: " + candidate.getExperienceYears() + " years\n" +
                "Candidate Summary: " + candidate.getResumeSummary() + "\n\n" +
                "Return ONLY a JSON object with exactly these fields: " +
                "{\"score\": <integer 0-100>, \"explanation\": \"<2-3 sentence reasoning covering what matches and what's missing>\"}. " +
                "No markdown, no extra text.";

        String response = llmService.generateText(prompt);
        String cleaned = response.replaceAll("```json", "").replaceAll("```", "").trim();

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> result = mapper.readValue(cleaned, Map.class);


            List<CandidateScore> existing = scoreRepository.findByJobDescriptionIdAndCandidateId(jobId, candidateId);
            CandidateScore score = existing.isEmpty() ? new CandidateScore() : existing.get(0);



            score.setJobDescription(job);
            score.setCandidate(candidate);
            score.setScore((Integer) result.get("score"));
            score.setExplanation((String) result.get("explanation"));

            return scoreRepository.save(score);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI scoring response: " + response, e);
        }
    }

    public List<CandidateScore> scoreAllCandidatesForJob(Long jobId) {
        JobDescription job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));


        String searchText = (job.getKeySkills() != null && !job.getKeySkills().isBlank())
                ? job.getKeySkills()
                : job.getDescription();

        List<Long> relevantCandidateIds;
        try {
            relevantCandidateIds = embeddingService.findTopMatchingCandidateIds(
                    searchText, 5, 0.62
            );
        } catch (Exception e) {
            System.out.println("RAG retrieval failed, falling back to all candidates: " + e.getMessage());
            relevantCandidateIds = new java.util.ArrayList<>();
        }

        List<Candidate> candidatesToScore;
        if (relevantCandidateIds.isEmpty()) {
            candidatesToScore = candidateRepository.findAll();
        } else {
            candidatesToScore = candidateRepository.findAllById(relevantCandidateIds);
        }

        List<CandidateScore> results = new java.util.ArrayList<>();

        for (Candidate candidate : candidatesToScore) {
            try {
                results.add(scoreCandidate(jobId, candidate.getId()));
                Thread.sleep(4000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.out.println("Skipped candidate " + candidate.getId() + ": " + e.getMessage());
            }
        }

        return results;
    }
}