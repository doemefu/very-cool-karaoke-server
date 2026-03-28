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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        given(sessionService.createSession(eq("Friday Night Karaoke"), eq("Fun session"), eq(admin)))
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