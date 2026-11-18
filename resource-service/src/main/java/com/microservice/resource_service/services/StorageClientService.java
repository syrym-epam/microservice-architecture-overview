package com.microservice.resource_service.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate; 

import java.util.List;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import com.microservice.resource_service.dto.StorageEntityDTO;
import com.microservice.resource_service.entities.StorageType;
import com.microservice.resource_service.exceptions.DataNotFoundException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class StorageClientService {

    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;

    @Value("${storage-service.url:http://localhost:8087}")
    private String storageServiceUrl;

    public StorageClientService(DiscoveryClient discoveryClient) {
        this.restTemplate = new RestTemplate();
        this.discoveryClient = discoveryClient;
    }

    @CircuitBreaker(name = "storageService", fallbackMethod = "getDefaultStorageFallback")
    public StorageEntityDTO getStorageByType(StorageType type) {

        List<StorageEntityDTO> storages = restTemplate.exchange(
                discoveryClient.getInstances("STORAGE-SERVICE").getFirst().getUri() + "/storages",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<StorageEntityDTO>>() {}
        ).getBody();

        if (storages != null) {
            for (StorageEntityDTO storage : storages) {
                if (type == storage.getStorageType()) {
                    return storage;
                }
            }
        }

        throw new DataNotFoundException("Storage type " + type + " not found");
    }

    private StorageEntityDTO getDefaultStorageFallback(StorageType type, Throwable t) {
        System.out.println("*** CircuitBreaker swtiched to method getDefaultStorageFallback ***");
        if (StorageType.STAGING == type) {
            return new StorageEntityDTO(-1L, StorageType.STAGING, "staging-mp3", "files/");
        } else {
            return new StorageEntityDTO(-1L, StorageType.PERMANENT, "permanent-mp3", "files/");
        }
    }
}