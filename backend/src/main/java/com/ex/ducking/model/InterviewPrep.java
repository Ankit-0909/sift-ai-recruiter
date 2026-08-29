package com.ex.ducking.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "interview_preps")
@Data
public class InterviewPrep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @Column(columnDefinition = "TEXT")
    private String strengths;

    @Column(columnDefinition = "TEXT")
    private String weaknesses;

    @Column(columnDefinition = "TEXT")
    private String suggestedQuestions;
}