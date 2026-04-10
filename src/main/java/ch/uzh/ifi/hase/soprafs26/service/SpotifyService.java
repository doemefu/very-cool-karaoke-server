package ch.uzh.ifi.hase.soprafs26.service;

import tools.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class SpotifyService {

    private static final Logger log = LoggerFactory.getLogger(SpotifyService.class);
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    private static final String SEARCH_URL = "https://api.spotify.com/v1/search?q={q}&type=track&limit=5";

    @Value("${spotify.client-id:}")
    private String clientId = "";

    @Value("${spotify.client-secret:}")
    private String clientSecret = "";

    private final RestTemplate restTemplate;
    private String accessToken;

    public SpotifyService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void fetchToken() {
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

        this.accessToken = response.getBody().path("access_token").asText();
        log.info("Spotify access token refreshed");
    }

    @Scheduled(fixedDelay = 3_500_000)
    public void refreshToken() {
        fetchToken();
    }

    public List<SpotifyTrack> search(String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                SEARCH_URL, HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class, query);

        List<SpotifyTrack> tracks = new ArrayList<>();
        for (JsonNode item : response.getBody().path("tracks").path("items")) {
            tracks.add(new SpotifyTrack(
                    item.path("id").asText(),
                    item.path("name").asText(),
                    item.path("artists").get(0).path("name").asText(),
                    item.path("album").path("images").get(0).path("url").asText(),
                    item.path("duration_ms").asInt()
            ));
        }
        return tracks;
    }

    // Package-private accessors for testing
    String getAccessToken() { return accessToken; }
    void setAccessToken(String token) { this.accessToken = token; }
    void setCredentials(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }
}
