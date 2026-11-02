package com.microservice.song_service.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
 
import com.microservice.song_service.ServiceSong;
import com.microservice.song_service.dto.EntitySongDTO;

@WebMvcTest(ControllerSong.class)
class ControllerSongIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean 
    private ServiceSong serviceSong;

    @Test
    void getSongById_ValidId_ReturnsSong() throws Exception {
        EntitySongDTO mockDto = new EntitySongDTO();
        mockDto.setId(1L);
        mockDto.setName("Test Song");

        when(serviceSong.getSongById(1L)).thenReturn(mockDto);

        mockMvc.perform(get("/songs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Song"));
    }

    @Test
    void getSongById_InvalidId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/songs/0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteSongs_InvalidCsvPattern_ReturnsBadRequest() throws Exception {
        mockMvc.perform(delete("/songs").param("id", "1,-2"))
                .andExpect(status().isBadRequest());
    }
}