package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.SessionStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.SessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.websocket.SongWebSocketPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SongWebSocketPublisher songWebSocketPublisher;

    @InjectMocks
    private SessionService sessionService;

    private Session session;
    private User admin;
    private User participant;

    @BeforeEach
    void setUp() {
        // Build admin user — only set fields that actually exist in User.java
        admin = new User();
        admin.setId(1L);
        admin.setUsername("adminUser");
        admin.setToken("admin-token-uuid");

        // Build participant user
        participant = new User();
        participant.setId(2L);
        participant.setUsername("partyUser");
        participant.setToken("party-token-uuid");

        // Build session
        session = new Session();
        session.setId(10L);
        session.setName("Friday Night Karaoke");
        session.setGamePin("482910");
        session.setAdmin(admin);
    }


    // createSession

    @Test
    void createSession_validInput_returnsPersistedSession() {
        when(sessionRepository.existsByGamePin(any())).thenReturn(false);
        when(sessionRepository.save(any(Session.class))).thenAnswer(i -> {
            Session s = i.getArgument(0);
            s.setId(10L);
            return s;
        });

        Session result = sessionService.createSession("Friday Night Karaoke", "Fun session", admin);

        assertNotNull(result);
        assertEquals("Friday Night Karaoke", result.getName());
        assertEquals("Fun session", result.getDescription());
        assertEquals(admin, result.getAdmin());
        assertNotNull(result.getGamePin());
        assertEquals(6, result.getGamePin().length());
        verify(sessionRepository, times(1)).save(any(Session.class));
    }

    @Test
    void createSession_adminIsAddedAsParticipant() {
        when(sessionRepository.existsByGamePin(any())).thenReturn(false);
        when(sessionRepository.save(any(Session.class))).thenAnswer(i -> i.getArgument(0));

        Session result = sessionService.createSession("Test Session", null, admin);

        assertTrue(result.getParticipants().contains(admin),
                "Admin must be automatically added as a participant");
    }

    @Test
    void createSession_defaultStatusIsCreated() {
        when(sessionRepository.existsByGamePin(any())).thenReturn(false);
        when(sessionRepository.save(any(Session.class))).thenAnswer(i -> i.getArgument(0));

        Session result = sessionService.createSession("Test Session", null, admin);

        assertEquals(SessionStatus.CREATED, result.getStatus());
    }

    @Test
    void createSession_pinCollision_retriesUntilUnique() {
        // First generated pin collides, second is unique
        when(sessionRepository.existsByGamePin(any()))
                .thenReturn(true)
                .thenReturn(false);
        when(sessionRepository.save(any(Session.class))).thenAnswer(i -> i.getArgument(0));

        Session result = sessionService.createSession("Test Session", null, admin);

        assertNotNull(result.getGamePin());
        // existsByGamePin must have been called at least twice
        verify(sessionRepository, atLeast(2)).existsByGamePin(any());
        verify(sessionRepository, times(1)).save(any(Session.class));
    }


    // joinSession

    @Test
    void joinSession_validPin_addsParticipantToSet() {
        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(userRepository.findById(2L)).thenReturn(Optional.of(participant));
        when(sessionRepository.save(any(Session.class))).thenAnswer(i -> i.getArgument(0));

        Session result = sessionService.joinSession(10L, "482910", 2L);

        assertTrue(result.getParticipants().contains(participant),
                "Participant must be in the set after a successful join");
        verify(sessionRepository, times(1)).save(session);
    }

    @Test
    void joinSession_rejoin_isIdempotentNoDuplicate() {
        // Pre-condition: participant is already in the session (simulates a re-join)
        session.addParticipant(participant);
        assertEquals(1, session.getParticipants().size(),
                "Pre-condition: exactly 1 participant before re-join");

        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(userRepository.findById(2L)).thenReturn(Optional.of(participant));
        when(sessionRepository.save(any(Session.class))).thenAnswer(i -> i.getArgument(0));

        // Must NOT throw — re-joining must be a silent no-op
        assertDoesNotThrow(
                () -> sessionService.joinSession(10L, "482910", 2L),
                "Re-joining must not throw any exception");

        // The set must still contain exactly one entry for this user
        long count = session.getParticipants().stream()
                .filter(u -> u.getId().equals(2L))
                .count();
        assertEquals(1, count,
                "Re-joining must not insert a duplicate entry in session_participants");
    }

    @Test
    void joinSession_wrongPin_throwsBadRequest() {
        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> sessionService.joinSession(10L, "000000", 2L));

        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().toLowerCase().contains("invalid game pin"));
        // Must not proceed to user lookup or save when PIN is wrong
        verify(userRepository, never()).findById(any());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void joinSession_sessionNotFound_throwsNotFound() {
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> sessionService.joinSession(99L, "482910", 2L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void joinSession_userNotFound_throwsNotFound() {
        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> sessionService.joinSession(10L, "482910", 99L));

        assertEquals(404, ex.getStatusCode().value());
        verify(sessionRepository, never()).save(any());
    }


    // leaveSession

    @Test
    void leaveSession_existingParticipant_removesFromSet() {
        session.addParticipant(participant);
        assertTrue(session.getParticipants().contains(participant),
                "Pre-condition: participant must be in session before leaving");

        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(userRepository.findById(2L)).thenReturn(Optional.of(participant));
        when(sessionRepository.save(any(Session.class))).thenAnswer(i -> i.getArgument(0));

        sessionService.leaveSession(10L, 2L);

        assertFalse(session.getParticipants().contains(participant),
                "Participant must not be in the set after leaving");
        verify(sessionRepository, times(1)).save(session);
    }

    @Test
    void leaveSession_sessionDataCompletelyUnchanged() {
        // Session name, status, and admin must survive a participant leave
        session.addParticipant(participant);
        String originalName = session.getName();
        SessionStatus originalStatus = session.getStatus();
        User originalAdmin = session.getAdmin();

        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(userRepository.findById(2L)).thenReturn(Optional.of(participant));
        when(sessionRepository.save(any(Session.class))).thenAnswer(i -> i.getArgument(0));

        sessionService.leaveSession(10L, 2L);

        assertEquals(originalName, session.getName(),
                "Session name must not change when a user leaves");
        assertEquals(originalStatus, session.getStatus(),
                "Session status must not change when a user leaves");
        assertEquals(originalAdmin, session.getAdmin(),
                "Session admin must not change when a user leaves");
    }

    @Test
    void leaveSession_sessionNotFound_throwsNotFound() {
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> sessionService.leaveSession(99L, 2L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void leaveSession_userNotFound_throwsNotFound() {
        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> sessionService.leaveSession(10L, 99L));

        assertEquals(404, ex.getStatusCode().value());
        verify(sessionRepository, never()).save(any());
    }


    // getParticipants

    @Test
    void getParticipants_returnsAllCurrentParticipants() {
        session.addParticipant(admin);
        session.addParticipant(participant);

        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));

        Set<User> result = sessionService.getParticipants(10L);

        assertEquals(2, result.size());
        assertTrue(result.contains(admin));
        assertTrue(result.contains(participant));
    }

    @Test
    void getParticipants_noParticipants_returnsEmptySet() {
        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));

        Set<User> result = sessionService.getParticipants(10L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getParticipants_sessionNotFound_throwsNotFound() {
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> sessionService.getParticipants(99L));

        assertEquals(404, ex.getStatusCode().value());
    }
}