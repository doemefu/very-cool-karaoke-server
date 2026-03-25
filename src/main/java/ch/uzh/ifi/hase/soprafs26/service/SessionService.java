package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.SessionStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.repository.SessionRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionPostDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Random;

@Service
@Transactional
public class SessionService {

    private final Logger log = LoggerFactory.getLogger(SessionService.class);

    private final SessionRepository sessionRepository;
    private final Random random = new Random();

    public SessionService(@Qualifier("sessionRepository") SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public SessionGetDTO createSession(SessionPostDTO dto) {
        Session session = new Session();
        session.setName(dto.getName());
        session.setDescription(dto.getDescription());
        session.setStatus(SessionStatus.CREATED);
        session.setCreatedAt(OffsetDateTime.now());
        session.setGamePin(generateUniqueGamePin());
        // TODO: set admin from authenticated user once auth is implemented

        session = sessionRepository.save(session);
        sessionRepository.flush();

        log.debug("Created session: {}", session.getId());
        return toGetDTO(session);
    }

    private String generateUniqueGamePin() {
        String pin;
        do {
            pin = String.format("%06d", random.nextInt(1_000_000));
        } while (sessionRepository.findByGamePin(pin) != null);
        return pin;
    }

    private SessionGetDTO toGetDTO(Session session) {
        SessionGetDTO dto = new SessionGetDTO();
        dto.setId(session.getId());
        dto.setName(session.getName());
        dto.setDescription(session.getDescription());
        dto.setGamePin(session.getGamePin());
        dto.setStatus(session.getStatus());
        dto.setCreatedAt(session.getCreatedAt());
        // admin and participants mapped here once auth is wired up
        return dto;
    }
}
