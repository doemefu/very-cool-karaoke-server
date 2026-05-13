package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.VotingRound;
import ch.uzh.ifi.hase.soprafs26.rest.dto.VotePostDTO;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.service.VotingService;
import ch.uzh.ifi.hase.soprafs26.rest.dto.VotingRoundGetDTO;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(VotingController.class)
class VotingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VotingService votingService;

    @MockitoBean
    private UserService userService;

    private User voter;

    @BeforeEach
    void setUp() {
        voter = new User();
        voter.setId(1L);
        voter.setUsername("singer1");
        voter.setToken("valid-token");
    }


    // POST - success

    @Test
    void castVote_validRequest_returns201() throws Exception {
        VotePostDTO body = new VotePostDTO();
        body.setSongId(100L);

        given(userService.getUserByToken("valid-token")).willReturn(voter);
        doNothing().when(votingService).castVote(10L, 50L, 100L, voter);

        mockMvc.perform(post("/sessions/10/votingRounds/50/votes")
                        .header("token", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(body)))
                .andExpect(status().isCreated());

        verify(votingService).castVote(10L, 50L, 100L, voter);
    }


    // POST — missing token

    @Test
    void castVote_missingToken_returns401() throws Exception {
        VotePostDTO body = new VotePostDTO();
        body.setSongId(100L);

        given(userService.getUserByToken(any()))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authentication token"));

        mockMvc.perform(post("/sessions/10/votingRounds/50/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(body)))
                .andExpect(status().isUnauthorized());

        verify(votingService, never()).castVote(any(), any(), any(), any());
    }


    // POST — invalid token

    @Test
    void castVote_invalidToken_returns401() throws Exception {
        VotePostDTO body = new VotePostDTO();
        body.setSongId(100L);

        given(userService.getUserByToken("bad-token"))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token"));

        mockMvc.perform(post("/sessions/10/votingRounds/50/votes")
                        .header("token", "bad-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(body)))
                .andExpect(status().isUnauthorized());

        verify(votingService, never()).castVote(any(), any(), any(), any());
    }


    // POST — voting round closed (410)

    @Test
    void castVote_roundClosed_returns410() throws Exception {
        VotePostDTO body = new VotePostDTO();
        body.setSongId(100L);

        given(userService.getUserByToken("valid-token")).willReturn(voter);
        doThrow(new ResponseStatusException(HttpStatus.GONE, "Voting round is CLOSED"))
                .when(votingService).castVote(10L, 50L, 100L, voter);

        mockMvc.perform(post("/sessions/10/votingRounds/50/votes")
                        .header("token", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(body)))
                .andExpect(status().isGone());
    }


    // POST — not a participant (403)

    @Test
    void castVote_notParticipant_returns403() throws Exception {
        VotePostDTO body = new VotePostDTO();
        body.setSongId(100L);

        given(userService.getUserByToken("valid-token")).willReturn(voter);
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a participant"))
                .when(votingService).castVote(10L, 50L, 100L, voter);

        mockMvc.perform(post("/sessions/10/votingRounds/50/votes")
                        .header("token", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(body)))
                .andExpect(status().isForbidden());
    }


    // POST — song not a candidate (400)

    @Test
    void castVote_songNotCandidate_returns400() throws Exception {
        VotePostDTO body = new VotePostDTO();
        body.setSongId(100L);

        given(userService.getUserByToken("valid-token")).willReturn(voter);
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Song is not a candidate"))
                .when(votingService).castVote(10L, 50L, 100L, voter);

        mockMvc.perform(post("/sessions/10/votingRounds/50/votes")
                        .header("token", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(body)))
                .andExpect(status().isBadRequest());
    }


    // POST — already voted (409)

    @Test
    void castVote_alreadyVoted_returns409() throws Exception {
        VotePostDTO body = new VotePostDTO();
        body.setSongId(100L);

        given(userService.getUserByToken("valid-token")).willReturn(voter);
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Already voted"))
                .when(votingService).castVote(10L, 50L, 100L, voter);

        mockMvc.perform(post("/sessions/10/votingRounds/50/votes")
                        .header("token", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(body)))
                .andExpect(status().isConflict());
    }

    // GET - success

    @Test
    void getVotingRound_validRequest_returns200() throws Exception {
        VotingRound mockRound = new VotingRound();
        mockRound.setId(50L);
        mockRound.setCandidates(new ArrayList<>());

        Map<Long, Long> mockCounts = new HashMap<>();
        mockCounts.put(100L, 5L);

        given(votingService.getVotingRound(10L, 50L)).willReturn(mockRound);
        given(votingService.getVoteCounts(mockRound)).willReturn(mockCounts);

        mockMvc.perform(get("/sessions/10/votingRounds/50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(votingService).getVotingRound(10L, 50L);
        verify(votingService).getVoteCounts(mockRound);
    }


    // GET — voting round not found (404)

    @Test
    void getVotingRound_roundNotFound_returns404() throws Exception {
        given(votingService.getVotingRound(10L, 99L))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Voting round not found"));

        mockMvc.perform(get("/sessions/10/votingRounds/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(votingService).getVotingRound(10L, 99L);
        verify(votingService, never()).getVoteCounts(any());
    }


    // GET — round does not belong to session (404)

    @Test
    void getVotingRound_wrongSession_returns404() throws Exception {
        given(votingService.getVotingRound(99L, 50L))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Voting round not in this session"));

        mockMvc.perform(get("/sessions/99/votingRounds/50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(votingService).getVotingRound(99L, 50L);
        verify(votingService, never()).getVoteCounts(any());
    }

    @Test
    void getVotingRounds_noRounds_returns200WithEmptyArray() throws Exception {
        given(votingService.getRoundsForSession(10L)).willReturn(List.of());

        mockMvc.perform(get("/sessions/10/votingRounds")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(votingService).getRoundsForSession(10L);
    }

    @Test
    void getVotingRounds_multipleRounds_returns200WithList() throws Exception {
        VotingRoundGetDTO dto1 = new VotingRoundGetDTO();
        dto1.setId(40L);
        VotingRoundGetDTO dto2 = new VotingRoundGetDTO();
        dto2.setId(50L);

        given(votingService.getRoundsForSession(10L)).willReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/sessions/10/votingRounds")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(40))
                .andExpect(jsonPath("$[1].id").value(50));

        verify(votingService).getRoundsForSession(10L);
    }

    @Test
    void getVotingRounds_sessionNotFound_returns404() throws Exception {
        given(votingService.getRoundsForSession(999L))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        mockMvc.perform(get("/sessions/999/votingRounds")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(votingService).getRoundsForSession(999L);
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