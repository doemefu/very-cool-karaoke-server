package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SessionsController implements SessionsApi {

    private final SessionService sessionService;

    SessionsController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public ResponseEntity<SessionGetDTO> sessionsPost(SessionPostDTO sessionPostDTO) {
        SessionGetDTO response = sessionService.createSession(sessionPostDTO);
        return ResponseEntity.status(201).body(response);
    }

    // All other SessionsApi endpoints remain NOT_IMPLEMENTED until you override them here
}
