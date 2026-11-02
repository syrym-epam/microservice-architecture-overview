package com.microservice.resource_service.contracts;

import au.com.dius.pact.consumer.dsl.PactBuilder;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.client.RestTemplate;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "song-service", port = "8081")
class SongServiceContractTest {

    @Pact(consumer = "resource-service", provider = "song-service")
    public V4Pact deleteSongsPact(PactBuilder builder) {
        return builder
            .usingLegacyDsl()
            .given("songs exist with IDs 1,2")
            .uponReceiving("a request to delete songs by IDs")
                .method("DELETE")
                .path("/songs")
                .query("id=1,2")
            .willRespondWith()
                .status(200)
            .toPact(V4Pact.class);
    }

    @Test
    void testDeleteSongs(V4Pact pact) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.delete("http://localhost:8081/songs?id=1,2");
    }
}