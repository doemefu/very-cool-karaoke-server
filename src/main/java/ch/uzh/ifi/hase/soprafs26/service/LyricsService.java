package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

@Service
public class LyricsService {

    private static final Logger log = LoggerFactory.getLogger(LyricsService.class);
    private static final String LYRICS_URL = "https://api.lyrics.ovh/v1/{artist}/{title}";

    private final RestTemplate restTemplate;

    public LyricsService(@Qualifier("lyricsRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Fetches lyrics for the given artist and title.
     *
     * @return lyrics text, or null if not found or if the request cannot be completed due to
     * connection/read failures (including configured timeouts of 500 ms connect / 1000 ms read)
     */
    public String fetchLyrics(String artist, String title) {
        try {
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(LYRICS_URL, JsonNode.class, artist, title);
            return response.getBody().path("lyrics").asText(null);
        }
        catch (HttpClientErrorException.NotFound | ResourceAccessException e) {
            if (e instanceof ResourceAccessException) {
                log.debug("Lyrics request failed for artist='{}', title='{}'", artist, title, e);
            }
            return null;
        }
    }
}
