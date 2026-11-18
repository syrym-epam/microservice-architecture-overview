package com.microservice.storage_service.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.microservice.storage_service.entities.StorageEntity;
import com.microservice.storage_service.entities.StorageType;

public interface StorageRepository extends JpaRepository<StorageEntity, Long> {
    
    Optional<StorageType> findByStorageType(StorageType storageType);
    boolean existsByStorageType(StorageType storageType);
}