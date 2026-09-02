package com.ex.ducking.service;

import com.ex.ducking.model.JobDescription;
import com.ex.ducking.repository.JobDescriptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ex.ducking.model.Candidate;
import com.ex.ducking.model.InterviewPrep;
import com.ex.ducking.repository.CandidateRepository;
import com.ex.ducking.repository.InterviewPrepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class InterviewPrepService {

    @Autowired
    private LlmService llmService;

    @Autowired
    private JobDescriptionRepository jobRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private InterviewPrepRepository prepRepository;

    public InterviewPrep generatePrep(Long candidateId,Long jobId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        JobDescription job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        String prompt = "You are preparing an interviewer for a candidate interview. " +
                "Based on the job requirements and the candidate's profile below, generate a tailored interview prep briefing. " +
                "Focus the weaknesses and questions specifically on gaps relevant to THIS role's requirements, not generic ones.\n\n" +
                "Job Title: " + job.getTitle() + "\n" +
                "Job Key Skills: " + job.getKeySkills() + "\n\n" +
                "Candidate Name: " + candidate.getName() + "\n" +
                "Candidate Skills: " + candidate.getSkills() + "\n" +
                "Candidate Experience: " + candidate.getExperienceYears() + " years\n" +
                "Candidate Summary: " + candidate.getResumeSummary() + "\n\n" +
                "Return ONLY a JSON object with exactly these fields: " +
                "{\"strengths\": \"<2-3 sentence summary of strengths relevant to this role>\", " +
                "\"weaknesses\": \"<2-3 sentence summary of gaps to probe, specific to this role's requirements>\", " +
                "\"suggestedQuestions\": \"<3 specific interview questions tailored to this role, numbered 1-3>\"}. " +
                "No markdown, no extra text.";

        String response = llmService.generateText(prompt);
        String cleaned = response.replaceAll("```json", "").replaceAll("```", "").trim();

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> result = mapper.readValue(cleaned, Map.class);


            List<InterviewPrep> existing = prepRepository.findByCandidateId(candidateId);
            InterviewPrep prep = existing.isEmpty() ? new InterviewPrep() : existing.get(0);

            prep.setCandidate(candidate);
            prep.setStrengths((String) result.get("strengths"));
            prep.setWeaknesses((String) result.get("weaknesses"));
            prep.setSuggestedQuestions((String) result.get("suggestedQuestions"));

            return prepRepository.save(prep);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI prep response: " + response, e);
        }
    }
}