package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.constant.SessionStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionPostDTO;
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

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JoinSessionDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionPutDTO;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SessionsController.class)
class SessionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SessionService sessionService;

    @MockitoBean
    private UserService userService;

    private User admin;
    private Session session;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setId(1L);
        admin.setUsername("adminUser");
        admin.setToken("valid-token");

        session = new Session();
        session.setId(10L);
        session.setName("Friday Night Karaoke");
        session.setDescription("Fun session");
        session.setGamePin("a1b2c3");
        session.setStatus(SessionStatus.CREATED);
        session.setAdmin(admin);
        session.addParticipant(admin);
    }

    @Test
    void sessionsPost_validToken_returns201WithBody() throws Exception {
        SessionPostDTO body = new SessionPostDTO();
        body.setName("Friday Night Karaoke");
        body.setDescription("Fun session");

        given(userService.getUserByToken("valid-token")).willReturn(admin);
        given(sessionService.createSession("Friday Night Karaoke", "Fun session", admin))
                .willReturn(session);

        mockMvc.perform(post("/sessions")
                        .header("token", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(body)))
                .andExpect(status().isCreated())
                // top-level SessionGetDTO fields
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.name", is("Friday Night Karaoke")))
                .andExpect(jsonPath("$.description", is("Fun session")))
                .andExpect(jsonPath("$.gamePin", is("a1b2c3")))
                .andExpect(jsonPath("$.status", is("CREATED")))
                // admin sub-object (spec: UserGetDTO with id + username)
                .andExpect(jsonPath("$.admin.id", is(1)))
                .andExpect(jsonPath("$.admin.username", is("adminUser")))
                // participants array — admin is auto-added on creation
                .andExpect(jsonPath("$.participants", hasSize(1)))
                .andExpect(jsonPath("$.participants[0].id", is(1)))
                .andExpect(jsonPath("$.participants[0].username", is("adminUser")));
    }

    @Test
    void sessionsPost_missingToken_returns401() throws Exception {
        SessionPostDTO body = new SessionPostDTO();
        body.setName("Friday Night Karaoke");

        given(userService.getUserByToken(any()))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authentication token"));

        mockMvc.perform(post("/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sessionsPost_invalidToken_returns401() throws Exception {
        SessionPostDTO body = new SessionPostDTO();
        body.setName("Friday Night Karaoke");

        given(userService.getUserByToken("bad-token"))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token"));

        mockMvc.perform(post("/sessions")
                        .header("token", "bad-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sessionsSessionIdGet_validRequest_returns200() throws Exception {
        given(sessionService.getSessionById(10L)).willReturn(session);

        mockMvc.perform(get("/sessions/10")
                        .header("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.name", is("Friday Night Karaoke")));
    }

    @Test
    void sessionsSessionIdGet_notFound_returns404() throws Exception {
        given(sessionService.getSessionById(99L))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        mockMvc.perform(get("/sessions/99")
                        .header("token", "valid-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void sessionsPinGamePinGet_validRequest_returns200() throws Exception {
        given(sessionService.getSessionByPin("a1b2c3")).willReturn(session);

        mockMvc.perform(get("/sessions/pin/a1b2c3")
                        .header("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gamePin", is("a1b2c3")));
    }

    @Test
    void sessionsSessionIdPut_adminUpdatesStatus_returns200() throws Exception {
        SessionPutDTO putBody = new SessionPutDTO();
        putBody.setStatus(SessionStatus.CREATED);

        given(userService.getUserByToken("valid-token")).willReturn(admin);
        given(sessionService.updateSessionStatus(eq(10L), any(SessionStatus.class), eq(1L)))
                .willReturn(session);

        mockMvc.perform(put("/sessions/10")
                        .header("token", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(putBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)));
    }

    @Test
    void sessionsSessionIdParticipantsPost_validRequest_returns200() throws Exception {
        JoinSessionDTO joinBody = new JoinSessionDTO("a1b2c3");

        given(userService.getUserByToken("valid-token")).willReturn(admin);
        given(sessionService.joinSession(10L, "a1b2c3", 1L)).willReturn(session);
        given(sessionService.requiresSongSelection(10L, 1L)).willReturn(false);

        mockMvc.perform(post("/sessions/10/participants")
                        .header("token", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(joinBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)));
    }

    @Test
    void sessionsSessionIdParticipantsGet_validRequest_returns200() throws Exception {
        given(sessionService.getParticipants(10L)).willReturn(Set.of(admin));

        mockMvc.perform(get("/sessions/10/participants")
                        .header("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is("adminUser")));
    }

    @Test
    void sessionsSessionIdParticipantsUserIdDelete_validRequest_returns204() throws Exception {
        mockMvc.perform(delete("/sessions/10/participants/1")
                        .header("token", "valid-token"))
                .andExpect(status().isNoContent());
    }

    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        }
        catch (JacksonException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e));
        }
    }
}