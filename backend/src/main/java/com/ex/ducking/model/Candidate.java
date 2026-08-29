package com.ex.ducking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table(name = "candidates")
@Data
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Valid email is required")
    private String email;

    @Column(columnDefinition = "TEXT")
    private String skills;

    private Integer experienceYears;

    @Column(columnDefinition = "TEXT")
    private String resumeSummary;
}