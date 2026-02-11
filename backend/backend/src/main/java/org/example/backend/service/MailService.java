package org.example.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final MailQueueService mailQueueService;
    @Value("${app.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;
    @Value("${app.mobile.deep-link-base:taximobile://user/ride-tracking}")
    private String mobileDeepLinkBase;


    @Value("${spring.mail.from}")
    private String from;

    public MailService(JavaMailSender mailSender, MailQueueService mailQueueService) {
        this.mailSender = mailSender;
        this.mailQueueService = mailQueueService;
    }

    public void sendDriverActivationEmail(String to, String webLink, String androidLink, int validHours) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("Driver Activation Email");
        msg.setText(
                "Your driver account has been created.\n\n" +
                        "To set a password and activate your account, open one of the links below (valid for " + validHours + " hours):\n\n" +
                        "Web: " + webLink + "\n" +
                        "Open in Android app: " + androidLink + "\n"
        );

        mailQueueService.sendNow(msg);
    }

    public SimpleMailMessage buildRideFinishedEmail(
            String to,
            Long rideId,
            String startAddress,
            String destinationAddress
    ) {
        String webTrackingLink = buildWebTrackingLink(rideId);
        String mobileTrackingLink = buildMobileTrackingLink(rideId);
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("Ride finished");
        msg.setText(
                "Your ride has been completed.\n\n" +
                        "Ride ID: " + rideId + "\n" +
                        "From: " + startAddress + "\n" +
                        "To: " + destinationAddress + "\n\n" +
                        "You can rate the ride within 3 days.\n\n" +
                        "Web details: " + webTrackingLink + "\n" +
                        "Open in Android app: " + mobileTrackingLink + "\n"
        );
        return msg;
    }


    public SimpleMailMessage buildRideAcceptedEmail(
            String to, Long rideId, String startAddress, String destinationAddress
    ) {
        String webTrackingLink = buildWebTrackingLink(rideId);
        String mobileTrackingLink = buildMobileTrackingLink(rideId);
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("Ride accepted");
        msg.setText(
                "You were added to a ride and a driver has accepted it.\n\n" +
                        "Ride ID: " + rideId + "\n" +
                        "From: " + startAddress + "\n" +
                        "To: " + destinationAddress + "\n\n" +
                        "Track the ride (web): " + webTrackingLink + "\n" +
                        "Open in Android app: " + mobileTrackingLink + "\n"
        );
        return msg;
    }

    public void sendRideAcceptedEmail(
            String to,
            Long rideId,
            String startAddress,
            String destinationAddress
    ) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("Ride accepted");
        String webTrackingLink = buildWebTrackingLink(rideId);
        String mobileTrackingLink = buildMobileTrackingLink(rideId);
        msg.setText(
                "You were added to a ride and a driver has accepted it.\n\n" +
                        "Ride ID: " + rideId + "\n" +
                        "From: " + startAddress + "\n" +
                        "To: " + destinationAddress + "\n\n" +
                        "Track the ride (web): " + webTrackingLink + "\n" +
                        "Open in Android app: " + mobileTrackingLink + "\n"
        );
        mailSender.send(msg);
    }


    public void sendRideFinishedEmail(
            String to,
            Long rideId,
            String startAddress,
            String destinationAddress
    ) {
        String webTrackingLink = buildWebTrackingLink(rideId);
        String mobileTrackingLink = buildMobileTrackingLink(rideId);
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("Ride finished");
        msg.setText(
                "Your ride has been completed.\n\n" +
                        "Ride ID: " + rideId + "\n" +
                        "From: " + startAddress + "\n" +
                        "To: " + destinationAddress + "\n\n" +
                        "You can rate the ride within 3 days.\n\n" +
                        "Web details: " + webTrackingLink + "\n" +
                        "Open in Android app: " + mobileTrackingLink + "\n"
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

    private String buildWebTrackingLink(Long rideId) {
        return frontendBaseUrl + "/user/ride-tracking?rideId=" + rideId;
    }

    private String buildMobileTrackingLink(Long rideId) {
        String base = mobileDeepLinkBase == null ? "" : mobileDeepLinkBase.trim();
        if (base.isEmpty()) {
            base = "taximobile://user/ride-tracking";
        }
        return base + (base.contains("?") ? "&" : "?") + "rideId=" + rideId;
    }
}



    public void sendPasswordResetEmail(String to, String resetLink) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("Password reset");
        msg.setText(
                "We received a request to reset your password.\n\n" +
                        "Set a new password using this link (valid for 24 hours):\n" +
                        resetLink + "\n\n" +
                        "If you did not request this, you can ignore this email."
        );

        mailSender.send(msg);
    }
}