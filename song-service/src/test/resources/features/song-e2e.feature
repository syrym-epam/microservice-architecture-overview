Feature: Song Service End-to-End API Coverage
  As a client of the song service
  I want to interact with the song endpoints via HTTP
  So that I can create, retrieve, and delete song records through REST APIs

  Scenario: Create and retrieve a song successfully via REST endpoints
    When I send a POST request to "/songs" with the JSON payload:
      """
      {
        "id": 101,
        "name": "Hotel California",
        "artist": "Eagles",
        "album": "Hotel California",
        "duration": "06:30",
        "year": "1976"
      }
      """
    Then the HTTP response status should be 200 OK
    And the response body should contain ResultIdDTO with ID 101

    When I send a GET request to "/songs/101"
    Then the HTTP response status should be 200 OK
    And the response body should contain song name "Hotel California" and artist "Eagles"

  Scenario: Validation error when retrieving song with non-positive ID
    When I send a GET request to "/songs/0"
    Then the HTTP response status should be 400 Bad Request
    And the error message should indicate that the ID must be a positive integer

  Scenario: Batch delete songs via query parameters successfully
    Given songs with IDs 10, 20 exist in the database
    When I send a DELETE request to "/songs?id=10,20"
    Then the HTTP response status should be 200 OK
    And the response body should contain ResultIdsDTO with existing IDs [10, 20]

  Scenario: Validation error on invalid CSV format during deletion
    When I send a DELETE request to "/songs?id=invalid_csv"
    Then the HTTP response status should be 400 Bad Request
    And the response body should indicate invalid ID format