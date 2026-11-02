package com.microservice.song_service.steps;

import com.microservice.song_service.SongServiceApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@CucumberContextConfiguration
@SpringBootTest(classes = SongServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CucumberSpringConfiguration {
    
}