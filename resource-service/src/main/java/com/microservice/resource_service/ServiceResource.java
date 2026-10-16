package com.microservice.resource_service;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.client.RestTemplate;

import com.microservice.resource_service.dto.EntitySongDTO;
import com.microservice.resource_service.exceptions.ContentTypeException;
import com.microservice.resource_service.exceptions.DataNotFoundException;
import com.microservice.resource_service.exceptions.DataReadErrorException;
import com.microservice.resource_service.exceptions.MetaDataFieldResourceNotFoundException;
import com.microservice.resource_service.exceptions.RestTemplateErrorException;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

import java.util.UUID;
import java.util.function.Predicate;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Value;

@Service 
public class ServiceResource {
    
    @Value("${aws.backet_name}")
    private String bucketName;

    private final S3Client s3Client;

    private final static String MEDIA_TYPE_MP3 = "audio/mpeg";

    private RepositoryResource repositoryResource;
    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;
    
    public ServiceResource(RepositoryResource repositoryResource,
         DiscoveryClient discoveryClient,
        S3Client s3Client) {
        this.repositoryResource = repositoryResource;
        this.restTemplate = new RestTemplate();
        this.discoveryClient = discoveryClient;
        this.s3Client = s3Client;
    }

    @PostConstruct
    public void initBucket() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
                System.out.println("Bucket created automatically on startup: " + bucketName);
            } else {
                throw e;
            }
        }
    }


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

        Metadata metadata = extractMetadataFromMP3File(data);

        EntitySongDTO entitySongDTO = new EntitySongDTO();
        entitySongDTO.setName(
                Optional.ofNullable(metadata.get("dc:title"))
                        .filter(Predicate.not(String::isBlank))
                        .orElseThrow(() -> new MetaDataFieldResourceNotFoundException("title")));
        entitySongDTO.setArtist( 
                Optional.ofNullable(metadata.get("xmpDM:artist"))
                        .or(() -> Optional.ofNullable(metadata.get("author")))
                        .filter(Predicate.not(String::isBlank))
                        .orElseThrow(() -> new MetaDataFieldResourceNotFoundException("artist")));
        entitySongDTO.setAlbum(
                Optional.ofNullable(metadata.get("xmpDM:album"))
                        .filter(Predicate.not(String::isBlank))
                        .orElseThrow(() -> new MetaDataFieldResourceNotFoundException("album")));
        entitySongDTO.setYear(
                Optional.ofNullable(metadata.get("xmpDM:releaseDate"))
                        .or(() -> Optional.ofNullable(metadata.get("xmpDM:Year recorded")))
                        .or(() -> Optional.ofNullable(metadata.get("xmpDM:releaseDate")))
                        .or(() -> Optional.ofNullable(metadata.get("xmpDM:year")))
                        .filter(Predicate.not(String::isBlank))
                        .orElseThrow(() -> new MetaDataFieldResourceNotFoundException("year")));
        entitySongDTO.setDuration(calcDurationMP3(metadata.get("xmpDM:duration"))
                        .orElseThrow(() -> new MetaDataFieldResourceNotFoundException("duration")));

        String fileName = UUID.randomUUID().toString();
        
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(MEDIA_TYPE_MP3)
                .contentLength((long) data.length)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(data));
        
        EntityResource entityResource = new EntityResource();
        entityResource.setFileName(fileName);
        entityResource = repositoryResource.save(entityResource);

        entitySongDTO.setId(entityResource.getId());

        try {
            restTemplate.postForObject(discoveryClient.getInstances("SONG-SERVICE").getFirst().getUri() + "/songs", entitySongDTO, EntitySongDTO.class);
        } catch (Exception e) {
            throw new RestTemplateErrorException(e.getMessage());
        }

        return entityResource.getId();
    }

    // public EntityResource getResource(Long id) {
    //     if(!existResource(id))
    //         throw new DataNotFoundException("Resource with ID=%d not found".formatted(id));
    //     return  repositoryResource
    //         .findById(id)
    //         .orElseThrow(() -> new DataNotFoundException("Audio file with %d not found in Database".formatted(id)));
    // }

    public byte[] getResource(Long id) {
        if (!existResource(id))
            throw new DataNotFoundException("Resource with ID=%d not found".formatted(id));

        EntityResource entityResource = repositoryResource
                .findById(id)
                .orElseThrow(() -> new DataNotFoundException("Audio file with %d not found in Database".formatted(id)));

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(entityResource.getFileName())
                .build();

        try (ResponseInputStream<GetObjectResponse> s3Stream = s3Client.getObject(getObjectRequest)) {
            return s3Stream.readAllBytes();
        } catch (IOException e) {
            throw new DataReadErrorException(
                    "Failed to read file bytes from S3: %s".formatted(entityResource.fileName));
        }
    }

    public Metadata makeMetadataFromClass(Object obj) {
        Metadata metadata = new Metadata();

        Field[] attributes = obj.getClass().getDeclaredFields();
        for(int i = 1; i< attributes.length; i++) {
            try {
                metadata.set(attributes[i].getName(), "" + Optional.ofNullable(attributes[i].get(obj)).orElse(""));
            } catch (IllegalArgumentException e) {
                continue;
            } catch (IllegalAccessException e) {
                continue;
            }
        }

        return metadata;
    }

    public Optional<String> calcDurationMP3(String value) {
        if(value == null)
            return Optional.empty();
        else {
            double second = Double.parseDouble(value);
            int totalSecs = (int) Math.round(second);
            int mins = totalSecs / 60;
            int secs = totalSecs % 60;
            return Optional.of("%02d:%02d".formatted(mins, secs));
        }
    }

    public Metadata extractMetadataFromMP3File(byte[] data){
        Metadata metaData = new Metadata();
        try {
            BodyContentHandler handler = new BodyContentHandler();
            ParseContext context = new ParseContext();
            Mp3Parser parser = new Mp3Parser();
            parser.parse(new ByteArrayInputStream(data), handler, metaData, context);
        } catch(Exception e) {
            return metaData;
        }

        return metaData;
    }

    @Transactional
    public List<Long> deleteAllResourceByIDs(String data) {
        List<Long> Ids = Arrays.stream(data.split(",")).map(Long::valueOf).toList();
        List<Long> existingIds = repositoryResource.findExistingIds(Ids);

        if (!existingIds.isEmpty()) {
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
