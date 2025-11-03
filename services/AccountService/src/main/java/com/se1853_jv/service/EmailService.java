package com.se1853_jv.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send email with custom subject and content
     */
    public void sendEmail(String toEmail, String fullName, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        
        String htmlContent = buildEmailTemplate(fullName, content);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    /**
     * Send new password email (backward compatibility)
     */
    public void sendNewPassword(String toEmail, String fullName, String newPassword) throws MessagingException {
        String content = "Here is your new temporary password: <strong>" + newPassword + "</strong><br><br>"
                + "Please change your password immediately after logging in.";
        sendEmail(toEmail, fullName, "LabVerse - Password Reset", content);
    }

    /**
     * Common email template
     */
    private String buildEmailTemplate(String fullName, String content) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<body style='font-family: Arial, sans-serif; padding: 20px;'>"
                + "<p>Hello " + fullName + ",</p>"
                + "<div style='margin: 20px 0;'>" + content + "</div>"
                + "<br>"
                + "<p>Best regards,<br>LabVerse Team</p>"
                + "</body>"
                + "</html>";
    }
}








