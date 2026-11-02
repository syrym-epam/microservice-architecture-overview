package com.microservice.song_service;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.microservice.song_service.controllers.ControllerSong;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public abstract class ContractBaseClass {

    @Autowired
    private ControllerSong controllerSong;

    @MockitoBean 
    private ServiceSong serviceSong;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.standaloneSetup(controllerSong);
    }
}