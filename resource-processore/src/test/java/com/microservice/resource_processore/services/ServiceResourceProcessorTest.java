package com.microservice.resource_processore.services;

import org.junit.jupiter.api.Test;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceResourceProcessorTest {

    @Test
    void testCalcDurationMP3_ValidSeconds() {
        ServiceResourceProcessor processor = new ServiceResourceProcessor(null);
        Optional<String> duration = processor.calcDurationMP3("125.7");
        
        assertThat(duration).isPresent();
        assertThat(duration.get()).isEqualTo("02:06");
    }

    @Test
    void testCalcDurationMP3_NullValue() {
        ServiceResourceProcessor processor = new ServiceResourceProcessor(null);
        Optional<String> duration = processor.calcDurationMP3(null);
        
        assertThat(duration).isEmpty();
    }
}