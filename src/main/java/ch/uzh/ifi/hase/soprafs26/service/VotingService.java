package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.VotingStatus;
import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.SongRepository;
import ch.uzh.ifi.hase.soprafs26.repository.VoteRepository;
import ch.uzh.ifi.hase.soprafs26.repository.VotingRoundRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.VotingRoundGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.websocket.VotingWebSocketPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.context.ApplicationContext;

import java.time.ZoneOffset;
import java.util.*;

import java.time.LocalDateTime;
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
    private final Random random = new Random();
    private final TaskScheduler taskScheduler;
    private final ApplicationContext applicationContext;

    public VotingService(VotingRoundRepository votingRoundRepository,
                         SongRepository songRepository, VoteRepository voteRepository,
                         SessionService sessionService, @Lazy SongService songService,
                         VotingWebSocketPublisher votingWebSocketPublisher,
                         ApplicationContext applicationContext,
                         TaskScheduler taskScheduler) {
        this.votingRoundRepository = votingRoundRepository;
        this.songRepository = songRepository;
        this.voteRepository = voteRepository;
        this.sessionService = sessionService;
        this.songService = songService;
        this.votingWebSocketPublisher = votingWebSocketPublisher;
        this.taskScheduler = taskScheduler;
        this.applicationContext = applicationContext;
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
        boolean isParticipant = round.getSession().getParticipants().stream()
                .anyMatch(p -> p.getId().equals(voter.getId()));
        if (!isParticipant) {
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
        try {
            voteRepository.saveAndFlush(vote);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already voted");
        }

        Map<Long, Long> counts = getVoteCounts(round);
        VotingRoundGetDTO roundDTO = DTOMapper.INSTANCE.toVotingRoundGetDTO(round, counts);
        votingWebSocketPublisher.broadcastVotingRound(sessionId, roundDTO);
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

        List<VotingRound> openRounds = votingRoundRepository.findBySessionAndStatus(session, VotingStatus.OPEN);
        for (VotingRound stale : openRounds) {
            stale.setStatus(VotingStatus.CLOSED);
            stale.setEndsAt(LocalDateTime.now());
        }
        if (!openRounds.isEmpty()) {
            votingRoundRepository.saveAll(openRounds);
        }

        List<Song> playlist = session.getPlaylist().stream()
                .filter(s -> !Boolean.TRUE.equals(s.getPerformed()))
                .collect(Collectors.toCollection(ArrayList::new));

        if (playlist.size() <= 1) {
            songService.nextSong(sessionId);
            return;
        }

        LocalDateTime startsAt = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime endsAt = startsAt.plusSeconds(30);
        VotingRound round = new VotingRound();
        round.setSession(session);
        round.setStatus(VotingStatus.OPEN);
        round.setStartsAt(startsAt);
        round.setEndsAt(endsAt);
        round.setCandidates(playlist);
        VotingRound savedRound = votingRoundRepository.save(round);

        Map<Long, Long> counts = getVoteCounts(savedRound);
        VotingRoundGetDTO roundDTO = DTOMapper.INSTANCE.toVotingRoundGetDTO(savedRound, counts);
        votingWebSocketPublisher.broadcastVotingRound(sessionId, roundDTO);

        VotingService self = applicationContext.getBean(VotingService.class);
        taskScheduler.schedule(
                () -> self.finishVotingRoundAndPlayNextSong(sessionId, savedRound.getId()),
                endsAt.toInstant(ZoneOffset.UTC)
        );
    }

    private void moveWinnerToFrontOfQueue(long sessionId, Song winner) {
        Session session = sessionService.getSessionById(sessionId);
        List<Song> playlist = session.getPlaylist();
        Long winnerId = winner.getId();
        int indexOfWinner = -1;
        for (int i = 0; i < playlist.size(); i++) {
            if (Objects.equals(playlist.get(i).getId(), winnerId)) {
                indexOfWinner = i;
                break;
            }
        }
        if (indexOfWinner == -1) {
            return;
        }
        int indexOfFirstUnplayedSong = -1;

        for (int i = 0; i < playlist.size(); i++) {
            if (!Boolean.TRUE.equals(playlist.get(i).getPerformed())) {
                indexOfFirstUnplayedSong = i;
                break;
            }
        }

        if (indexOfFirstUnplayedSong != -1 && indexOfWinner != indexOfFirstUnplayedSong) {
            Collections.swap(playlist, indexOfWinner, indexOfFirstUnplayedSong);
        }
    }


    @Transactional
    public void finishVotingRoundAndPlayNextSong(Long sessionId, Long votingRoundId) {
        VotingRound round = votingRoundRepository.findByIdWithCandidates(votingRoundId).orElse(null);
        if (round == null || round.getStatus() == VotingStatus.CLOSED) {
            return;
        }

        round.setStatus(VotingStatus.CLOSED);
        round.setEndsAt(LocalDateTime.now());
        votingRoundRepository.save(round);

        Map<Long, Long> finalCounts = getVoteCounts(round);
        votingWebSocketPublisher.broadcastVotingRound(sessionId, DTOMapper.INSTANCE.toVotingRoundGetDTO(round, finalCounts));

        Map<Long, Long> counts = getVoteCounts(round);
        long maxVotes = counts.values().stream().max(Long::compare).orElse(0L);

        List<Song> winnerCandidates = round.getCandidates().stream()
                .filter(s -> counts.getOrDefault(s.getId(), 0L) == maxVotes)
                .toList();

        Song votingRoundSongWinner = winnerCandidates.get(random.nextInt(winnerCandidates.size()));
        moveWinnerToFrontOfQueue(sessionId, votingRoundSongWinner);
        songService.broadcastVotingRoundSongWinner(sessionId, votingRoundSongWinner);
    }

    @Transactional(readOnly = true)
    public List<VotingRoundGetDTO> getRoundsForSession(Long sessionId) {
        Session session = sessionService.getSessionById(sessionId);
        List<VotingRound> rounds = votingRoundRepository.findBySessionOrderByStartsAtAsc(session);
        return rounds.stream()
                .map(r -> {
                    Map<Long, Long> counts = getVoteCounts(r);
                    return DTOMapper.INSTANCE.toVotingRoundGetDTO(r, counts);
                })
                .toList();
    }
}