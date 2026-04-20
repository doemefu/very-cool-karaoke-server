package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.constant.SessionStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs26.service.SessionService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsersController.class)
class UsersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private SessionService sessionService;

    private User user;
    private Session session;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setToken("valid-token");

        session = new Session();
        session.setId(10L);
        session.setName("Test Session");
        session.setGamePin("abc123");
        session.setStatus(SessionStatus.CREATED);
        session.setAdmin(user);
        session.addParticipant(user);
    }

    @Test
    void usersUserIdPut_validRequest_returns204() throws Exception {
        UserPutDTO body = new UserPutDTO();
        body.setOldPassword("oldPass");
        body.setNewPassword("newPass");

        mockMvc.perform(put("/users/1")
                        .header("token", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(body)))
                .andExpect(status().isNoContent());
    }

    @Test
    void usersUserIdPut_wrongUser_returns403() throws Exception {
        UserPutDTO body = new UserPutDTO();
        body.setOldPassword("oldPass");
        body.setNewPassword("newPass");

        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot edit another user"))
                .when(userService).changePassword(any(), any(), any(), any());

        mockMvc.perform(put("/users/2")
                        .header("token", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    void usersUserIdPut_userNotFound_returns404() throws Exception {
        UserPutDTO body = new UserPutDTO();
        body.setOldPassword("oldPass");
        body.setNewPassword("newPass");

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(userService).changePassword(any(), any(), any(), any());

        mockMvc.perform(put("/users/99")
                        .header("token", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void usersUserIdSessionsGet_ownSessions_returns200() throws Exception {
        given(userService.getUserByToken("valid-token")).willReturn(user);
        given(sessionService.getSessionsByUser(1L)).willReturn(List.of(session));

        mockMvc.perform(get("/users/1/sessions")
                        .header("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void usersUserIdSessionsGet_otherUser_returns403() throws Exception {
        User other = new User();
        other.setId(2L);
        other.setToken("valid-token");
        given(userService.getUserByToken("valid-token")).willReturn(other);

        mockMvc.perform(get("/users/1/sessions")
                        .header("token", "valid-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void usersUserIdSessionsGet_invalidToken_returns401() throws Exception {
        given(userService.getUserByToken("bad-token"))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));

        mockMvc.perform(get("/users/1/sessions")
                        .header("token", "bad-token"))
                .andExpect(status().isUnauthorized());
    }

    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JacksonException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e));
        }
    }
}
