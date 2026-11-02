package com.microservice.resource_service.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class ServiceResourceUnitTest {

    private ServiceResource serviceResource;

    @BeforeEach
    void setUp() {
        serviceResource = new ServiceResource(null, null, null, null);
    }

    @Test
    void testCalcDurationMP3_ValidSeconds() {
        Optional<String> duration = serviceResource.calcDurationMP3("125.5");
        assertTrue(duration.isPresent());
        assertEquals("02:06", duration.get());
    }

    @Test
    void testCalcDurationMP3_NullValue() {
        Optional<String> duration = serviceResource.calcDurationMP3(null);
        assertTrue(duration.isEmpty());
    }
}
