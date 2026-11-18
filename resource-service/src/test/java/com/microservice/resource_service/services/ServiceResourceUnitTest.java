package com.microservice.resource_service.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class ServiceResourceUnitTest {

    private ServiceResource serviceResource;

    @BeforeEach
    void setUp() {
        serviceResource = new ServiceResource(null, null, null, null, null);
    }
}
