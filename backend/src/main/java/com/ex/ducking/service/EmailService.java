package com.ex.ducking.service;

import com.ex.ducking.model.CandidateScore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private LlmService llmService;

    public String generateOutreachEmail(CandidateScore candidateScore) {
        String prompt = "Write a short, professional outreach email to a shortlisted job candidate. " +
                "Candidate Name: " + candidateScore.getCandidate().getName() + "\n" +
                "Job Title: " + candidateScore.getJobDescription().getTitle() + "\n" +
                "Match reasoning: " + candidateScore.getExplanation() + "\n\n" +
                "The email should: mention interest in their profile, ask them to complete an online assessment, " +
                "and ask them to send their resume. Keep it warm but professional, 4-5 sentences. " +
                "Return plain text only, no markdown, no subject line — just the email body.";

        return llmService.generateText(prompt);
    }

    public void sendOutreachEmail(CandidateScore candidateScore) {
        String emailBody = generateOutreachEmail(candidateScore);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(candidateScore.getCandidate().getEmail());
        message.setSubject("Interested in your profile — " + candidateScore.getJobDescription().getTitle());
        message.setText(emailBody);
        message.setFrom("recruiting@sift-demo.com");

        mailSender.send(message);
    }
}