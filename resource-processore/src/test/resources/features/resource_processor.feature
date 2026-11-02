Feature: Resource Processor MP3 Metadata Extraction
  As a microservice system
  I want to consume an MP3 resource ID from Kafka, extract metadata, and post it to the Song Service

  Scenario: Successfully consume MP3 event and save song metadata
    Given a resource with ID 10 exists with valid MP3 byte data
    When the Kafka topic "mp3" receives message "10"
    Then the metadata is extracted and a POST request is sent to "SONG-SERVICE" with the song details