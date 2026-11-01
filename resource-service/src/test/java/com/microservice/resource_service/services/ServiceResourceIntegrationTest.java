package com.microservice.resource_service.services;

import com.microservice.resource_service.entities.EntityResource;
import com.microservice.resource_service.repositories.RepositoryResource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class ServiceResourceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
    }

    @Autowired
    private RepositoryResource repositoryResource;

    @Test
    void testRepositorySaveAndFind() {
        EntityResource entity = new EntityResource();
        entity.setFileName("test-audio.mp3");
        
        EntityResource saved = repositoryResource.save(entity);
        
        assertThat(saved.getId()).isNotNull();
        assertThat(repositoryResource.findById(saved.getId())).isPresent();
    }
}
