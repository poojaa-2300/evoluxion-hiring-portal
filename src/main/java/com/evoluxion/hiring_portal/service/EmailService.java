package com.evoluxion.hiring_portal.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends OTP for candidate email verification.
     */
    public void sendOtp(
            String email,
            String name,
            int otp) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(email);

        message.setSubject(
                "Hiring Portal - Candidate OTP Verification");

        message.setText(
                "Hello " + name + ",\n\n"
                + "Thank you for registering with the Hiring Portal.\n\n"
                + "Your email verification OTP is: "
                + otp + "\n\n"
                + "This OTP is valid for 5 minutes.\n\n"
                + "Please do not share this OTP with anyone.\n\n"
                + "If you did not register on the Hiring Portal, "
                + "please ignore this email.\n\n"
                + "Regards,\n"
                + "Hiring Portal Team"
        );

        mailSender.send(message);
    }

    /**
     * Sends OTP for password reset.
     * We will use this later when we add Forgot Password.
     */
    public void sendPasswordResetOtp(
            String email,
            String name,
            int otp) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(email);

        message.setSubject(
                "Hiring Portal - Password Reset OTP");

        message.setText(
                "Hello " + name + ",\n\n"
                + "Your password reset OTP is: "
                + otp + "\n\n"
                + "This OTP is valid for 5 minutes.\n\n"
                + "If you did not request a password reset, "
                + "please ignore this email.\n\n"
                + "Regards,\n"
                + "Hiring Portal Team"
        );

        mailSender.send(message);
    }
}