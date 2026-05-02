package ch.uzh.ifi.hase.soprafs26.websocket;

import ch.uzh.ifi.hase.soprafs26.rest.dto.VotingRoundGetDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class VotingWebSocketPublisherImpl implements VotingWebSocketPublisher {

    private static final String TOPIC_PREFIX = "/topic/sessions/";

    private final SimpMessagingTemplate messagingTemplate;

    public VotingWebSocketPublisherImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void broadcastVotingRound(Long sessionId, VotingRoundGetDTO votingRound) {
        messagingTemplate.convertAndSend(TOPIC_PREFIX + sessionId + "/votingRound", votingRound);
    }
}