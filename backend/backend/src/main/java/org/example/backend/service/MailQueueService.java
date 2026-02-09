package org.example.backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MailQueueService {

    private final JavaMailSender mailSender;
    private static final Pattern LINK_PATTERN = Pattern.compile("(https?://\\S+|taximobile://\\S+)");

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
                if (msg == null) {
                    continue;
                }
                try {
                    sendAsMime(msg);
                    System.out.println("MAIL SENT TO: " + recipientsOf(msg));
                } catch (Exception ex) {
                    System.out.println(
                            "MAIL FAILED TO: " + recipientsOf(msg) +
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

    private void sendAsMime(SimpleMailMessage source) throws Exception {
        var mime = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(mime, true, "UTF-8");

        if (source.getFrom() != null && !source.getFrom().isBlank()) helper.setFrom(source.getFrom());
        if (source.getTo() != null && source.getTo().length > 0) helper.setTo(source.getTo());
        if (source.getCc() != null && source.getCc().length > 0) helper.setCc(source.getCc());
        if (source.getBcc() != null && source.getBcc().length > 0) helper.setBcc(source.getBcc());
        if (source.getReplyTo() != null && !source.getReplyTo().isBlank()) helper.setReplyTo(source.getReplyTo());
        if (source.getSubject() != null) helper.setSubject(source.getSubject());

        String plainText = source.getText() == null ? "" : source.getText();
        helper.setText(plainText, toHtml(plainText));

        mailSender.send(mime);
    }

    private String toHtml(String plainText) {
        StringBuilder out = new StringBuilder();
        Matcher matcher = LINK_PATTERN.matcher(plainText);
        int cursor = 0;

        while (matcher.find()) {
            out.append(escapeAndBreaks(plainText.substring(cursor, matcher.start())));
            String url = matcher.group(1);
            String escapedUrl = HtmlUtils.htmlEscape(url);
            out.append("<a href=\"").append(escapedUrl).append("\">")
                    .append(escapedUrl)
                    .append("</a>");
            cursor = matcher.end();
        }

        out.append(escapeAndBreaks(plainText.substring(cursor)));
        return "<html><body>" + out + "</body></html>";
    }

    private String escapeAndBreaks(String text) {
        return HtmlUtils.htmlEscape(text).replace("\n", "<br/>");
    }

    private String recipientsOf(SimpleMailMessage msg) {
        String[] to = msg.getTo();
        if (to == null || to.length == 0) return "<no-recipient>";
        return String.join(",", to);
    }

}
