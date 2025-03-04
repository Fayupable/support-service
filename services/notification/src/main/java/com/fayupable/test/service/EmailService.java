package com.fayupable.test.service;

import com.fayupable.test.kafka.support.SupportStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.fayupable.test.enums.EmailTemplate.SUPPORT_TICKET_RESOLUTION_CONFIRMATION;
import static com.fayupable.test.enums.EmailTemplate.USER_VERIFICATION;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService implements IEmailService {
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Override
    @Async
    public void sendUserVerificationCode(String destinationEmail, String verificationCode, LocalDateTime verificationExpiredDate) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper;

        try {
            messageHelper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_RELATED, StandardCharsets.UTF_8.name());
            messageHelper.setFrom("fayupable@gmail.com");
            messageHelper.setTo(destinationEmail);
            messageHelper.setSubject(USER_VERIFICATION.getSubject());

            final String templateName = USER_VERIFICATION.getTemplate();
            Map<String, Object> variables = new HashMap<>();
            variables.put("verificationCode", verificationCode);
            variables.put("verificationCodeExpiration", verificationExpiredDate);

            Context context = new Context();
            context.setVariables(variables);

            String htmlTemplate = templateEngine.process(templateName, context);
            messageHelper.setText(htmlTemplate, true);

            javaMailSender.send(mimeMessage);
            log.info("Email sent to {} with template {}", destinationEmail, templateName);

        } catch (MessagingException e) {
            log.error("MessagingException occurred while sending email to {}", destinationEmail, e);
            throw new RuntimeException("Failed to send email due to messaging error", e);
        } catch (MailException e) {
            log.error("MailException occurred while sending email to {}", destinationEmail, e);
            throw new RuntimeException("Failed to send email due to mail error", e);
        }
    }


    @Override
    @Async
    public void sendSupportTicket(UUID supportTicketId, String email, SupportStatus status) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper;

        try {
            messageHelper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_RELATED, StandardCharsets.UTF_8.name());
            messageHelper.setFrom("fayupable@gmail.com");
            messageHelper.setTo(email);
            messageHelper.setSubject(SUPPORT_TICKET_RESOLUTION_CONFIRMATION.getSubject());

            final String templateName = SUPPORT_TICKET_RESOLUTION_CONFIRMATION.getTemplate();
            Map<String, Object> variables = new HashMap<>();
            variables.put("ticketId", supportTicketId);
            variables.put("status", status);
            variables.put("priority", "HIGH");

            Context context = new Context();
            context.setVariables(variables);

            String htmlTemplate = templateEngine.process(templateName, context);
            messageHelper.setText(htmlTemplate, true);

            javaMailSender.send(mimeMessage);
            log.info("Email sent to {} with template {}", email, templateName);

        } catch (MessagingException e) {
            log.error("Error sending support ticket email to {}", email, e);
            throw new RuntimeException("Failed to send support ticket email", e);
        } catch (MailException e) {
            log.error("Mail error while sending support ticket email to {}", email, e);
            throw new RuntimeException("Failed to send support ticket email", e);
        }
    }


    @Override
    @Async
    public void sendSupportResolution(UUID supportTicketId, String email, String resolutionMessage, LocalDateTime resolvedAt, SupportStatus status) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper;

        try {
            messageHelper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_RELATED, StandardCharsets.UTF_8.name());
            messageHelper.setFrom("fayupable@gmail.com");
            messageHelper.setTo(email);
            messageHelper.setSubject("Support Ticket Resolution");

            Map<String, Object> variables = new HashMap<>();
            variables.put("ticketId", supportTicketId);
            variables.put("status", status);
            variables.put("priority", "HIGH");
            variables.put("resolutionMessage", resolutionMessage);
            variables.put("resolvedAt", resolvedAt);

            Context context = new Context();
            context.setVariables(variables);

            String htmlTemplate = templateEngine.process("SupportTicketResolution", context);
            messageHelper.setText(htmlTemplate, true);

            javaMailSender.send(mimeMessage);
            log.info("Support ticket resolution email sent to {}", email);

        } catch (MessagingException e) {
            log.error("Error sending support ticket resolution email to {}", email, e);
            throw new RuntimeException("Failed to send support ticket resolution email", e);
        } catch (MailException e) {
            log.error("Mail error while sending support ticket resolution email to {}", email, e);
            throw new RuntimeException("Failed to send support ticket resolution email", e);
        }

    }

}
