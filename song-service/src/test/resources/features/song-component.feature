Feature: Song Component Business Logic
  As a song service component
  I want to manage song metadata correctly
  So that data integrity and business rules are maintained

  Scenario: Successfully save a new song
    Given a song DTO with ID 1, name "Bohemian Rhapsody", artist "Queen", album "A Night at the Opera", duration "05:55", and year "1975"
    When the song is saved via the ServiceSong layer
    Then the song should be successfully stored with ID 1

  Scenario: Attempt to save a song with an existing ID
    Given a song with ID 1 already exists in the repository
    When I attempt to save a new song DTO with ID 1
    Then a MetaDataExistExceptions should be thrown indicating the resource ID already exists

  Scenario: Retrieve an existing song by ID successfully
    Given a song with ID 1 exists in the repository
    When I request the song via ServiceSong with ID 1
    Then the returned EntitySongDTO should match the stored song details

  Scenario: Attempt to retrieve a non-existent song
    Given no song exists with ID 999 in the repository
    When I request the song via ServiceSong with ID 999
    Then a DataNotFoundException should be thrown indicating the song metadata was not found

  Scenario: Batch delete existing songs by CSV IDs
    Given songs with IDs 1, 2, and 3 exist in the repository
    When I invoke deleteAllSongById with CSV string "1,2,3"
    Then the returned list should contain IDs [1, 2, 3]
    And the songs should be removed from the repository