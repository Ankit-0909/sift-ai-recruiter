package com.ex.ducking.service;


import com.ex.ducking.model.Candidate;
import com.ex.ducking.repository.CandidateRepository;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CandidateEmbeddingService {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    private CandidateRepository candidateRepository;


    public String embedAllCandidates() {
        List<Candidate> candidates = candidateRepository.findAll();
        embeddingStore.removeAll();

        for (Candidate c : candidates) {
            String text = "Primary Skills: " + c.getSkills() + ". " +
                    "Role: " + c.getName() + " has " + c.getExperienceYears() + " years experience in " + c.getSkills() + ". " +
                    c.getResumeSummary();

            TextSegment segment = TextSegment.from(text, Metadata.from("candidateId", String.valueOf(c.getId())));
            Embedding embedding = embeddingModel.embed(segment).content();
            embeddingStore.add(embedding, segment);
        }

        return "Embedded " + candidates.size() + " candidates.";
    }


    public List<Long> findTopMatchingCandidateIds(String jobDescriptionText, int topN, double minScore) {
        Embedding queryEmbedding = embeddingModel.embed(jobDescriptionText).content();

        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(topN)
                .minScore(minScore)
                .build();

        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);

        return result.matches().stream()
                .map(match -> Long.valueOf(match.embedded().metadata().getString("candidateId")))
                .collect(Collectors.toList());
    }
    public List<String> findMatchesWithScores(String jobDescriptionText) {
        Embedding queryEmbedding = embeddingModel.embed(jobDescriptionText).content();

        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(10)
                .build();

        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);

        return result.matches().stream()
                .map(match -> "ID: " + match.embedded().metadata().getString("candidateId") +
                        " | Score: " + match.score())
                .collect(Collectors.toList());
    }
}