package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.VotingStatus;
import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.repository.SongRepository;
import ch.uzh.ifi.hase.soprafs26.repository.VoteRepository;
import ch.uzh.ifi.hase.soprafs26.repository.VotingRoundRepository;
import ch.uzh.ifi.hase.soprafs26.websocket.VotingWebSocketPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VotingServiceTest {

    @Mock
    private VotingRoundRepository votingRoundRepository;

    @Mock
    private SongRepository songRepository;

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private SessionService sessionService;

    @Mock
    private SongService songService;

    @Mock
    private VotingWebSocketPublisher votingWebSocketPublisher;

    @InjectMocks
    private VotingService votingService;

    private Session session;
    private User voter;
    private User nonParticipant;
    private Song candidateSong;
    private Song nonCandidateSong;
    private VotingRound votingRound;

    @BeforeEach
    void setUp() {
        voter = new User();
        voter.setId(1L);
        voter.setUsername("singer1");
        voter.setToken("voter-token");

        nonParticipant = new User();
        nonParticipant.setId(2L);
        nonParticipant.setUsername("outsider");
        nonParticipant.setToken("outsider-token");

        session = new Session();
        session.setId(10L);
        session.setName("Karaoke Night");
        session.setGamePin("123456");
        session.addParticipant(voter);

        candidateSong = new Song();
        candidateSong.setId(100L);
        candidateSong.setTitle("Bohemian Rhapsody");
        candidateSong.setArtist("Queen");
        candidateSong.setSpotifyId("spotify:123");
        candidateSong.setDurationMs(354000);
        candidateSong.setPerformed(false);

        nonCandidateSong = new Song();
        nonCandidateSong.setId(200L);
        nonCandidateSong.setTitle("Yesterday");
        nonCandidateSong.setArtist("The Beatles");
        nonCandidateSong.setSpotifyId("spotify:456");
        nonCandidateSong.setDurationMs(125000);
        nonCandidateSong.setPerformed(false);

        votingRound = new VotingRound();
        votingRound.setId(50L);
        votingRound.setSession(session);
        votingRound.setStatus(VotingStatus.OPEN);
        votingRound.setStartsAt(LocalDateTime.now());
        votingRound.setEndsAt(LocalDateTime.now().plusSeconds(30));
        votingRound.getCandidates().add(candidateSong);
    }


    // Check valid vote

    @Test
    void castVote_validInput_savesVote() {
        when(votingRoundRepository.findById(50L)).thenReturn(Optional.of(votingRound));
        when(songRepository.findById(100L)).thenReturn(Optional.of(candidateSong));
        when(voteRepository.existsByVotingRoundAndVoter(votingRound, voter)).thenReturn(false);
        when(voteRepository.save(any(Vote.class))).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> votingService.castVote(10L, 50L, 100L, voter));

        verify(voteRepository, times(1)).save(any(Vote.class));
    }

    // Check fields correctly set in vote

    @Test
    void castVote_validInput_setsCorrectFieldsOnVote() {
        when(votingRoundRepository.findById(50L)).thenReturn(Optional.of(votingRound));
        when(songRepository.findById(100L)).thenReturn(Optional.of(candidateSong));
        when(voteRepository.existsByVotingRoundAndVoter(votingRound, voter)).thenReturn(false);
        when(voteRepository.save(any(Vote.class))).thenAnswer(i -> i.getArgument(0));

        votingService.castVote(10L, 50L, 100L, voter);

        verify(voteRepository).save(argThat(vote ->
                vote.getVotingRound().equals(votingRound) &&
                        vote.getVoter().equals(voter) &&
                        vote.getVotedSong().equals(candidateSong)
        ));
    }


    // Check invalid voting round

    @Test
    void castVote_roundNotFound_throwsNotFound() {
        when(votingRoundRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> votingService.castVote(10L, 99L, 100L, voter));

        assertEquals(404, ex.getStatusCode().value());
        verify(voteRepository, never()).save(any());
    }


    // Check round - session mismatch

    @Test
    void castVote_roundBelongsToDifferentSession_throwsBadRequest() {
        when(votingRoundRepository.findById(50L)).thenReturn(Optional.of(votingRound));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> votingService.castVote(999L, 50L, 100L, voter));

        assertEquals(400, ex.getStatusCode().value());
        verify(voteRepository, never()).save(any());
    }


    // Check round is closed

    @Test
    void castVote_roundClosed_throwsGone() {
        votingRound.setStatus(VotingStatus.CLOSED);
        when(votingRoundRepository.findById(50L)).thenReturn(Optional.of(votingRound));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> votingService.castVote(10L, 50L, 100L, voter));

        assertEquals(410, ex.getStatusCode().value());
        verify(voteRepository, never()).save(any());
    }


    // Check voter is not participant

    @Test
    void castVote_notAParticipant_throwsForbidden() {
        when(votingRoundRepository.findById(50L)).thenReturn(Optional.of(votingRound));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> votingService.castVote(10L, 50L, 100L, nonParticipant));

        assertEquals(403, ex.getStatusCode().value());
        verify(voteRepository, never()).save(any());
    }


    // Check song not found

    @Test
    void castVote_songNotFound_throwsNotFound() {
        when(votingRoundRepository.findById(50L)).thenReturn(Optional.of(votingRound));
        when(songRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> votingService.castVote(10L, 50L, 999L, voter));

        assertEquals(404, ex.getStatusCode().value());
        verify(voteRepository, never()).save(any());
    }


    // Check song is not candidate

    @Test
    void castVote_songNotACandidate_throwsBadRequest() {
        when(votingRoundRepository.findById(50L)).thenReturn(Optional.of(votingRound));
        when(songRepository.findById(200L)).thenReturn(Optional.of(nonCandidateSong));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> votingService.castVote(10L, 50L, 200L, voter));

        assertEquals(400, ex.getStatusCode().value());
        verify(voteRepository, never()).save(any());
    }


    // Check user has already voted

    @Test
    void castVote_alreadyVoted_throwsConflict() {
        when(votingRoundRepository.findById(50L)).thenReturn(Optional.of(votingRound));
        when(songRepository.findById(100L)).thenReturn(Optional.of(candidateSong));
        when(voteRepository.existsByVotingRoundAndVoter(votingRound, voter)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> votingService.castVote(10L, 50L, 100L, voter));

        assertEquals(409, ex.getStatusCode().value());
        verify(voteRepository, never()).save(any());
    }


    @Test
    void getVotingRound_validInput_returnsRound() {
        when(votingRoundRepository.findById(50L)).thenReturn(Optional.of(votingRound));

        VotingRound result = votingService.getVotingRound(10L, 50L);

        assertNotNull(result);
        assertEquals(50L, result.getId());
        assertEquals(10L, result.getSession().getId());
    }

    @Test
    void getVotingRound_roundNotFound_throwsNotFound() {
        when(votingRoundRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> votingService.getVotingRound(10L, 99L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void getVotingRound_wrongSession_throwsNotFound() {
        when(votingRoundRepository.findById(50L)).thenReturn(Optional.of(votingRound));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> votingService.getVotingRound(99L, 50L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void getVoteCounts_returnsCorrectMap() {
        VoteRepository.SongVoteCount count1 = mock(VoteRepository.SongVoteCount.class);
        when(count1.getSongId()).thenReturn(100L);
        when(count1.getCount()).thenReturn(5L);
        VoteRepository.SongVoteCount count2 = mock(VoteRepository.SongVoteCount.class);
        when(count2.getSongId()).thenReturn(200L);
        when(count2.getCount()).thenReturn(2L);
        when(voteRepository.countVotesPerSong(votingRound)).thenReturn(List.of(count1, count2));

        Map<Long, Long> result = votingService.getVoteCounts(votingRound);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(5L, result.get(100L));
        assertEquals(2L, result.get(200L));
    }

    @Test
    void getVoteCounts_noVotes_returnsEmptyMap() {
        when(voteRepository.countVotesPerSong(votingRound)).thenReturn(List.of());

        Map<Long, Long> result = votingService.getVoteCounts(votingRound);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

//    @Test
//    void createVotingRound_validInput_success() {
//        session.setPlaylist(new ArrayList<>(List.of(candidateSong, nonCandidateSong)));
//
//        when(sessionService.getSessionById(10L)).thenReturn(session);
//        when(votingRoundRepository.existsBySessionAndStatus(session, VotingStatus.OPEN)).thenReturn(false);
//        when(votingRoundRepository.save(any(VotingRound.class))).thenReturn(votingRound);
//        when(voteRepository.countVotesPerSong(any())).thenReturn(List.of());
//
//        assertDoesNotThrow(() -> votingService.createVotingRound(10L));
//
//        verify(votingRoundRepository, times(1)).save(any(VotingRound.class));
//        verify(votingWebSocketPublisher, times(1)).broadcastVotingRound(eq(10L), any());
//    }

//    @Test
//    void createVotingRound_playlistTooSmall_skipsRound() {
//        session.setPlaylist(new ArrayList<>(List.of(candidateSong)));
//        when(sessionService.getSessionById(10L)).thenReturn(session);
//        when(votingRoundRepository.existsBySessionAndStatus(session, VotingStatus.OPEN)).thenReturn(false);
//
//        assertDoesNotThrow(() -> votingService.createVotingRound(10L));
//
//        // Skip voting round and play song if length of playlist is one
//        verify(songService, times(1)).nextSong(10L);
//        verify(votingRoundRepository, never()).save(any());
//        verify(votingWebSocketPublisher, never()).broadcastVotingRound(anyLong(), any());
//    }

//    @Test
//    void createVotingRound_alreadyOpen_throwsConflict() {
//        when(sessionService.getSessionById(10L)).thenReturn(session);
//        when(votingRoundRepository.existsBySessionAndStatus(session, VotingStatus.OPEN)).thenReturn(true);
//
//        ResponseStatusException exc = assertThrows(ResponseStatusException.class,
//                () -> votingService.createVotingRound(10L));
//
//        assertEquals(409, exc.getStatusCode().value());
//        verify(votingRoundRepository, never()).save(any());
//    }
}