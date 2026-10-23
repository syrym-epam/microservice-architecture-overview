package com.microservice.resource_processore.services;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Predicate;
 
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.microservice.resource_processore.dto.EntitySongDTO;
import com.microservice.resource_processore.exceptions.MetaDataFieldResourceNotFoundException;
import com.microservice.resource_processore.exceptions.RestTemplateErrorException;

@Service
public class ServiceResourceProcessor {

    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;

    ServiceResourceProcessor(DiscoveryClient discoveryClient) {
        this.restTemplate = new RestTemplate();
        this.discoveryClient = discoveryClient;
    }

    @KafkaListener(topics = "mp3", groupId = "microservice")
    public void consumeMessage(String message) {
        System.out.println("Consumed message: " + message);
        pullMP3FileByIdFromResourceService(Long.valueOf(message));
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

    public EntitySongDTO applyMetadatatoEntity(byte[] data){

        Metadata metadata = extractMetadataFromMP3File(data);

        var entitySongDTO = new EntitySongDTO();
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
        
        return entitySongDTO;
    }

    public void extractAndSaveFromMP3FileMetedataToSongService(byte[] data, Long ID) {

        EntitySongDTO entitySongDTO = applyMetadatatoEntity(data);        
        entitySongDTO.setId(ID);

        try {
            restTemplate.postForObject(discoveryClient.getInstances("SONG-SERVICE").getFirst().getUri() + "/songs", entitySongDTO, EntitySongDTO.class);
        } catch (Exception e) {
            throw new RestTemplateErrorException(e.getMessage());
        }
    }

    public void pullMP3FileByIdFromResourceService(Long ID) {
        try {
            byte[] file = restTemplate.getForObject(discoveryClient.getInstances("RESOURCE-SERVICE").getFirst().getUri() + "/resources/" + ID, byte[].class);
            extractAndSaveFromMP3FileMetedataToSongService(file, ID);
        } catch (Exception e) {
            throw new RestTemplateErrorException(e.getMessage());
        }
    }
}