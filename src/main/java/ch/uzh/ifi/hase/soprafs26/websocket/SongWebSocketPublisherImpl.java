package ch.uzh.ifi.hase.soprafs26.websocket;

import ch.uzh.ifi.hase.soprafs26.rest.dto.SongGetDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SongWebSocketPublisherImpl implements SongWebSocketPublisher {

    private static final String TOPIC_PREFIX = "/topic/sessions/";

    private final SimpMessagingTemplate messagingTemplate;

    public SongWebSocketPublisherImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void broadcastQueue(Long sessionId, List<SongGetDTO> queue) {
        messagingTemplate.convertAndSend(TOPIC_PREFIX + sessionId + "/queue", queue);
    }

    @Override
    public void broadcastCurrentSong(Long sessionId, SongGetDTO currentSong) {
        messagingTemplate.convertAndSend(TOPIC_PREFIX + sessionId + "/currentSong", currentSong);
    }

    @Override
    public void broadcastLyrics(Long sessionId, String lyrics) {
        messagingTemplate.convertAndSend(
                TOPIC_PREFIX + sessionId + "/lyrics",
                new LyricsPayload(lyrics)
        );
    }
}

