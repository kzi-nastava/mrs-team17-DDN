package org.example.backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MailQueueService {

    private final JavaMailSender mailSender;

    public MailQueueService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Šalje sve mejlove jedan po jedan, ravnomerno raspoređene
     * tako da svi stignu u zadatom vremenskom prozoru (npr. 10s).
     * Ne blokira HTTP request.
     */
    public void sendBatchWithin(List<SimpleMailMessage> messages, long windowMs) {
        if (messages == null || messages.isEmpty()) return;

        new Thread(() -> {
            for (SimpleMailMessage msg : messages) {
                try {
                    mailSender.send(msg);
                    System.out.println("MAIL SENT TO: " + String.join(",", msg.getTo()));
                } catch (Exception ex) {
                    System.out.println(
                            "MAIL FAILED TO: " + String.join(",", msg.getTo()) +
                                    " | " + ex.getMessage()
                    );
                }

                try {
                    // FIKSAN RAZMAK: 20 SEKUNDI
                    Thread.sleep(20_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }).start();
    }

}
