package ch.uzh.ifi.hase.soprafs26.websocket;

import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;

import java.util.List;

/**
 * Publishes WebSocket broadcasts for session-related events.
 * <p>
 * Channels:
 * /topic/sessions/{sessionId}/status       – triggered by PUT /sessions/{id}
 * /topic/sessions/{sessionId}/participants – triggered by POST/PUT /sessions/{id}/participants
 */
public interface SessionWebSocketPublisher {

    void broadcastSessionStatus(Long sessionId, SessionGetDTO session);

    void broadcastParticipants(Long sessionId, List<UserGetDTO> participants);
}
