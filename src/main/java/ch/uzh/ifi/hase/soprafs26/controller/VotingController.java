package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.VotePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.VotingRoundGetDTO;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.service.VotingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class VotingController implements VotingApi {

    private final UserService userService;
    private final VotingService votingService;
    private final HttpServletRequest request;

    public VotingController(UserService userService, VotingService votingService, HttpServletRequest request) {
        this.userService = userService;
        this.votingService = votingService;
        this.request = request;
    }

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
    public ResponseEntity<Void> sessionsSessionIdVotingRoundsRoundIdVotesPost(
            Long sessionId, Long roundId, VotePostDTO votePostDTO) {
        String token = request.getHeader("token");
        User voter = userService.getUserByToken(token);
        votingService.castVote(sessionId, roundId, votePostDTO.getSongId(), voter);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
