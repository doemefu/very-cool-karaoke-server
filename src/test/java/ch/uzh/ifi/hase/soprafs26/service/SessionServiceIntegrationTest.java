package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.SessionStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.SessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
@Transactional
class SessionServiceIntegrationTest {

    @Qualifier("sessionRepository")
    @Autowired
    private SessionRepository sessionRepository;

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionService sessionService;

    private User admin;

    @BeforeEach
    void setup() {
        sessionRepository.deleteAll();
        userRepository.deleteAll();

        admin = new User();
        admin.setUsername("adminUser");
        admin.setPassword("password");
        admin.setToken("admin-token-uuid");
        admin = userRepository.save(admin);
    }

    @Test
    void createSession_validInputs_persistedCorrectly() {
        Session created = sessionService.createSession("Friday Night Karaoke", "Fun session", admin);

        assertNotNull(created.getId());
        assertEquals("Friday Night Karaoke", created.getName());
        assertEquals("Fun session", created.getDescription());
        assertEquals(SessionStatus.CREATED, created.getStatus());
        assertNotNull(created.getGamePin());
        assertEquals(6, created.getGamePin().length());
        assertNotNull(created.getCreatedAt());
        assertEquals(admin.getId(), created.getAdmin().getId());
    }

    @Test
    void createSession_adminIsParticipant_persistedInJoinTable() {
        Session created = sessionService.createSession("Test Session", null, admin);

        Session fetched = sessionRepository.findById(created.getId()).orElseThrow();
        assertTrue(fetched.getParticipants().stream()
                        .anyMatch(u -> u.getId().equals(admin.getId())),
                "Admin must appear in the persisted participants join table");
    }

    @Test
    void createSession_twoSessions_haveDistinctGamePins() {
        Session first = sessionService.createSession("Session One", null, admin);
        Session second = sessionService.createSession("Session Two", null, admin);

        assertNotEquals(first.getGamePin(), second.getGamePin(),
                "Each session must receive a unique game pin");
    }
}