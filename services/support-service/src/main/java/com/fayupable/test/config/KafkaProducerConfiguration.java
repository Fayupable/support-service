package com.fayupable.test.config;

import com.fayupable.test.kafka.SupportTicketConfirmation;
import com.fayupable.test.kafka.SupportTicketResolutionConfirmation;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfiguration {

    private Map<String, Object> createProducerConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return config;
    }

    private <T> ProducerFactory<String, T> createProducerFactory(Class<T> clazz) {
        return new DefaultKafkaProducerFactory<>(createProducerConfig());
    }

    private <T> KafkaTemplate<String, T> createTemplate(Class<T> clazz) {
        return new KafkaTemplate<>(createProducerFactory(clazz));
    }

    @Bean
    public KafkaTemplate<String, SupportTicketConfirmation> supportKafkaTemplate() {
        return createTemplate(SupportTicketConfirmation.class);
    }

    @Bean
    public KafkaTemplate<String, SupportTicketResolutionConfirmation> resolutionKafkaTemplate() {
        return createTemplate(SupportTicketResolutionConfirmation.class);
    }
}