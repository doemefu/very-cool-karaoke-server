package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPutDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UsersController implements UsersApi {

    // TODO: inject UserService here
    // UsersController(UserService userService) { this.userService = userService; }

    // PUT /users/{userId} — Update user profile, e.g. change password (S2)
    // Returns 204 on success, 400 if invalid input, 403 if editing another user, 404 if not found
    @Override
    public ResponseEntity<Void> usersUserIdPut(Long userId, UserPutDTO userPutDTO) {
        // TODO: verify caller is the same user (auth), delegate to userService.updateUser(userId, userPutDTO)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // GET /users/{userId}/sessions — Get session history for a user (S13)
    // Returns 200 + list of SessionGetDTO, 404 if user not found
    @Override
    public ResponseEntity<List<SessionGetDTO>> usersUserIdSessionsGet(Long userId) {
        // TODO: delegate to sessionService.getSessionsByUser(userId)
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
