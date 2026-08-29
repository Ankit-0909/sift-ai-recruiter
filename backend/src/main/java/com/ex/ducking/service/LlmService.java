package com.ex.ducking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class LlmService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    public String generateText(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        Map<String, Object> requestBody = Map.of(
                "model", "openai/gpt-oss-120b",
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        Map response = restTemplate.postForObject(apiUrl, request, Map.class);

        List<Map> choices = (List<Map>) response.get("choices");
        Map message = (Map) choices.get(0).get("message");
        return (String) message.get("content");
    }
}