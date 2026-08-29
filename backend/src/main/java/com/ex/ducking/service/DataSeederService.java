package com.ex.ducking.service;


import com.ex.ducking.model.Candidate;
import com.ex.ducking.repository.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

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
}