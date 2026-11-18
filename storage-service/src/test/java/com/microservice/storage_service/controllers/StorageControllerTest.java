package com.microservice.storage_service.controllers;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.microservice.storage_service.dto.StorageRequestDTO;
import com.microservice.storage_service.entities.StorageEntity;
import com.microservice.storage_service.entities.StorageType;
import com.microservice.storage_service.services.StorageService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(StorageController.class)
class StorageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StorageService storageService;

    @Test
    void createStorage_success() throws Exception {
        StorageRequestDTO requestDTO = new StorageRequestDTO(StorageType.STAGING, "my-bucket", "files/");
        when(storageService.createStorage(any(StorageRequestDTO.class))).thenReturn(1L);

        mockMvc.perform(post("/storages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createStorage_validationFailure() throws Exception {
        // Missing bucket and path
        StorageRequestDTO requestDTO = new StorageRequestDTO(StorageType.STAGING, "", "");

        mockMvc.perform(post("/storages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllStorages_success() throws Exception {
        StorageEntity entity = new StorageEntity();
        entity.setId(1L);
        entity.setStorageType(StorageType.STAGING);
        entity.setBucket("bucket");
        entity.setPath("path/");

        when(storageService.getAllStorages()).thenReturn(List.of(entity));

        mockMvc.perform(get("/storages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].bucket").value("bucket"));
    }

    @Test
    void deleteStorages_success() throws Exception {
        when(storageService.deleteStorages(anyString())).thenReturn(List.of(1L, 2L));

        mockMvc.perform(delete("/storages")
                        .param("id", "1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ids[0]").value(1))
                .andExpect(jsonPath("$.ids[1]").value(2));
    }

    @Test
    void deleteStorages_invalidFormatPattern() throws Exception {
        mockMvc.perform(delete("/storages")
                        .param("id", "abc"))
                .andExpect(status().isBadRequest());
    }
}