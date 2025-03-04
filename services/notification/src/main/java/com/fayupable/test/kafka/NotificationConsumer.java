package com.fayupable.test.kafka;

import com.fayupable.test.entity.Notification;
import com.fayupable.test.enums.NotificationType;
import com.fayupable.test.kafka.support.SupportTicketConfirmation;
import com.fayupable.test.kafka.support.SupportTicketResolutionConfirmation;
import com.fayupable.test.kafka.user.UserConfirmation;
import com.fayupable.test.repository.INotificationRepository;
import com.fayupable.test.service.IEmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {
    private final IEmailService emailService;
    private final INotificationRepository notificationRepository;


    @KafkaListener(
            topics = "user-confirmation-topic",
            groupId = "user-group",
            containerFactory = "userKafkaListenerContainerFactory"
    )
    public void consumeUserConfirmation(UserConfirmation userConfirmation) {
        log.info("Consuming user confirmation message: {}", userConfirmation);
        saveUserConfirmation(userConfirmation);
        try {
            sendUserConfirmationEmail(userConfirmation);
        } catch (MessagingException e) {
            log.error("MessagingException occurred while sending user confirmation email to user {}", userConfirmation.getEmail(), e);
            throw new RuntimeException("Failed to send user confirmation email due to messaging error", e);
        }

    }

    private void saveUserConfirmation(UserConfirmation userConfirmation) {
        log.info("Saving user confirmation notification for user {}", userConfirmation.getEmail());
        notificationRepository.save(
                Notification.builder()
                        .notificationType(NotificationType.USER_VERIFICATION)
                        .sendAt(LocalDateTime.now())
                        .build()
        );
        log.info("User confirmation notification saved for user {}", userConfirmation.getEmail());
    }

    private void sendUserConfirmationEmail(UserConfirmation userConfirmation) throws MessagingException {
        log.info("Sending user confirmation email to user {}", userConfirmation.getEmail());
        emailService.sendUserVerificationCode(userConfirmation.getEmail(), userConfirmation.getVerificationCode(), userConfirmation.getVerificationCodeExpiration());
        log.info("User confirmation email sent to user {}", userConfirmation.getEmail());
    }

    @KafkaListener(
            topics = "support-ticket-confirmation-topic",
            groupId = "support-group",
            containerFactory = "supportKafkaListenerContainerFactory"
    )
    public void consumeSupportTicketConfirmation(SupportTicketConfirmation supportTicketConfirmation) {
        log.info("Consuming support ticket confirmation message: {}", supportTicketConfirmation);
        saveSupportTicketConfirmation(supportTicketConfirmation);
        try {
            sendSupportTicketConfirmationEmail(supportTicketConfirmation);
        } catch (MessagingException e) {
            log.error("MessagingException occurred while sending support ticket confirmation email to user {}", supportTicketConfirmation.getEmail(), e);
            throw new RuntimeException("Failed to send support ticket confirmation email due to messaging error", e);
        }
    }

    private void saveSupportTicketConfirmation(SupportTicketConfirmation supportTicketConfirmation) {
        log.info("Saving support ticket confirmation notification for user {}", supportTicketConfirmation.getEmail());
        notificationRepository.save(
                Notification.builder()
                        .notificationType(NotificationType.SUPPORT_CONFIRMATION)
                        .sendAt(LocalDateTime.now())
                        .build()
        );
        log.info("Support ticket confirmation notification saved for user {}", supportTicketConfirmation.getEmail());
    }

    private void sendSupportTicketConfirmationEmail(SupportTicketConfirmation supportTicketConfirmation) throws MessagingException {
        log.info("Sending support ticket confirmation email to user {}", supportTicketConfirmation.getEmail());
        emailService.sendSupportTicket(supportTicketConfirmation.getSupportTicketId(), supportTicketConfirmation.getEmail(), supportTicketConfirmation.getStatus());
        log.info("Support ticket confirmation email sent to user {}", supportTicketConfirmation.getEmail());
    }


    @KafkaListener(
            topics = "support-ticket-resolution-topic",
            groupId = "support-group",
            containerFactory = "supportTicketResolutionKafkaListenerContainerFactory"
    )
    public void consumeSupportTicketResolutionConfirmation(SupportTicketResolutionConfirmation supportTicketResolutionConfirmation) {
        log.info("Consuming support ticket resolution confirmation message: {}", supportTicketResolutionConfirmation);
        saveSupportTicketResolutionConfirmation(supportTicketResolutionConfirmation);
        try {
            sendSupportTicketResolutionConfirmationEmail(supportTicketResolutionConfirmation);
        } catch (MessagingException e) {
            log.error("MessagingException occurred while sending support ticket resolution confirmation email to user {}", supportTicketResolutionConfirmation.getEmail(), e);
            throw new RuntimeException("Failed to send support ticket resolution confirmation email due to messaging error", e);
        }
    }

    private void saveSupportTicketResolutionConfirmation(SupportTicketResolutionConfirmation supportTicketResolutionConfirmation) {
        log.info("Saving support ticket resolution confirmation notification for user {}", supportTicketResolutionConfirmation.getEmail());
        notificationRepository.save(
                Notification.builder()
                        .notificationType(NotificationType.SUPPORT_RESOLUTION_CONFIRMATION)
                        .sendAt(LocalDateTime.now())
                        .build()
        );
        log.info("Support ticket resolution confirmation notification saved for user {}", supportTicketResolutionConfirmation.getEmail());
    }

    private void sendSupportTicketResolutionConfirmationEmail(SupportTicketResolutionConfirmation supportTicketResolutionConfirmation) throws MessagingException {
        log.info("Sending support ticket resolution confirmation email to user {}", supportTicketResolutionConfirmation.getEmail());
        emailService.sendSupportResolution(supportTicketResolutionConfirmation.getSupportTicketId(), supportTicketResolutionConfirmation.getEmail(), supportTicketResolutionConfirmation.getResolutionMessage(), supportTicketResolutionConfirmation.getResolvedAt(), supportTicketResolutionConfirmation.getStatus());
        log.info("Support ticket resolution confirmation email sent to user {}", supportTicketResolutionConfirmation.getEmail());
    }


}
