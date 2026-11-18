package com.microservice.resource_service.services;


import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
public class KafkaConsumerService {

    private static final String TOPIC = "SONG";
    private static final String TOPIC_GROUP_ID = "microservice";

    private final ServiceResource serviceResource;

    KafkaConsumerService(ServiceResource serviceResource) {
        this.serviceResource = serviceResource;
    }

    @KafkaListener(topics = TOPIC, groupId = TOPIC_GROUP_ID)
    public void consumeMessage(String message) {
        System.out.println("Consumed message: " + message);
        serviceResource.updateResource(Long.valueOf(message));
    }
}


