package org.example.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String from;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendDriverActivationEmail(String to, String activationLink) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("Driver Activation Email");
        msg.setText(
                "Your driver account has been created.\n\n" +
                "To set a password and activate your account, open the link (valid for 24 hours):\n" +
                activationLink + "\n"
        );

        mailSender.send(msg);
    }

    public void sendRideFinishedEmail(
            String to,
            Long rideId,
            String startAddress,
            String destinationAddress
    ) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("Ride finished");
        msg.setText(
                "Your ride has been completed.\n\n" +
                "Ride ID: " + rideId + "\n" +
                "From: " + startAddress + "\n" +
                "To: " + destinationAddress + "\n\n" +
                "You can rate the ride within 3 days."
        );

        mailSender.send(msg);
    }

    public void sendRegistrationConfirmation(String to, String activationLink) {

    System.out.println(">>> SENDING REGISTRATION MAIL TO: " + to);
    System.out.println(">>> LINK: " + activationLink);

    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setFrom(from);
    msg.setTo(to);
    msg.setSubject("Account Activation");
    msg.setText(
            "Your passenger account has been created.\n\n" +
            "To activate your account, click the link below (valid for 24 hours):\n" +
            activationLink + "\n"
    );

    mailSender.send(msg);

    System.out.println(">>> MAIL SENT SUCCESSFULLY");
}
}