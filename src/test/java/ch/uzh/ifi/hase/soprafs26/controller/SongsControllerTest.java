package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.rest.dto.SongGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SongSearchResultDTO;
import ch.uzh.ifi.hase.soprafs26.service.SongService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SongsController.class)
class SongsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SongService songService;

    @Test
    void songsSearch_validQuery_returns200WithResults() throws Exception {
        SongSearchResultDTO dto = new SongSearchResultDTO();
        dto.setSpotifyId("track123");
        dto.setTitle("Dancing Queen");
        dto.setArtist("ABBA");
        dto.setAlbumArt("http://img/art.jpg");
        dto.setDurationMs(230000);
        dto.setLyricsAvailable(true);

        given(songService.search("ABBA")).willReturn(List.of(dto));

        mockMvc.perform(get("/songs/search")
                        .param("query", "ABBA")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].spotifyId", is("track123")))
                .andExpect(jsonPath("$[0].title", is("Dancing Queen")))
                .andExpect(jsonPath("$[0].artist", is("ABBA")))
                .andExpect(jsonPath("$[0].albumArt", is("http://img/art.jpg")))
                .andExpect(jsonPath("$[0].durationMs", is(230000)))
                .andExpect(jsonPath("$[0].lyricsAvailable", is(true)));
    }

    @Test
    void songsSearch_blankQuery_returns400() throws Exception {
        mockMvc.perform(get("/songs/search")
                        .param("query", "")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void songsSearch_missingQuery_returns400() throws Exception {
        mockMvc.perform(get("/songs/search")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addSongToQueue_validRequest_returns201WithSong() throws Exception {
        SongGetDTO response = new SongGetDTO();
        response.setId(1L);
        response.setTitle("Dancing Queen");
        response.setArtist("ABBA");
        response.setSpotifyId("track123");

        given(songService.addToQueue(eq(42L), any())).willReturn(response);

        mockMvc.perform(post("/sessions/42/songs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"spotifyId\":\"track123\",\"title\":\"Dancing Queen\",\"artist\":\"ABBA\",\"durationMs\":230000}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Dancing Queen")))
                .andExpect(jsonPath("$.artist", is("ABBA")));
    }

    @Test
    void addSongToQueue_sessionNotFound_returns404() throws Exception {
        given(songService.addToQueue(eq(99L), any()))
                .willThrow(new ResponseStatusException(NOT_FOUND, "Session not found"));

        mockMvc.perform(post("/sessions/99/songs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"spotifyId\":\"track123\",\"title\":\"Dancing Queen\",\"artist\":\"ABBA\",\"durationMs\":230000}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void addSongToQueue_missingTitle_returns400() throws Exception {
        mockMvc.perform(post("/sessions/42/songs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"spotifyId\":\"track123\",\"artist\":\"ABBA\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
