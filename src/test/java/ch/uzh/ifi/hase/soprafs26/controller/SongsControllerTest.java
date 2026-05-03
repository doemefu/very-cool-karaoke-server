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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Test
    void deleteSong_validRequest_returns204AndDelegatesToService() throws Exception {
        willDoNothing().given(songService).deleteSongFromQueue(42L, 7L, "admin-token");

        mockMvc.perform(delete("/sessions/42/songs/7")
                        .header("token", "admin-token"))
                .andExpect(status().isNoContent());

        verify(songService).deleteSongFromQueue(42L, 7L, "admin-token");
    }

    @Test
    void deleteSong_notAdmin_returns403() throws Exception {
        willThrow(new ResponseStatusException(FORBIDDEN, "Only the session admin can perform this action"))
                .given(songService).deleteSongFromQueue(eq(42L), eq(7L), any());

        mockMvc.perform(delete("/sessions/42/songs/7")
                        .header("token", "intruder-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteSong_songNotFound_returns404() throws Exception {
        willThrow(new ResponseStatusException(NOT_FOUND, "Song not found"))
                .given(songService).deleteSongFromQueue(eq(42L), eq(999L), any());

        mockMvc.perform(delete("/sessions/42/songs/999")
                        .header("token", "admin-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteSong_sessionNotFound_returns404() throws Exception {
        willThrow(new ResponseStatusException(NOT_FOUND, "Session not found"))
                .given(songService).deleteSongFromQueue(eq(99L), eq(7L), any());

        mockMvc.perform(delete("/sessions/99/songs/7")
                        .header("token", "admin-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteSong_songFromDifferentSession_returns404() throws Exception {
        willThrow(new ResponseStatusException(NOT_FOUND, "Song not found in this session"))
                .given(songService).deleteSongFromQueue(eq(42L), eq(7L), any());

        mockMvc.perform(delete("/sessions/42/songs/7")
                        .header("token", "admin-token"))
                .andExpect(status().isNotFound());
    }
}
