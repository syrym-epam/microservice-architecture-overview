package com.microservice.song_service.steps;

import com.microservice.song_service.ServiceSong;
import com.microservice.song_service.dto.EntitySongDTO;
import com.microservice.song_service.entities.EntitySong;
import com.microservice.song_service.exceptions.DataNotFoundException;
import com.microservice.song_service.exceptions.MetaDataExistExceptions;
import com.microservice.song_service.repositories.RepositorySong;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SongComponentSteps {

    @Autowired
    private ServiceSong serviceSong;

    @Autowired
    private RepositorySong repositorySong;

    private EntitySongDTO songDTO;
    private Long savedId;
    private EntitySongDTO retrievedDTO;
    private Exception capturedException;
    private List<Long> deletedIds;

    @Given("a song DTO with ID {long}, name {string}, artist {string}, album {string}, duration {string}, and year {string}")
    public void a_song_dto_with_details(Long id, String name, String artist, String album, String duration, String year) {
        songDTO = new EntitySongDTO();
        songDTO.id = id;
        songDTO.name = name;
        songDTO.artist = artist;
        songDTO.album = album;
        songDTO.duration = duration;
        songDTO.year = year;
    }

    @When("the song is saved via the ServiceSong layer")
    public void the_song_is_saved_via_service_layer() {
        try {
            savedId = serviceSong.saveSong(songDTO);
        } catch (Exception e) {
            capturedException = e;
        }
    }

    @Then("the song should be successfully stored with ID {long}")
    public void the_song_should_be_successfully_stored(Long expectedId) {
        assertEquals(expectedId, savedId);
        assertTrue(repositorySong.existsById(expectedId));
    }

    @Given("a song with ID {long} already exists in the repository")
    public void a_song_with_id_already_exists(Long id) {
        EntitySong entity = new EntitySong();
        entity.setId(id);
        entity.setName("Existing Song");
        repositorySong.save(entity);
    }

    @When("I attempt to save a new song DTO with ID {long}")
    public void i_attempt_to_save_song_with_id(Long id) {
        songDTO = new EntitySongDTO();
        songDTO.id = id;
        songDTO.name = "New Song";
        try {
            serviceSong.saveSong(songDTO);
        } catch (Exception e) {
            capturedException = e;
        }
    }

    @Then("a MetaDataExistExceptions should be thrown indicating the resource ID already exists")
    public void metadata_exists_exception_thrown() {
        assertNotNull(capturedException);
        assertTrue(capturedException instanceof MetaDataExistExceptions);
    }

    @Given("a song with ID {long} exists in the repository")
    public void a_song_exists_in_repository(Long id) {
        EntitySong entity = new EntitySong();
        entity.setId(id);
        entity.setName("Test Song Title");
        repositorySong.save(entity);
    }

    @When("I request the song via ServiceSong with ID {long}")
    public void i_request_song_via_service_with_id(Long id) {
        try {
            retrievedDTO = serviceSong.getSongById(id);
        } catch (Exception e) {
            capturedException = e;
        }
    }

    @Then("the returned EntitySongDTO should match the stored song details")
    public void returned_dto_should_match() {
        assertNotNull(retrievedDTO);
        assertEquals("Test Song Title", retrievedDTO.name);
    }

    @Given("no song exists with ID {long} in the repository")
    public void no_song_exists_with_id(Long id) {
        if (repositorySong.existsById(id)) {
            repositorySong.deleteById(id);
        }
    }

    @Then("a DataNotFoundException should be thrown indicating the song metadata was not found")
    public void data_not_found_exception_thrown() {
        assertNotNull(capturedException);
        assertTrue(capturedException instanceof DataNotFoundException);
    }

    @Given("songs with IDs {long}, {long}, and {long} exist in the repository")
    public void multiple_songs_exist_in_repository(Long id1, Long id2, Long id3) {
        for (Long id : List.of(id1, id2, id3)) {
            EntitySong entity = new EntitySong();
            entity.setId(id);
            entity.setName("Song " + id);
            repositorySong.save(entity);
        }
    }

    @When("I invoke deleteAllSongById with CSV string {string}")
    public void i_invoke_delete_all_song_by_id(String csv) {
        deletedIds = serviceSong.deleteAllSongById(csv);
    }

    @Then("the returned list should contain IDs [{long}, {long}, {long}]")
    public void returned_list_should_contain_ids(Long id1, Long id2, Long id3) {
        assertNotNull(deletedIds);
        assertEquals(List.of(id1, id2, id3), deletedIds);
    }

    @Then("the songs should be removed from the repository")
    public void songs_should_be_removed_from_repository() {
        assertFalse(repositorySong.existsById(1L));
        assertFalse(repositorySong.existsById(2L));
        assertFalse(repositorySong.existsById(3L));
    }
}