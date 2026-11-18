package com.microservice.storage_service.services;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.microservice.storage_service.dto.StorageRequestDTO;
import com.microservice.storage_service.entities.StorageEntity;
import com.microservice.storage_service.entities.StorageType;
import com.microservice.storage_service.repositories.StorageRepository;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class StorageService {

    private final StorageRepository storageRepository;
    private final S3Client s3Client;

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    public StorageService(StorageRepository storageRepository, S3Client s3Client) {
        this.storageRepository = storageRepository;
        this.s3Client = s3Client;
    }

    @PostConstruct
    public void initDefaultStorages() {
        createDefaultStorageIfNotExists(StorageType.STAGING, "staging-mp3", "files/");
        createDefaultStorageIfNotExists(StorageType.PERMANENT, "permanent-mp3", "files/");
    } 

    private void createDefaultStorageIfNotExists(StorageType type, String bucket, String path) {
        if (!storageRepository.existsByStorageType(type)) {
            ensureBucketExists(bucket);
            StorageEntity entity = new StorageEntity();
            entity.setStorageType(type);
            entity.setBucket(bucket);
            entity.setPath(path);
            storageRepository.save(entity);
        }
    }

    private void ensureBucketExists(String bucketName) {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
            }
        }
    }

    @Transactional
    public Long createStorage(StorageRequestDTO dto) {
        ensureBucketExists(dto.bucket());
        StorageEntity entity = new StorageEntity();
        entity.setStorageType(dto.storageType());
        entity.setBucket(dto.bucket());
        entity.setPath(dto.path());
        return storageRepository.save(entity).getId();
    }

    public List<StorageEntity> getAllStorages() {
        return storageRepository.findAll();
    }

    @Transactional
    public List<Long> deleteStorages(String data) {
        List<Long> ids = Arrays.stream(data.split(",")).map(Long::valueOf).toList();
        List<StorageEntity> found = storageRepository.findAllById(ids);
        List<Long> existingIds = found.stream().map(StorageEntity::getId).toList();
        if (!existingIds.isEmpty()) {
            storageRepository.deleteAllByIdInBatch(existingIds);
        }
        return existingIds;
    }
}