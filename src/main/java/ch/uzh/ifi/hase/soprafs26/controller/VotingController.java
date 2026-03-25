package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.rest.dto.VotePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.VotingRoundGetDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class VotingController implements VotingApi {

    // TODO: inject VotingService here
    // VotingController(VotingService votingService) { this.votingService = votingService; }

    // GET /sessions/{sessionId}/votingRounds — List all voting rounds of a session
    // Voting rounds are started automatically by the server when a song ends
    // Returns 200 + list of VotingRoundGetDTO
    @Override
    public ResponseEntity<List<VotingRoundGetDTO>> sessionsSessionIdVotingRoundsGet(Long sessionId) {
        // TODO: delegate to votingService.getRounds(sessionId)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // GET /sessions/{sessionId}/votingRounds/{roundId} — Get a specific voting round (S14)
    // Returns candidates sorted by vote count (leaderboard)
    // Returns 200 + VotingRoundGetDTO, 404 if not found
    @Override
    public ResponseEntity<VotingRoundGetDTO> sessionsSessionIdVotingRoundsRoundIdGet(Long sessionId, Long roundId) {
        // TODO: delegate to votingService.getRound(sessionId, roundId)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // POST /sessions/{sessionId}/votingRounds/{roundId}/votes — Cast a vote (S12)
    // Returns 201 on success
    // Returns 400 if song is not a candidate, 409 if user already voted, 410 if round is CLOSED
    @Override
    public ResponseEntity<Void> sessionsSessionIdVotingRoundsRoundIdVotesPost(Long sessionId, Long roundId, VotePostDTO votePostDTO) {
        // TODO: verify user hasn't voted yet, delegate to votingService.castVote(sessionId, roundId, votePostDTO)
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
