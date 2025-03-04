package com.fayupable.test.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportTicketProducer {
    private final KafkaTemplate<String, SupportTicketConfirmation> kafkaTemplate;
    private final KafkaTemplate<String, SupportTicketResolutionConfirmation> resolutionKafkaTemplate;


    public void sendConfirmation(SupportTicketConfirmation supportTicketConfirmation) {
        log.info("Sending confirmation: {}", supportTicketConfirmation);
        Message<SupportTicketConfirmation> message = MessageBuilder.withPayload(supportTicketConfirmation)
                .setHeader(KafkaHeaders.TOPIC, "support-ticket-confirmation-topic")
                .setHeader(KafkaHeaders.KEY, supportTicketConfirmation.getSupportTicketId().toString())
                .build();
        kafkaTemplate.send(message);
    }

    public void sendResolutionConfirmation(SupportTicketResolutionConfirmation resolutionConfirmation) {
        log.info("Sending resolution confirmation: {}", resolutionConfirmation);
        Message<SupportTicketResolutionConfirmation> message = MessageBuilder
                .withPayload(resolutionConfirmation)
                .setHeader(KafkaHeaders.TOPIC, "support-ticket-resolution-topic")
                .setHeader(KafkaHeaders.KEY, resolutionConfirmation.getSupportTicketId().toString())
                .build();
        resolutionKafkaTemplate.send(message);
    }
}
