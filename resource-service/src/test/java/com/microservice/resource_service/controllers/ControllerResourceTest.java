package com.microservice.resource_service.controllers;

import com.microservice.resource_service.services.ServiceResource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ControllerResource.class)
class ControllerResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServiceResource serviceResource;

    @Test
    void testCreateResource_Success() throws Exception {
        byte[] audioData = new byte[]{1, 2, 3, 4};
        given(serviceResource.preSaveResource(any(byte[].class), any())).willReturn(10L);

        mockMvc.perform(post("/resources")
                        .contentType("audio/mpeg")
                        .content(audioData))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void testGetResource_Success() throws Exception {
        byte[] audioData = new byte[]{1, 2, 3, 4};
        given(serviceResource.getResource(1L)).willReturn(audioData);

        mockMvc.perform(get("/resources/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("audio/mpeg")))
                .andExpect(content().bytes(audioData));
    }

    @Test
    void testGetResource_InvalidId_ShouldFailValidation() throws Exception {
        mockMvc.perform(get("/resources/0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteResource_Success() throws Exception {
        given(serviceResource.deleteAllResourceByIDs("1,2")).willReturn(List.of(1L, 2L));

        mockMvc.perform(delete("/resources")
                        .param("id", "1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ids[0]").value(1))
                .andExpect(jsonPath("$.ids[1]").value(2));
    }

    @Test
    void testDeleteResource_InvalidPattern_ShouldFailValidation() throws Exception {
        mockMvc.perform(delete("/resources")
                        .param("id", "1,-2,abc"))
                .andExpect(status().isBadRequest());
    }
}