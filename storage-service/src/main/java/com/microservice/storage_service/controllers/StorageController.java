package com.microservice.storage_service.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.microservice.storage_service.dto.StorageIdDTO;
import com.microservice.storage_service.dto.StorageIdsDTO;
import com.microservice.storage_service.dto.StorageRequestDTO;
import com.microservice.storage_service.entities.StorageEntity;
import com.microservice.storage_service.services.StorageService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Validated
@RestController
@RequestMapping("/storages")
public class StorageController {

    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping
    public ResponseEntity<StorageIdDTO> createStorage(@Valid @RequestBody StorageRequestDTO requestDTO) {
        Long id = storageService.createStorage(requestDTO);

        return ResponseEntity.status(HttpStatus.OK).body(new StorageIdDTO(id));
    }

    @GetMapping
    public ResponseEntity<List<StorageEntity>> getAllStorages() {
        return ResponseEntity.ok(storageService.getAllStorages());
    }

    @DeleteMapping
    public ResponseEntity<StorageIdsDTO> deleteStorages(
            @RequestParam("id")
            @Size(max = 200, message = "CSV string is too long")
            @Pattern(regexp = "^[1-9]\\d*(,[1-9]\\d*)*$", message = "Invalid ID format")
            String ids) {
        List<Long> deletedIds = storageService.deleteStorages(ids);
        return ResponseEntity.ok(new StorageIdsDTO(deletedIds));
    }
}