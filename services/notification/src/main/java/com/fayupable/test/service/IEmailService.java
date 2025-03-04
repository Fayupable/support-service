package com.fayupable.test.service;

import com.fayupable.test.kafka.support.SupportStatus;
import jakarta.mail.MessagingException;
import org.springframework.scheduling.annotation.Async;

import java.time.LocalDateTime;
import java.util.UUID;

public interface IEmailService {
    void sendUserVerificationCode(String destinationEmail, String verificationCode, LocalDateTime verificationExpiredDate) throws MessagingException;

    void sendSupportTicket (UUID supportTicketId, String email, SupportStatus status);

    void sendSupportResolution(UUID supportTicketId, String email, String resolutionMessage, LocalDateTime resolvedAt, SupportStatus status);
}
