package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.VotingStatus;
import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class VotingService {

    private final VotingRoundRepository votingRoundRepository;
    private final SongRepository songRepository;
    private final VoteRepository voteRepository;

    public VotingService(VotingRoundRepository votingRoundRepository,
                         SongRepository songRepository, VoteRepository voteRepository) {
        this.votingRoundRepository = votingRoundRepository;
        this.songRepository = songRepository;
        this.voteRepository = voteRepository;
    }

    public void castVote(Long sessionId, Long votingRoundId, Long songId, User voter) {
        VotingRound round = votingRoundRepository.findById(votingRoundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voting round not found"));

        // Enforce path hierarchy
        if (!round.getSession().getId().equals(sessionId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voting round does not belong to the specified session");
        }

        // Round still open check
        if (round.getStatus() != VotingStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.GONE, "Voting round is CLOSED");
        }
        // Voter is participant check
        if (!round.getSession().getParticipants().contains(voter)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a participant");
        }

        // Song is candidate in voting round check
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Song not found"));
        if (!round.getCandidates().contains(song)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Song is not a candidate");
        }

        // Voter has not yet voted check
        if (voteRepository.existsByVotingRoundAndVoter(round, voter)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already voted");
        }

        Vote vote = new Vote();
        vote.setVotingRound(round);
        vote.setVoter(voter);
        vote.setVotedSong(song);
        voteRepository.save(vote);
    }
}