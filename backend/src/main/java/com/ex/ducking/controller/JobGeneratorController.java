package com.ex.ducking.controller;

import com.ex.ducking.service.LlmService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/generate")
public class JobGeneratorController {

    @Autowired
    private LlmService llmService;

    @PostMapping("/job-description")
    public Map<String, String> generateJobDescription(@RequestBody Map<String, String> request) {
        String roughIdea = request.get("idea");

        String prompt = "Generate a professional, structured job description for the following role. " +
                "IMPORTANT: Return plain text only. Do NOT use markdown formatting like **, ###, ---, or bullet symbols. " +
                "Use simple line breaks and colons for structure instead. " +
                "Also extract 5-8 key technical skills required for this role as a comma-separated list. " +
                "Role idea: " + roughIdea + "\n\n" +
                "Return ONLY a JSON object with exactly these two fields: " +
                "{\"description\": \"<the full job description>\", \"keySkills\": \"<comma-separated skills like Java, Spring Boot, MySQL>\"}. " +
                "No markdown, no extra text outside the JSON.";

        String response = llmService.generateText(prompt);
        String cleaned = response.replaceAll("```json", "").replaceAll("```", "").trim();

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> result = mapper.readValue(cleaned, Map.class);
            return result;
        } catch (Exception e) {

            return Map.of("description", response, "keySkills", "");
        }
    }
}