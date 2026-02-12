package org.example.backend.event;

import org.example.backend.service.MailService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
public class RegistrationEmailListener {

    private final MailService mailService;

    public RegistrationEmailListener(MailService mailService) {
        this.mailService = mailService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRegistrationEmail(RegistrationEmailEvent e) {
        mailService.sendRegistrationConfirmation(e.email(), e.link());
    }
}
