package ch.uzh.ifi.hase.soprafs26.websocket;

import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SessionWebSocketPublisherImplTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private SessionWebSocketPublisherImpl publisher;

    @Test
    void broadcastSessionStatus_sendsToCorrectTopic() {
        SessionGetDTO dto = new SessionGetDTO();
        publisher.broadcastSessionStatus(42L, dto);
        verify(messagingTemplate).convertAndSend("/topic/sessions/42/status", dto);
    }

    @Test
    void broadcastParticipants_sendsToCorrectTopic() {
        List<UserGetDTO> participants = List.of(new UserGetDTO());
        publisher.broadcastParticipants(42L, participants);
        verify(messagingTemplate).convertAndSend("/topic/sessions/42/participants", participants);
    }
}