package ch.uzh.ifi.hase.soprafs26.websocket;

import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SessionWebSocketPublisherImpl implements SessionWebSocketPublisher {

    private static final String TOPIC_PREFIX = "/topic/sessions/";
    private final SimpMessagingTemplate messagingTemplate;

    public SessionWebSocketPublisherImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void broadcastSessionStatus(Long sessionId, SessionGetDTO session) {
        messagingTemplate.convertAndSend(TOPIC_PREFIX + sessionId + "/status", session);
    }

    @Override
    public void broadcastParticipants(Long sessionId, List<UserGetDTO> participants) {
        messagingTemplate.convertAndSend(TOPIC_PREFIX + sessionId + "/participants", participants);
    }
}