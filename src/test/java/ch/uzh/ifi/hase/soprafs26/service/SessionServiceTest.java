package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.SessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SessionService sessionService;

    private Session session;
    private User admin;
    private User participant;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setId(1L);
        admin.setUsername("adminUser");
        admin.setStatus(UserStatus.ONLINE);

        participant = new User();
        participant.setId(2L);
        participant.setUsername("participantUser");
        participant.setStatus(UserStatus.ONLINE);

        session = new Session();
        session.setId(10L);
        session.setName("Friday Night Karaoke");
        session.setGamePin("482910");
        session.setAdmin(admin);
    }

    // joinSession

    @Test
    void joinSession_validPin_addsParticipant() {
        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(userRepository.findById(2L)).thenReturn(Optional.of(participant));
        when(sessionRepository.save(any(Session.class))).thenAnswer(i -> i.getArgument(0));

        Session result = sessionService.joinSession(10L, "482910", 2L);

        assertTrue(result.getParticipants().contains(participant),
            "Participant should be in the set after joining");
        verify(sessionRepository).save(session);
    }

    @Test
    void joinSession_idempotent_noErrorOnRejoin() {
        // Pre-load: participant is already in the session
        session.addParticipant(participant);

        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(userRepository.findById(2L)).thenReturn(Optional.of(participant));
        when(sessionRepository.save(any(Session.class))).thenAnswer(i -> i.getArgument(0));

        // Should NOT throw — rejoining must be silent
        assertDoesNotThrow(() -> sessionService.joinSession(10L, "482910", 2L));

        Session result = sessionRepository.save(session);
        // Still exactly one entry for this user
        long count = result.getParticipants().stream()
            .filter(u -> u.getId().equals(2L))
            .count();
        assertEquals(1, count,
            "Re-joining must not create a duplicate participant entry");
    }

    @Test
    void joinSession_wrongPin_throwsBadRequest() {
        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> sessionService.joinSession(10L, "000000", 2L));

        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().toLowerCase().contains("invalid game pin"));
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
    }

    // leaveSession

    @Test
    void leaveSession_existingParticipant_removesFromSet() {
        session.addParticipant(participant);

        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(userRepository.findById(2L)).thenReturn(Optional.of(participant));
        when(sessionRepository.save(any(Session.class))).thenAnswer(i -> i.getArgument(0));

        sessionService.leaveSession(10L, 2L);

        assertFalse(session.getParticipants().contains(participant),
            "Participant should no longer be in the set after leaving");
        verify(sessionRepository).save(session);
    }

    @Test
    void leaveSession_sessionNotFound_throwsNotFound() {
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> sessionService.leaveSession(99L, 2L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void leaveSession_sessionUnaffectedByLeave() {
        // Verify the session itself (name, status, admin) is unchanged
        session.addParticipant(participant);
        String originalName = session.getName();

        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(userRepository.findById(2L)).thenReturn(Optional.of(participant));
        when(sessionRepository.save(any(Session.class))).thenAnswer(i -> i.getArgument(0));

        sessionService.leaveSession(10L, 2L);

        assertEquals(originalName, session.getName(),
            "Session name must not change when a user leaves");
        assertEquals(admin, session.getAdmin(),
            "Session admin must not change when a user leaves");
    }

    // getParticipants 

    @Test
    void getParticipants_returnsCurrentSet() {
        session.addParticipant(admin);
        session.addParticipant(participant);

        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));

        var result = sessionService.getParticipants(10L);

        assertEquals(2, result.size());
        assertTrue(result.contains(admin));
        assertTrue(result.contains(participant));
    }

    @Test
    void getParticipants_sessionNotFound_throwsNotFound() {
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> sessionService.getParticipants(99L));

        assertEquals(404, ex.getStatusCode().value());
    }
}