package com.fayupable.test.config;

import com.fayupable.test.kafka.support.SupportTicketConfirmation;
import com.fayupable.test.kafka.support.SupportTicketResolutionConfirmation;
import com.fayupable.test.kafka.user.UserConfirmation;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {
    private <T> ConsumerFactory<String, T> createConsumerFactory(Class<T> clazz) {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, clazz.getName());

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(),
                new JsonDeserializer<>(clazz));
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> createFactory(Class<T> clazz) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(createConsumerFactory(clazz));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserConfirmation> userKafkaListenerContainerFactory() {
        return createFactory(UserConfirmation.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SupportTicketConfirmation> supportKafkaListenerContainerFactory() {
        return createFactory(SupportTicketConfirmation.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SupportTicketResolutionConfirmation> supportTicketResolutionKafkaListenerContainerFactory() {
        return createFactory(SupportTicketResolutionConfirmation.class);
    }

}
