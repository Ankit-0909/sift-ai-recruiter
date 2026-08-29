package com.ex.ducking.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "candidate_scores")
@Data
public class CandidateScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private JobDescription jobDescription;

    @ManyToOne
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    private Integer score;

    private boolean contacted = false;

    private String contactedAt;

    @Column(columnDefinition = "TEXT")
    private String explanation;
}