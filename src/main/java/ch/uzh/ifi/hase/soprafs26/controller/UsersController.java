package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UsersController implements UsersApi {

    private final UserService userService;
    private final HttpServletRequest request;

    @Autowired
    public UsersController(UserService userService, HttpServletRequest request) {
        this.userService = userService;
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
        // TODO: delegate to sessionService.getSessionsByUser(userId)
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
