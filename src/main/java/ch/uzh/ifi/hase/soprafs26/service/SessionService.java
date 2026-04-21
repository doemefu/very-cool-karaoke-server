package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.SessionStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.SessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SongGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.websocket.SongWebSocketPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@Transactional
public class SessionService {

    private static final String USER_NOT_FOUND = "User not found";
    private static final String SESSION_NOT_FOUND = "Session not found";

    private final Logger log = LoggerFactory.getLogger(SessionService.class);

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final SongWebSocketPublisher songWebSocketPublisher;

    @Autowired
    public SessionService(SessionRepository sessionRepository,
                          UserRepository userRepository,
                          SongWebSocketPublisher songWebSocketPublisher) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.songWebSocketPublisher = songWebSocketPublisher;
    }

    /*
    create a new session. The calling user becomes the admin and is
    also added to the participant list automatically.
     */
    public Session createSession(String name, String description, User admin) {
        Session session = new Session();
        session.setName(name);
        session.setDescription(description);
        session.setAdmin(admin);
        // Admin is automatically a participant of their own session
        session.addParticipant(admin);

        String gamePin;
        do {
            gamePin = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        } while (sessionRepository.existsByGamePin(gamePin));

        session.setGamePin(gamePin);

        Session saved = sessionRepository.save(session);
        log.debug("Created session {} with admin {}", saved.getId(), admin.getId());
        return saved;
    }

    public Session getSessionById(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, SESSION_NOT_FOUND));
    }

    public Session getSessionByPin(String gamePin) {
        Session session = sessionRepository.findByGamePin(gamePin);
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, SESSION_NOT_FOUND);
        }
        return session;
    }


    public Session updateSessionStatus(Long sessionId, SessionStatus newStatus, Long requesterId) {
        Session session = getSessionById(sessionId);

        if (!session.getAdmin().getId().equals(requesterId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only the admin can change session status");
        }

        SessionStatus current = session.getStatus();
        boolean validTransition =
                (current == SessionStatus.CREATED && newStatus == SessionStatus.ACTIVE) ||
                        (current == SessionStatus.ACTIVE && newStatus == SessionStatus.PAUSED) ||
                        (current == SessionStatus.PAUSED && newStatus == SessionStatus.ACTIVE) ||
                        (current == SessionStatus.ACTIVE && newStatus == SessionStatus.ENDED) ||
                        (current == SessionStatus.PAUSED && newStatus == SessionStatus.ENDED);

        if (!validTransition) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid status transition: " + current + " → " + newStatus);
        }

        session.setStatus(newStatus);
        return sessionRepository.save(session);
    }


    /**
     * Add a user to a session's participant list via game PIN.
     * <p>
     * Idempotency: if the user is already a participant (re-join case),
     * {@code Session.addParticipant()} is a Set no-op — no error is
     * thrown and no duplicate row is inserted into session_participants.
     * <p>
     * Corresponds to: POST /sessions/{sessionId}/participants
     *
     * @param sessionId target session
     * @param gamePin   the PIN that must match {@code session.gamePin}
     * @param userId    the user who wants to join
     * @return the updated Session (with participants collection populated)
     * @throws ResponseStatusException 404 if session or user not found
     * @throws ResponseStatusException 400 if the PIN is wrong
     */
    public Session joinSession(Long sessionId, String gamePin, Long userId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, SESSION_NOT_FOUND));

        if (!session.getGamePin().equals(gamePin)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid game pin");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, USER_NOT_FOUND));

        boolean isNewParticipant = !session.getParticipants().contains(user);

        // Set.add() is a no-op when user is already present → idempotent
        session.addParticipant(user);

        if (isNewParticipant) {
            session.addToPendingInitialSong(user);
        }

        Session saved = sessionRepository.save(session);

        Map<Long, Long> emptyVotes = new HashMap<>();
        List<SongGetDTO> queue = saved.getPlaylist().stream()
                .map(s -> DTOMapper.INSTANCE.toSongGetDTO(s, emptyVotes))
                .toList();
        songWebSocketPublisher.broadcastQueue(sessionId, queue);

        saved.getPlaylist().stream()
                .filter(s -> !Boolean.TRUE.equals(s.getPerformed()))
                .findFirst()
                .ifPresent(s -> {
                    songWebSocketPublisher.broadcastCurrentSong(sessionId,
                            DTOMapper.INSTANCE.toSongGetDTO(s, emptyVotes));
                    songWebSocketPublisher.broadcastLyrics(sessionId, s.getLyrics());
                });

        log.debug("User {} joined session {}", userId, sessionId);
        return saved;
    }


    /**
     * Remove a user from a session's participant list (soft-leave).
     * <p>
     * The session record and all its data are preserved; only the join-
     * table row is deleted. This satisfies the S5 acceptance criterion
     * "The session itself is not affected by users leaving or rejoining."
     * <p>
     * Corresponds to: PUT /sessions/{sessionId}/participants/{userId}
     *
     * @param sessionId session the user wants to leave
     * @param userId    the user leaving
     * @throws ResponseStatusException 404 if session or user not found
     */
    public void leaveSession(Long sessionId, Long userId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Session or participant not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Session or participant not found"));

        session.removeParticipant(user);
        sessionRepository.save(session);
        log.debug("User {} left session {}", userId, sessionId);
    }


    /**
     * Return all current participants of a session.
     * <p>
     * Corresponds to: GET /sessions/{sessionId}/participants
     *
     * @param sessionId the session to query
     * @return unmodifiable set of participant Users
     * @throws ResponseStatusException 404 if session not found
     */
    @Transactional(readOnly = true)
    public Set<User> getParticipants(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, SESSION_NOT_FOUND));
        return session.getParticipants();
    }


    @Transactional(readOnly = true)
    public List<Session> getSessionsByUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, USER_NOT_FOUND));

        List<Session> created = sessionRepository.findByAdminId(userId);
        List<Session> joined = sessionRepository.findByParticipantId(userId);
        Set<Session> all = new LinkedHashSet<>(created);
        all.addAll(joined);

        return new ArrayList<>(all);
    }


    @Transactional(readOnly = true)
    public boolean requiresSongSelection(Long sessionId, Long userId) {
        Session session = getSessionById(sessionId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, USER_NOT_FOUND));
        return session.isPendingInitialSong(user);
    }

    public void markInitialSongAdded(Long sessionId, Long userId) {
        Session session = getSessionById(sessionId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, USER_NOT_FOUND));
        session.removeFromPendingInitialSong(user);
        sessionRepository.save(session);
        log.debug("User {} fulfilled initial song requirement for session {}",
                userId, sessionId);
    }
}