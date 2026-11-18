package com.microservice.storage_service.dto;

import com.microservice.storage_service.entities.StorageType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StorageRequestDTO(
    @NotNull(message = "Storage type cannot be null")
    StorageType storageType,

    @NotBlank(message = "Bucket name cannot be blank")
    String bucket,

    @NotBlank(message = "Path cannot be blank")
    String path
) {}