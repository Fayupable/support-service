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
public class UserProducer {
    private final KafkaTemplate<String, UserConfirmation> kafkaTemplate;

    public void sendConfirmation(UserConfirmation userConfirmation) {
        log.info("Sending confirmation: {}", userConfirmation);
        Message<UserConfirmation> message = MessageBuilder.withPayload(userConfirmation)
                .setHeader(KafkaHeaders.TOPIC, "user-confirmation-topic")
                .setHeader(KafkaHeaders.KEY, userConfirmation.getUserId().toString())
                .build();
        kafkaTemplate.send(message);
    }
}
