package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UsersController implements UsersApi {

    private final UserService userService;
    private final HttpServletRequest request;
    private final SessionService sessionService;

    @Autowired
    public UsersController(UserService userService, SessionService sessionService, HttpServletRequest request) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.request = request;
    }

    // PUT /users/{userId} — Update user profile, e.g. change password (S2)
    // Returns 204 on success, 400 if invalid input, 403 if editing another user, 404 if not found
    @Override
    public ResponseEntity<Void> usersUserIdPut(Long userId, UserPutDTO userPutDTO) {
        String token = request.getHeader("token");
        userService.changePassword(
                userId,
                token,
                userPutDTO.getOldPassword(),
                userPutDTO.getNewPassword()
        );
        return ResponseEntity.noContent().build();
    }

    // GET /users/{userId}/sessions — Get session history for a user (S13)
    // Returns 200 + list of SessionGetDTO, 404 if user not found
    @Override
    public ResponseEntity<List<SessionGetDTO>> usersUserIdSessionsGet(Long userId) {
        String token = request.getHeader("token");
        User requester = userService.getUserByToken(token);

        if (!requester.getId().equals(userId)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "Can only view your own session history");
        }
        
        List<SessionGetDTO> dtos = sessionService.getSessionsByUser(userId)
            .stream()
            .map(DTOMapper.INSTANCE::convertEntityToSessionGetDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
