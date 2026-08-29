package com.ex.ducking.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddingConfig {

    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel(); // local model, free, no API key
    }

    @Bean
    public EmbeddingStore<dev.langchain4j.data.segment.TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>(); // simple in-memory vector store
    }
}