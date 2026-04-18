package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JoinSessionDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class SessionsController implements SessionsApi {

    private final SessionService sessionService;
    private final UserService userService;
    private final HttpServletRequest request;

    @Autowired
    public SessionsController(SessionService sessionService,
                               UserService userService,
                               HttpServletRequest request) {
        this.sessionService = sessionService;
        this.userService = userService;
        this.request = request;
    }


    @Override
    public ResponseEntity<SessionGetDTO> sessionsPost(SessionPostDTO sessionPostDTO) {
        String token = request.getHeader("token");
        User admin = userService.getUserByToken(token);

        Session created = sessionService.createSession(
            sessionPostDTO.getName(),
            sessionPostDTO.getDescription(),
            admin
        );

        return ResponseEntity
            .status(201)
            .body(DTOMapper.INSTANCE.convertEntityToSessionGetDTO(created));
    }


    @Override
    public ResponseEntity<SessionGetDTO> sessionsSessionIdGet(Long sessionId) {
        Session session = sessionService.getSessionById(sessionId);
        return ResponseEntity.ok(DTOMapper.INSTANCE.convertEntityToSessionGetDTO(session));
    }

    @Override
    public ResponseEntity<SessionGetDTO> sessionsPinGamePinGet(String gamePin) {
        Session session = sessionService.getSessionByPin(gamePin);
        return ResponseEntity.ok(DTOMapper.INSTANCE.convertEntityToSessionGetDTO(session));
    }


    @Override
    public ResponseEntity<SessionGetDTO> sessionsSessionIdPut(Long sessionId,
                                                               SessionPutDTO sessionPutDTO) {
        String token = request.getHeader("token");
        User requester = userService.getUserByToken(token);

        Session updated = sessionService.updateSessionStatus(
            sessionId,
            sessionPutDTO.getStatus(),   // SessionStatus enum — generated from YAML
            requester.getId()
        );

        return ResponseEntity.ok(DTOMapper.INSTANCE.convertEntityToSessionGetDTO(updated));
    }


    /**
     * POST /sessions/{sessionId}/participants
     *
     * Joins the authenticated user to the session.
     * Re-joining is idempotent: calling this a second time has no effect.
     *
     * @param sessionId  target session (path variable)
     * @param joinSessionDTO  request body containing gamePin
     * @return 200 + full SessionGetDTO (including updated participants list)
     */
    @Override
    public ResponseEntity<SessionGetDTO> sessionsSessionIdParticipantsPost(
            Long sessionId,
            JoinSessionDTO joinSessionDTO) {

        String token = request.getHeader("token");
        User requester = userService.getUserByToken(token);

        Session updated = sessionService.joinSession(
            sessionId,
            joinSessionDTO.getGamePin(),
            requester.getId()
        );

        return ResponseEntity.ok(DTOMapper.INSTANCE.convertEntityToSessionGetDTO(updated));
    }


    /**
     * GET /sessions/{sessionId}/participants
     *
     * Returns the current list of participants in the session.
     *
     * @param sessionId  target session (path variable)
     * @return 200 + List<UserGetDTO>
     */
    @Override
    public ResponseEntity<List<UserGetDTO>> sessionsSessionIdParticipantsGet(Long sessionId) {
        List<UserGetDTO> dtos = sessionService.getParticipants(sessionId)
            .stream()
            .map(DTOMapper.INSTANCE::convertEntityToUserGetDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * DELETE /sessions/{sessionId}/participants/{userId}
     *
     * Removes the user from the active participant list (soft-leave).
     * The session itself is unaffected — all data is preserved.
     *
     * @param sessionId  target session (path variable)
     * @param userId     the user leaving (path variable)
     * @return 204 No Content
     */
    @Override
    public ResponseEntity<Void> sessionsSessionIdParticipantsUserIdDelete(Long sessionId,
                                                                           Long userId) {
        sessionService.leaveSession(sessionId, userId);
        return ResponseEntity.noContent().build();
    }

    // All other SessionsApi endpoints remain NOT_IMPLEMENTED until you override them here
}
