package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserTokenDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController implements AuthApi {

    // TODO: inject AuthService (or UserService) here
    // AuthController(AuthService authService) { this.authService = authService; }

    private final UserService userService;
    private final HttpServletRequest request;

    AuthController(UserService userService, HttpServletRequest request) {
        this.userService = userService;
        this.request = request;
    }

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
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
        User loggedInUser = userService.loginUser(userInput);
        return ResponseEntity.status(HttpStatus.OK)
                .body(DTOMapper.INSTANCE.convertEntityToUserTokenDTO(loggedInUser));
    }

    // POST /auth/logout — Invalidates token, sets user status to OFFLINE
    // Returns 204 on success, 401 if unauthorized
    @Override
    public ResponseEntity<Void> authLogoutPost() {
        String token = request.getHeader("token");
        userService.logoutUser(token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
