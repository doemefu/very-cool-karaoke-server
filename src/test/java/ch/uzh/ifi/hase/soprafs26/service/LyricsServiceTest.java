package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class LyricsServiceTest {

    private MockRestServiceServer mockServer;
    private LyricsService lyricsService;

    @BeforeEach
    void setup() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        lyricsService = new LyricsService(restTemplate);
    }

    @Test
    void fetchLyrics_found_returnsLyricsText() {
        String response = "{\"lyrics\": \"We are the champions\\nNo time for losers\"}";
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("Queen")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        String lyrics = lyricsService.fetchLyrics("Queen", "We Are the Champions");

        assertEquals("We are the champions\nNo time for losers", lyrics);
    }

    @Test
    void fetchLyrics_notFound_returnsNull() {
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("lyrics.ovh")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withResourceNotFound());

        String lyrics = lyricsService.fetchLyrics("Unknown Artist", "Unknown Song");

        assertNull(lyrics);
    }
}
