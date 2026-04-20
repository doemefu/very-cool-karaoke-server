package ch.uzh.ifi.hase.soprafs26.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

@Service
public class SpotifyService {

    private static final Logger log = LoggerFactory.getLogger(SpotifyService.class);
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    private static final String SEARCH_URL = "https://api.spotify.com/v1/search?q={q}&type=track&limit=5";
    private final RestTemplate restTemplate;
    @Value("${SPOTIFY_CLIENT_ID:}")
    private String clientId = "";
    @Value("${SPOTIFY_CLIENT_SECRET:}")
    private String clientSecret = "";
    private String accessToken;

    public SpotifyService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void fetchToken() {
        // @Value may not resolve in all run environments; fall back to System.getenv()
        if (clientId.isBlank()) clientId = System.getenv().getOrDefault("SPOTIFY_CLIENT_ID", "");
        if (clientSecret.isBlank()) clientSecret = System.getenv().getOrDefault("SPOTIFY_CLIENT_SECRET", "");

        if (clientId.isBlank() || clientSecret.isBlank()) {
            log.warn("Spotify credentials not configured — skipping token fetch");
            return;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                TOKEN_URL, new HttpEntity<>(body, headers), JsonNode.class);

        JsonNode tokenBody = response.getBody();
        if (tokenBody == null) {
            log.warn("Empty response from Spotify token endpoint");
            return;
        }
        this.accessToken = tokenBody.path("access_token").textValue();
        log.info("Spotify access token refreshed");
    }

    @Scheduled(fixedDelay = 3_500_000)
    public void refreshToken() {
        fetchToken();
    }

    public List<SpotifyTrack> search(String query) {
        try {
            return doSearch(query);
        }
        catch (HttpClientErrorException.Unauthorized e) {
            log.warn("Spotify token rejected (401) — refreshing and retrying once");
            fetchToken();
            try {
                return doSearch(query);
            }
            catch (HttpClientErrorException.Unauthorized retryEx) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Spotify authentication failed after token refresh");
            }
        }
    }

    private List<SpotifyTrack> doSearch(String query) {
        if (accessToken == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Spotify not configured");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                SEARCH_URL, HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class, query);

        JsonNode searchBody = response.getBody();
        List<SpotifyTrack> tracks = new ArrayList<>();
        if (searchBody == null) return tracks;
        for (JsonNode item : searchBody.path("tracks").path("items")) {
            tracks.add(new SpotifyTrack(
                    item.path("id").textValue(),
                    item.path("name").textValue(),
                    item.path("artists").get(0).path("name").textValue(),
                    item.path("album").path("images").get(0).path("url").textValue(),
                    item.path("duration_ms").asInt()
            ));
        }
        return tracks;
    }

    // Package-private accessors for testing
    String getAccessToken() {
        return accessToken;
    }

    void setAccessToken(String token) {
        this.accessToken = token;
    }

    void setCredentials(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }
}
