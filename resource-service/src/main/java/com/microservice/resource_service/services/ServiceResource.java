package com.microservice.resource_service.services;

 
import java.io.IOException; 
import java.util.Arrays;
import java.util.List; 
import java.util.stream.Collectors;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.client.RestTemplate;

import com.microservice.resource_service.dto.StorageEntityDTO;
import com.microservice.resource_service.entities.EntityResource;
import com.microservice.resource_service.exceptions.ContentTypeException;
import com.microservice.resource_service.exceptions.DataNotFoundException;
import com.microservice.resource_service.exceptions.DataReadErrorException;
import com.microservice.resource_service.exceptions.RestTemplateErrorException;
import com.microservice.resource_service.exceptions.S3AWSExceptions;
import com.microservice.resource_service.repositories.RepositoryResource;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client; 
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse; 
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception; 
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

import java.util.UUID; 
 
import com.microservice.resource_service.entities.StorageType;

@Service 
public class ServiceResource {
    
    // @Value("${aws.backet_name}")
    // private String bucketName;

    private final S3Client s3Client;

    private final static String MEDIA_TYPE_MP3 = "audio/mpeg";

    private RepositoryResource repositoryResource;
    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;
    private final KafkaProducerService kafkaProducerService;
    private final StorageClientService storageClientService;
    
    public ServiceResource(RepositoryResource repositoryResource,
        DiscoveryClient discoveryClient,
        S3Client s3Client,
        KafkaProducerService kafkaProducerService,
        StorageClientService storageClientService) {
        this.repositoryResource = repositoryResource;
        this.restTemplate = new RestTemplate();
        this.discoveryClient = discoveryClient;
        this.s3Client = s3Client;
        this.kafkaProducerService = kafkaProducerService;
        this.storageClientService = storageClientService;
    }

    // @PostConstruct
    // public void initBucket() {
    //     int maxRetries = 5;
    //     int delayMs = 3000;
    //     for(int i = 1; i <= maxRetries; i++) {
    //         try {
    //             s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
    //             break;
    //         } catch (S3Exception e) {
    //             if (e.statusCode() == 404) {
    //                 s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
    //                 System.out.println("Bucket created automatically on startup: " + bucketName);
    //             } else {
    //                 if (i == maxRetries) throw new RuntimeException("Failed to connect to LocalStack S3 after retries", e);
    //                 try { Thread.sleep(delayMs); } catch (InterruptedException ignored) {}
    //             }
    //         }
    //     }
    // }


    public Long preSaveResource(byte[] data, HttpServletRequest request) throws IOException {
        
        MimeType mimeType = MimeTypeUtils.parseMimeType(request.getContentType());
        String contentType = "%s/%s".formatted(mimeType.getType(), mimeType.getSubtype());

        if (contentType == null || !contentType.equalsIgnoreCase(MEDIA_TYPE_MP3)) {
            throw new ContentTypeException("Invalid file format: application/json. Only MP3 files are allowed");
        }

        try {
            return saveResource(data);
        } catch (IOException e) {
            throw new IOException("An error occurred on the server");
        } 
    }

    @Transactional
    public Long saveResource(byte[] data) throws IOException {

        String fileName = UUID.randomUUID().toString();
        StorageEntityDTO storageEntity = storageClientService.getStorageByType(StorageType.STAGING);
        String path = storageEntity.getPath() + fileName;
        
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(storageEntity.getBucket())
                .key(path)
                .contentType(MEDIA_TYPE_MP3)
                .contentLength((long) data.length)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(data));
        
        EntityResource entityResource = new EntityResource();
        entityResource.setFileName(fileName);
        entityResource.setPath(path);
        entityResource.setStorageType(StorageType.STAGING);
        entityResource = repositoryResource.save(entityResource);

        kafkaProducerService.sendMessage(entityResource.getId().toString());

        return entityResource.getId();
    }

    public byte[] getResource(Long id) {
        if (!existResource(id))
            throw new DataNotFoundException("Resource with ID=%d not found".formatted(id));

        EntityResource entityResource = repositoryResource
                .findById(id)
                .orElseThrow(() -> new DataNotFoundException("Audio file with %d not found in Database".formatted(id)));
        
        StorageEntityDTO storageEntity = storageClientService.getStorageByType(entityResource.getStorageType());

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(storageEntity.getBucket())
                .key(entityResource.getPath())
                .build();

        try (ResponseInputStream<GetObjectResponse> s3Stream = s3Client.getObject(getObjectRequest)) {
            return s3Stream.readAllBytes();
        } catch (IOException e) {
            throw new DataReadErrorException(
                    "Failed to read file bytes from S3: %s".formatted(entityResource.getFileName()));
        }
    }

    public void updateResource(Long ID) {

        EntityResource entityResource = repositoryResource
                .findById(ID)
                .orElseThrow(() -> new DataNotFoundException("Audio file with %d not found in Database".formatted(ID)));

        StorageEntityDTO storageEntity = storageClientService.getStorageByType(StorageType.PERMANENT);
        String path = storageEntity.getPath() + entityResource.getFileName();

        var data = getResource(ID);
        
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(storageEntity.getBucket())
                .key(path)
                .contentType(MEDIA_TYPE_MP3)
                .contentLength((long) data.length)
                .build();  

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(data));   

        deleteResourceFromS3(entityResource.getStorageType(), entityResource.getPath());

        entityResource.setPath(path);
        entityResource.setStorageType(StorageType.PERMANENT);
        repositoryResource.save(entityResource);
    }

    public void deleteResourceFromS3(StorageType type, String path) {
            StorageEntityDTO storageEntity = storageClientService.getStorageByType(type);

            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(storageEntity.getBucket())
                    .key(path)
                    .build();
            try {
                s3Client.deleteObject(request);
            } catch (S3Exception e) {
                throw new S3AWSExceptions(e.getMessage());
            }
    }

    public void deleteAllRecordsBucketByIDs(List<EntityResource> entityResources) { 
        
        for (StorageType type : StorageType.values()) {
            List<ObjectIdentifier> fileNames = entityResources.stream()
                    .filter(entity -> entity.getStorageType() == type)
                    .map(entity -> ObjectIdentifier
                            .builder()
                            .key(entity.getPath())
                            .build())
                    .toList();
                    
            if(fileNames.isEmpty()) continue;

            Delete delete = Delete.builder()
                    .objects(fileNames)
                    .quiet(false)
                    .build();

            StorageEntityDTO storageEntity = storageClientService.getStorageByType(type);

            DeleteObjectsRequest request = DeleteObjectsRequest.builder()
                    .bucket(storageEntity.getBucket())
                    .delete(delete)
                    .build();

            try {
                s3Client.deleteObjects(request);
            } catch (S3Exception e) {
                throw new S3AWSExceptions(e.getMessage());
            }
        }
    }

    @Transactional
    public List<Long> deleteAllResourceByIDs(String data) {
        List<Long> Ids = Arrays.stream(data.split(",")).map(Long::valueOf).toList();
        // List<Long> existingIds = repositoryResource.findExistingIds(Ids);

        List<EntityResource> entityResources = repositoryResource.findAllById(Ids);
        List<Long> existingIds = entityResources.stream().map(entity -> entity.getId()).toList();

        if (!existingIds.isEmpty()) {

            deleteAllRecordsBucketByIDs(entityResources);

            try { 
                String result = existingIds.stream().map(String::valueOf).collect(Collectors.joining(","));
                restTemplate.delete(discoveryClient.getInstances("SONG-SERVICE").getFirst().getUri() + "/songs?id=" + result);
            } catch (Exception e) {
                throw new RestTemplateErrorException(e.getMessage());
            }

            repositoryResource.deleteAllByIdInBatch(existingIds);
        }

        return existingIds;
    }

    public Boolean existResource(Long id) {
        
        return repositoryResource.existsById(id);
    }
}