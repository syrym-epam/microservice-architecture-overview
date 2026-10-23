package com.microservice.resource_service.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class CloudConfig {

    @Value("${aws.localstack-endpoint}")
    private String url;
    
    @Value("${aws.region}")
    private String region;

    @Value("${aws.access_id}")
    private String access_id;

    @Value("${aws.access_key}")
    private String access_key;


    @Bean
    public S3Client s3Client() {

        return S3Client.builder()
                .endpointOverride(URI.create(url))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(access_id, access_key)))
                .region(Region.of(region))
                .forcePathStyle(true)
                .build();
    }
}
