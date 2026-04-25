package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.SessionStatus;
import ch.uzh.ifi.hase.soprafs26.constant.VotingStatus;
import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.SessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SongRepository;
import ch.uzh.ifi.hase.soprafs26.repository.VoteRepository;
import ch.uzh.ifi.hase.soprafs26.repository.VotingRoundRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SongGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.VotingRoundGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.websocket.VotingWebSocketPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class VotingService {

    private final VotingRoundRepository votingRoundRepository;
    private final SongRepository songRepository;
    private final VoteRepository voteRepository;
    private final SessionService sessionService;
    private final SongService songService;
    private final VotingWebSocketPublisher votingWebSocketPublisher;

    public VotingService(VotingRoundRepository votingRoundRepository,
                         SongRepository songRepository, VoteRepository voteRepository,
                         SessionService sessionService, SongService songService,
                         VotingWebSocketPublisher votingWebSocketPublisher) {
        this.votingRoundRepository = votingRoundRepository;
        this.songRepository = songRepository;
        this.voteRepository = voteRepository;
        this.sessionService = sessionService;
        this.songService = songService;
        this.votingWebSocketPublisher = votingWebSocketPublisher;
    }

    @Transactional
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

    @Transactional(readOnly = true)
    public VotingRound getVotingRound(Long sessionId, Long roundId) {
        VotingRound round = votingRoundRepository.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voting round not found"));

        if (!round.getSession().getId().equals(sessionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Voting round not in this session");
        }
        return round;
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> getVoteCounts(VotingRound round) {
        return voteRepository.countVotesPerSong(round).stream().collect(Collectors.toMap(
                VoteRepository.SongVoteCount::getSongId, VoteRepository.SongVoteCount::getCount));
    }

    @Transactional
    public void createVotingRound(Long sessionId) {
        Session session = sessionService.getSessionById(sessionId);

        if (votingRoundRepository.existsBySessionAndStatus(session, VotingStatus.OPEN)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "An open voting round already exists for this session");
        }

        List<Song> playlist = session.getPlaylist().stream()
                .filter(s -> !Boolean.TRUE.equals(s.getPerformed()))
                .collect(Collectors.toCollection(ArrayList::new));

        if (playlist.size() <= 1) {
            songService.nextSong(sessionId);
            return;
        }

        VotingRound round = new VotingRound();
        round.setSession(session);
        round.setStatus(VotingStatus.OPEN);
        round.setStartsAt(LocalDateTime.now());
        round.setCandidates(playlist);
        VotingRound savedRound = votingRoundRepository.save(round);

        Map<Long, Long> counts = getVoteCounts(savedRound);
        VotingRoundGetDTO roundDTO = DTOMapper.INSTANCE.toVotingRoundGetDTO(savedRound, counts);
        votingWebSocketPublisher.broadcastVotingRound(sessionId, roundDTO);
    }
}