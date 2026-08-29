package com.ex.ducking.service;

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
    private CandidateRepository candidateRepository;

    @Autowired
    private InterviewPrepRepository prepRepository;

    public InterviewPrep generatePrep(Long candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        String prompt = "You are preparing an interviewer for a candidate interview. " +
                "Based on the candidate's profile below, generate an interview prep briefing. " +
                "Candidate Name: " + candidate.getName() + "\n" +
                "Skills: " + candidate.getSkills() + "\n" +
                "Experience: " + candidate.getExperienceYears() + " years\n" +
                "Summary: " + candidate.getResumeSummary() + "\n\n" +
                "Return ONLY a JSON object with exactly these fields: " +
                "{\"strengths\": \"<2-3 sentence summary of strengths>\", " +
                "\"weaknesses\": \"<2-3 sentence summary of potential gaps or areas to probe>\", " +
                "\"suggestedQuestions\": \"<3 specific interview questions, numbered 1-3>\"}. " +
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