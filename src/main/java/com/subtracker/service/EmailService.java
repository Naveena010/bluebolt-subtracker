package com.subtracker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    // In a real project this would call an SMTP provider (Gmail/SendGrid/AWS SES).
    // For practice, we simulate the alert instead of actually sending it.
    public void sendAlertEmail(String to, String subject, String body) {
        try {
            logger.info("Simulated email sent -> To: {}, Subject: {}, Body: {}", to, subject, body);
        } catch (Exception e) {
            logger.error("Failed to simulate alert email to {}: {}", to, e.getMessage());
        }
    }
}