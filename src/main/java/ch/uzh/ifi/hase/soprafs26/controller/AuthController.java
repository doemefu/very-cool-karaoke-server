package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserTokenDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController implements AuthApi {

    // TODO: inject AuthService (or UserService) here
    // AuthController(AuthService authService) { this.authService = authService; }

    private final UserService userService;
    AuthController(UserService userService) { this.userService = userService; }

    // POST /users — Register a new user (S1)
    // Returns 201 + UserTokenDTO on success, 409 if username already taken
    @Override
    public ResponseEntity<UserTokenDTO> usersPost(UserPostDTO userPostDTO) {
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
        User createdUser = userService.createUser(userInput);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DTOMapper.INSTANCE.convertEntityToUserTokenDTO(createdUser));
    }

    // POST /auth/login — Login with username and password
    // Returns 200 + UserTokenDTO on success, 401 if credentials invalid, 404 if user not found
    @Override
    public ResponseEntity<UserTokenDTO> authLoginPost(UserPostDTO userPostDTO) {
        // TODO: delegate to authService.login(userPostDTO)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // POST /auth/logout — Invalidates token, sets user status to OFFLINE
    // Returns 204 on success, 401 if unauthorized
    @Override
    public ResponseEntity<Void> authLogoutPost() {
        // TODO: extract token from Authorization header, delegate to authService.logout(token)
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
