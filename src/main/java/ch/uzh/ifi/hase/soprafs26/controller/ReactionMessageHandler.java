package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ReactionGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ReactionPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.service.SessionService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.websocket.ReactionWebSocketPublisher;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Controller
public class ReactionMessageHandler {

    private final SessionService sessionService;
    private final UserService userService;
    private final ReactionWebSocketPublisher reactionPublisher;

    public ReactionMessageHandler(SessionService sessionService,
                                  UserService userService,
                                  ReactionWebSocketPublisher reactionPublisher) {
        this.sessionService = sessionService;
        this.userService = userService;
        this.reactionPublisher = reactionPublisher;
    }

    @MessageMapping("/sessions/{sessionId}/reactions")
    public void handleReaction(@DestinationVariable Long sessionId,
                               @Payload Map<String, Object> payload) {

        Long userId = Long.valueOf(payload.get("userId").toString());
        String type = payload.get("type").toString();

        Set<User> participants = sessionService.getParticipants(sessionId);
        User sender = participants.stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "User " + userId + " is not a participant of session " + sessionId));

        ReactionGetDTO reaction = new ReactionGetDTO();
        reaction.setType(ch.uzh.ifi.hase.soprafs26.constant.ReactionType.fromValue(type));
        reaction.setSentAt(Instant.now());

        UserGetDTO senderDTO = new UserGetDTO();
        senderDTO.setId(sender.getId());
        senderDTO.setUsername(sender.getUsername());
        reaction.setSender(senderDTO);

        reactionPublisher.broadcastReaction(sessionId, reaction);
    }
}