package ch.uzh.ifi.hase.soprafs26.websocket;

import ch.uzh.ifi.hase.soprafs26.rest.dto.ReactionGetDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class ReactionWebSocketPublisherImpl implements ReactionWebSocketPublisher {

    private static final String TOPIC_PREFIX = "/topic/sessions/";

    private final SimpMessagingTemplate messagingTemplate;

    public ReactionWebSocketPublisherImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void broadcastReaction(Long sessionId, ReactionGetDTO reaction) {
        messagingTemplate.convertAndSend(
                TOPIC_PREFIX + sessionId + "/reactions", reaction);
    }
}