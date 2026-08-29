package com.ex.ducking.config;

import com.ex.ducking.service.CandidateEmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CandidateEmbeddingService embeddingService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("App started — auto-generating candidate embeddings...");
        String result = embeddingService.embedAllCandidates();
        System.out.println(result);
    }
}