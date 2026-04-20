package ch.uzh.ifi.hase.soprafs26.websocket;

import ch.uzh.ifi.hase.soprafs26.rest.dto.SongGetDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SongWebSocketPublisherImplTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private SongWebSocketPublisherImpl publisher;

    @Test
    void broadcastQueue_sendsToCorrectTopic() {
        SongGetDTO song = new SongGetDTO();
        song.setTitle("Test Song");
        List<SongGetDTO> queue = List.of(song);

        publisher.broadcastQueue(1L, queue);

        verify(messagingTemplate).convertAndSend("/topic/sessions/1/queue", queue);
    }

    @Test
    void broadcastCurrentSong_sendsToCorrectTopic() {
        SongGetDTO song = new SongGetDTO();
        song.setTitle("Current Song");

        publisher.broadcastCurrentSong(1L, song);

        verify(messagingTemplate).convertAndSend("/topic/sessions/1/currentSong", song);
    }

    @Test
    void broadcastLyrics_sendsLyricsPayload() {
        ArgumentCaptor<LyricsPayload> captor = ArgumentCaptor.forClass(LyricsPayload.class);

        publisher.broadcastLyrics(1L, "La la la");

        verify(messagingTemplate).convertAndSend(
                org.mockito.ArgumentMatchers.eq("/topic/sessions/1/lyrics"),
                captor.capture());
        assertEquals("La la la", captor.getValue().getLyrics());
    }

    @Test
    void lyricsPayload_getterSetter_work() {
        LyricsPayload payload = new LyricsPayload();
        payload.setLyrics("Hello world");
        assertEquals("Hello world", payload.getLyrics());

        LyricsPayload payload2 = new LyricsPayload("Direct");
        assertEquals("Direct", payload2.getLyrics());
    }
}
