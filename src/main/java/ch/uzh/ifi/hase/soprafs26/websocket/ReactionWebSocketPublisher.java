package ch.uzh.ifi.hase.soprafs26.websocket;

import ch.uzh.ifi.hase.soprafs26.rest.dto.ReactionGetDTO;

/**
 * Publishes WebSocket broadcasts for live reaction events.
 * <p>
 * Channel:
 * /topic/sessions/{sessionId}/reactions – triggered by POST /sessions/{id}/reactions
 */
public interface ReactionWebSocketPublisher {

    void broadcastReaction(Long sessionId, ReactionGetDTO reaction);
}
