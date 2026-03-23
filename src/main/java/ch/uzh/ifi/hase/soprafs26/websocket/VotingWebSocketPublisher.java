package ch.uzh.ifi.hase.soprafs26.websocket;

import ch.uzh.ifi.hase.soprafs26.rest.dto.VotingRoundGetDTO;

/**
 * Publishes WebSocket broadcasts for voting-related events.
 * <p>
 * Channel:
 * /topic/sessions/{sessionId}/votingRound – triggered when a round starts,
 * vote counts change, or the round closes
 */
public interface VotingWebSocketPublisher {

    void broadcastVotingRound(Long sessionId, VotingRoundGetDTO votingRound);
}
