package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.rest.dto.ReactionPostDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReactionsController implements ReactionsApi {

    // TODO: inject ReactionService here

    // POST /sessions/{sessionId}/reactions — Send a live emoji reaction during a performance (S9)
    // Broadcasts the reaction via WebSocket to /topic/sessions/{sessionId}/reactions
    // Returns 201 on success, 400 if no song is currently playing, 404 if session not found
    @Override
    public ResponseEntity<Void> sessionsSessionIdReactionsPost(Long sessionId, ReactionPostDTO reactionPostDTO) {
        // TODO: delegate to reactionService.sendReaction(sessionId, reactionPostDTO)
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
