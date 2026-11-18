package com.microservice.storage_service.services;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.microservice.storage_service.dto.StorageRequestDTO;
import com.microservice.storage_service.entities.StorageEntity;
import com.microservice.storage_service.entities.StorageType;
import com.microservice.storage_service.repositories.StorageRepository;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock
    private StorageRepository storageRepository;

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private StorageService storageService;

    @Test
    void createStorage_success() {
        StorageRequestDTO dto = new StorageRequestDTO(StorageType.STAGING, "test-bucket", "files/");

        StorageEntity savedEntity = new StorageEntity();
        savedEntity.setId(10L);
        savedEntity.setStorageType(dto.storageType());
        savedEntity.setBucket(dto.bucket());
        savedEntity.setPath(dto.path());

        when(s3Client.headBucket(any(HeadBucketRequest.class))).thenReturn(HeadBucketResponse.builder().build());
        when(storageRepository.save(any(StorageEntity.class))).thenReturn(savedEntity);

        Long id = storageService.createStorage(dto);

        assertEquals(10L, id);
        verify(storageRepository, times(1)).save(any(StorageEntity.class));
    }

    @Test
    void deleteStorages_success() {
        StorageEntity entity = new StorageEntity();
        entity.setId(1L);
        entity.setStorageType(StorageType.PERMANENT);

        when(storageRepository.findAllById(List.of(1L))).thenReturn(List.of(entity));
        doNothing().when(storageRepository).deleteAllByIdInBatch(anyList());

        List<Long> deleted = storageService.deleteStorages("1");

        assertEquals(1, deleted.size());
        assertEquals(1L, deleted.get(0));
        verify(storageRepository, times(1)).deleteAllByIdInBatch(anyList());
    }

    @Test
    void getAllStorages_success() {
        when(storageRepository.findAll()).thenReturn(List.of());
        List<StorageEntity> storages = storageService.getAllStorages();
        assertNotNull(storages);
        verify(storageRepository, times(1)).findAll();
    }
}