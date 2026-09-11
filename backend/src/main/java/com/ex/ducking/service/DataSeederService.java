package com.ex.ducking.service;


import com.ex.ducking.model.Candidate;
import com.ex.ducking.repository.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
public class DataSeederService {

    @Autowired
    private LlmService llmService;

    @Autowired
    private CandidateRepository candidateRepository;

    public String seedCandidates(int count) {
        String prompt = "Generate " + count + " realistic fake candidate profiles for a software job portal, in JSON array format. " +
                "Each object must have exactly these fields: name (string), email (string), skills (comma-separated string like 'Java, Spring Boot, SQL'), " +
                "experienceYears (integer), resumeSummary (2-3 sentence string). " +
                "Return ONLY the JSON array, no extra text, no markdown formatting.";

        String response = llmService.generateText(prompt);

        try {

            String cleaned = response.replaceAll("```json", "").replaceAll("```", "").trim();

            ObjectMapper mapper = new ObjectMapper();
            List<Candidate> candidates = mapper.readValue(cleaned, mapper.getTypeFactory().constructCollectionType(List.class, Candidate.class));

            candidateRepository.saveAll(candidates);
            return "Seeded " + candidates.size() + " candidates successfully.";
        } catch (Exception e) {
            return "Error parsing candidates: " + e.getMessage() + " | Raw response: " + response;
        }
    }
    @Autowired
    private CandidateEmbeddingService embeddingService;

    public Candidate addCandidateFromText(String name, String email, Integer experienceYears, String rawText) {
        String prompt = "Extract skills and write a short professional resume summary based on this candidate description. " +
                "Return ONLY a JSON object with exactly these fields: " +
                "{\"skills\": \"<comma-separated list of skills, e.g. Java, Spring Boot, MySQL>\", " +
                "\"resumeSummary\": \"<2-3 sentence professional summary>\"}. " +
                "No markdown, no extra text.\n\n" +
                "Candidate description: " + rawText;

        String response = llmService.generateText(prompt);
        String cleaned = response.replaceAll("```json", "").replaceAll("```", "").trim();

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> result = mapper.readValue(cleaned, Map.class);

            Candidate candidate = new Candidate();
            candidate.setName(name);
            candidate.setEmail(email);
            candidate.setExperienceYears(experienceYears);
            candidate.setSkills(result.get("skills"));
            candidate.setResumeSummary(result.get("resumeSummary"));

            Candidate saved = candidateRepository.save(candidate);
            embeddingService.embedSingleCandidate(saved);
            return saved;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI response for candidate: " + response, e);
        }
    }
}