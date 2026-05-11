package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.constant.ReactionType;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ReactionGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.service.SessionService;
import ch.uzh.ifi.hase.soprafs26.websocket.ReactionWebSocketPublisher;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Controller
public class ReactionMessageHandler {

    private final SessionService sessionService;
    private final ReactionWebSocketPublisher reactionPublisher;

    public ReactionMessageHandler(SessionService sessionService,
                                  ReactionWebSocketPublisher reactionPublisher) {
        this.sessionService = sessionService;
        this.reactionPublisher = reactionPublisher;
    }

    @Transactional(readOnly = true)
    @MessageMapping("/sessions/{sessionId}/reactions")
    public void handleReaction(@DestinationVariable Long sessionId,
                               @Payload Map<String, Object> payload) {

        Object rawUserId = payload.get("userId");
        Object rawType = payload.get("type");
        if (rawUserId == null || rawType == null) {
            throw new IllegalArgumentException("Payload must contain 'userId' and 'type'");
        }

        Long userId = Long.valueOf(rawUserId.toString());
        String type = rawType.toString();

        Set<User> participants = sessionService.getParticipants(sessionId);
        User sender = participants.stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "User " + userId + " is not a participant of session " + sessionId));

        ReactionType reactionType;
        try {
            reactionType = ReactionType.fromValue(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown reaction type: " + type);
        }

        ReactionGetDTO reaction = new ReactionGetDTO();
        reaction.setType(reactionType);
        reaction.setSentAt(Instant.now());

        UserGetDTO senderDTO = new UserGetDTO();
        senderDTO.setId(sender.getId());
        senderDTO.setUsername(sender.getUsername());
        reaction.setSender(senderDTO);

        reactionPublisher.broadcastReaction(sessionId, reaction);
    }
}
