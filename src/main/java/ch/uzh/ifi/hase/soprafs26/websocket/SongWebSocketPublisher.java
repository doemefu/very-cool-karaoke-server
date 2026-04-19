package ch.uzh.ifi.hase.soprafs26.websocket;

import ch.uzh.ifi.hase.soprafs26.rest.dto.SongGetDTO;

import java.util.List;

/**
 * Publishes WebSocket broadcasts for song-related events.
 * <p>
 * Channels:
 * /topic/sessions/{sessionId}/currentSong – triggered by skip, natural end, or mark-as-played
 * /topic/sessions/{sessionId}/queue       – triggered by POST/DELETE /sessions/{id}/songs
 */
public interface SongWebSocketPublisher {

    void broadcastCurrentSong(Long sessionId, SongGetDTO currentSong);

    void broadcastQueue(Long sessionId, List<SongGetDTO> queue);
    void broadcastLyrics(Long sessionId, String lyrics);
}
