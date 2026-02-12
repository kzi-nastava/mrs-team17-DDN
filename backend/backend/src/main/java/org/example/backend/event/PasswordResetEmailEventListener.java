package org.example.backend.event;

import org.example.backend.service.MailService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetEmailEventListener {

    private final MailService mailService;

    public PasswordResetEmailEventListener(MailService mailService) {
        this.mailService = mailService;
    }

    @EventListener
    public void onPasswordResetEmailEvent(PasswordResetEmailEvent event) {
        mailService.sendPasswordResetEmail(event.email(), event.link());
    }
}
