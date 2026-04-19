package ch.uzh.ifi.hase.soprafs26.service;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class SpotifyServiceTest {

    private MockRestServiceServer mockServer;
    private SpotifyService spotifyService;

    @BeforeEach
    void setup() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        spotifyService = new SpotifyService(restTemplate);
        spotifyService.setAccessToken("test-token");
    }

    @Test
    void fetchToken_storesAccessTokenFromResponse() {
        spotifyService.setCredentials("test-client-id", "test-client-secret");
        String tokenResponse = """
                {"access_token":"new-token","token_type":"Bearer","expires_in":3600}
                """;
        mockServer.expect(requestTo("https://accounts.spotify.com/api/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(tokenResponse, MediaType.APPLICATION_JSON));

        spotifyService.fetchToken();

        assertEquals("new-token", spotifyService.getAccessToken());
    }

    @Test
    void search_validQuery_returnsMappedTracks() {
        String spotifyResponse = """
                {
                  "tracks": {
                    "items": [{
                      "id": "track123",
                      "name": "Dancing Queen",
                      "artists": [{"name": "ABBA"}],
                      "album": {"images": [{"url": "http://img.test/art.jpg"}]},
                      "duration_ms": 230000
                    }]
                  }
                }
                """;
        mockServer.expect(requestTo(Matchers.containsString("/v1/search")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer test-token"))
                .andRespond(withSuccess(spotifyResponse, MediaType.APPLICATION_JSON));

        List<SpotifyTrack> results = spotifyService.search("Dancing Queen");

        assertEquals(1, results.size());
        SpotifyTrack track = results.get(0);
        assertEquals("track123", track.spotifyId());
        assertEquals("Dancing Queen", track.title());
        assertEquals("ABBA", track.artist());
        assertEquals("http://img.test/art.jpg", track.albumArt());
        assertEquals(230000, track.durationMs());
    }

    @Test
    void search_spotifyReturns401_refreshesTokenAndRetries() {
        String tokenResponse = """
                {"access_token":"refreshed-token","token_type":"Bearer","expires_in":3600}
                """;
        String spotifyResponse = "{\"tracks\":{\"items\":[]}}";

        spotifyService.setCredentials("id", "secret");

        // First search → 401
        mockServer.expect(requestTo(Matchers.containsString("/v1/search")))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        // Token refresh
        mockServer.expect(requestTo("https://accounts.spotify.com/api/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(tokenResponse, MediaType.APPLICATION_JSON));
        // Retry search → success
        mockServer.expect(requestTo(Matchers.containsString("/v1/search")))
                .andRespond(withSuccess(spotifyResponse, MediaType.APPLICATION_JSON));

        List<SpotifyTrack> results = spotifyService.search("ABBA");

        assertTrue(results.isEmpty());
        assertEquals("refreshed-token", spotifyService.getAccessToken());
    }

    @Test
    void search_spotifyReturns401TwiceAfterRetry_throwsBadGateway() {
        String tokenResponse = """
                {"access_token":"refreshed-token","token_type":"Bearer","expires_in":3600}
                """;

        spotifyService.setCredentials("id", "secret");

        // First search → 401
        mockServer.expect(requestTo(Matchers.containsString("/v1/search")))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        // Token refresh
        mockServer.expect(requestTo("https://accounts.spotify.com/api/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(tokenResponse, MediaType.APPLICATION_JSON));
        // Retry search → 401 again
        mockServer.expect(requestTo(Matchers.containsString("/v1/search")))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> spotifyService.search("ABBA"));
        assertEquals(HttpStatus.BAD_GATEWAY, ex.getStatusCode());
    }

    @Test
    void search_emptyItemsArray_returnsEmptyList() {
        String spotifyResponse = "{\"tracks\":{\"items\":[]}}";
        mockServer.expect(requestTo(Matchers.containsString("/v1/search")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(spotifyResponse, MediaType.APPLICATION_JSON));

        List<SpotifyTrack> results = spotifyService.search("nonexistent song xyz");

        assertTrue(results.isEmpty());
    }
}
