package com.microservice.song_service.steps;

import com.microservice.song_service.entities.EntitySong;
import com.microservice.song_service.repositories.RepositorySong;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class SongE2ESteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RepositorySong repositorySong;

    private ResultActions resultActions;

    @When("I send a POST request to {string} with the JSON payload:")
    public void i_send_a_post_request_with_payload(String url, String jsonPayload) throws Exception {
        resultActions = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload));
    }

    @Then("the HTTP response status should be {int} OK")
    public void the_http_response_status_should_be_ok(int status) throws Exception {
        resultActions.andExpect(status().is(status));
    }

    @Then("the response body should contain ResultIdDTO with ID {long}")
    public void response_contains_result_id_dto(Long id) throws Exception {
        resultActions.andExpect(jsonPath("$.id").value(id));
    }

    @When("I send a GET request to {string}")
    public void i_send_a_get_request_to(String url) throws Exception {
        resultActions = mockMvc.perform(get(url));
    }

    @Then("the response body should contain song name {string} and artist {string}")
    public void response_contains_song_metadata(String name, String artist) throws Exception {
        resultActions.andExpect(jsonPath("$.name").value(name))
                      .andExpect(jsonPath("$.artist").value(artist));
    }

    @Then("the HTTP response status should be {int} Bad Request")
    public void the_http_response_status_should_be_bad_request(int status) throws Exception {
        resultActions.andExpect(status().is(status));
    }

    @Then("the error message should indicate that the ID must be a positive integer")
    public void error_message_indicates_positive_integer() throws Exception {
        resultActions.andExpect(status().isBadRequest());
    }

    @Given("songs with IDs {long}, {long} exist in the database")
    public void songs_exist_in_database(Long id1, Long id2) {
        for (Long id : List.of(id1, id2)) {
            EntitySong entity = new EntitySong();
            entity.setId(id);
            entity.setName("Song " + id);
            repositorySong.save(entity);
        }
    }

    @When("I send a DELETE request to {string}")
    public void i_send_a_delete_request_to(String url) throws Exception {
        resultActions = mockMvc.perform(delete(url));
    }

    @Then("the response body should contain ResultIdsDTO with existing IDs [{long}, {long}]")
    public void response_contains_result_ids_dto(Long id1, Long id2) throws Exception {
        resultActions.andExpect(jsonPath("$.ids[0]").value(id1))
                      .andExpect(jsonPath("$.ids[1]").value(id2));
    }

    @Then("the response body should indicate invalid ID format")
    public void response_indicates_invalid_id_format() throws Exception {
        resultActions.andExpect(status().isBadRequest());
    }
}