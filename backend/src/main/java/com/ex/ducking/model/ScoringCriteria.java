package com.ex.ducking.model;

import lombok.Data;

@Data
public class ScoringCriteria {
    private String mandatorySkills;
    private String preferredSkills;
    private Integer minExperience;
    private String recruiterNotes;
}