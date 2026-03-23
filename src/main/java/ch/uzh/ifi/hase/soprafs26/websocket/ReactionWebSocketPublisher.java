package ch.uzh.ifi.hase.soprafs26.websocket;

import ch.uzh.ifi.hase.soprafs26.rest.dto.ReactionGetDTO;

/**
 * Publishes WebSocket broadcasts for live reaction events.
 * <p>
 * Channel:
 * /topic/sessions/{sessionId}/reactions – triggered by POST /sessions/{id}/reactions
 * <p>
 * NOTE: ReactionGetDTO does not exist yet in the REST layer – create it with:
 * id (Long), type (ReactionType), sentAt (Instant), sender (UserGetDTO),
 * duringPerformance (SongGetDTO)
 */
public interface ReactionWebSocketPublisher {

    void broadcastReaction(Long sessionId, ReactionGetDTO reaction);
}
