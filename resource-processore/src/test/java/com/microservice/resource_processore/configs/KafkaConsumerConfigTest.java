package com.microservice.resource_processore.configs;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class KafkaConsumerConfigTest {

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    @Autowired
    private ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory;

    @Test
    void consumerFactoryNotNull() {
        assertThat(consumerFactory).isNotNull();
    }

    @Test
    void kafkaListenerContainerFactoryNotNull() {
        assertThat(kafkaListenerContainerFactory).isNotNull();
        assertThat(kafkaListenerContainerFactory.getContainerProperties()).isNotNull();
    }
}