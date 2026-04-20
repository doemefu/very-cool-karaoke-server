package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.constant.ReactionType;
import ch.uzh.ifi.hase.soprafs26.constant.SessionStatus;
import ch.uzh.ifi.hase.soprafs26.rest.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApiDefaultMethodsTest {

    private Optional<NativeWebRequest> mockRequest() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Accept", "application/json");
        return Optional.of(new ServletWebRequest(req, new MockHttpServletResponse()));
    }

    @Test
    void authApi_defaultMethods_returnNotImplemented() {
        AuthApi api = new AuthApi() {
            @Override
            public Optional<NativeWebRequest> getRequest() {
                return mockRequest();
            }
        };

        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("user");
        userPostDTO.setPassword("pass");

        assertEquals(HttpStatus.NOT_IMPLEMENTED, api.authLoginPost(userPostDTO).getStatusCode());
        assertEquals(HttpStatus.NOT_IMPLEMENTED, api.authLogoutPost().getStatusCode());
        assertEquals(HttpStatus.NOT_IMPLEMENTED, api.usersPost(userPostDTO).getStatusCode());
    }

    @Test
    void reactionsApi_defaultMethods_returnNotImplemented() {
        ReactionsApi api = new ReactionsApi() {
            @Override
            public Optional<NativeWebRequest> getRequest() {
                return mockRequest();
            }
        };

        ReactionPostDTO reactionPostDTO = new ReactionPostDTO(ReactionType.HEART);
        assertEquals(HttpStatus.NOT_IMPLEMENTED,
                api.sessionsSessionIdReactionsPost(1L, reactionPostDTO).getStatusCode());
    }

    @Test
    void sessionsApi_defaultMethods_returnNotImplemented() {
        SessionsApi api = new SessionsApi() {
            @Override
            public Optional<NativeWebRequest> getRequest() {
                return mockRequest();
            }
        };

        SessionPostDTO sessionPostDTO = new SessionPostDTO();
        sessionPostDTO.setName("Test");

        SessionPutDTO sessionPutDTO = new SessionPutDTO(SessionStatus.CREATED);
        JoinSessionDTO joinSessionDTO = new JoinSessionDTO("pin123");

        assertEquals(HttpStatus.NOT_IMPLEMENTED, api.sessionsPost(sessionPostDTO).getStatusCode());
        assertEquals(HttpStatus.NOT_IMPLEMENTED, api.sessionsSessionIdGet(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_IMPLEMENTED, api.sessionsPinGamePinGet("pin").getStatusCode());
        assertEquals(HttpStatus.NOT_IMPLEMENTED, api.sessionsSessionIdPut(1L, sessionPutDTO).getStatusCode());
        assertEquals(HttpStatus.NOT_IMPLEMENTED, api.sessionsSessionIdParticipantsGet(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_IMPLEMENTED, api.sessionsSessionIdParticipantsPost(1L, joinSessionDTO).getStatusCode());
        assertEquals(HttpStatus.NOT_IMPLEMENTED, api.sessionsSessionIdParticipantsUserIdDelete(1L, 1L).getStatusCode());
        assertEquals(HttpStatus.NOT_IMPLEMENTED, api.sessionsSessionIdReviewGet(1L).getStatusCode());
    }

    @Test
    void songsApi_defaultMethods_returnNotImplemented() {
        SongsApi api = new SongsApi() {
            @Override
            public Optional<NativeWebRequest> getRequest() {
                return mockRequest();
            }
        };

        SongPostDTO songPostDTO = new SongPostDTO();

        assertEquals(HttpStatus.NOT_IMPLEMENTED, api.sessionsSessionIdSongsGet(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_IMPLEMENTED, api.sessionsSessionIdSongsCurrentGet(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_IMPLEMENTED, api.sessionsSessionIdSongsPost(1L, songPostDTO).getStatusCode());
        assertEquals(HttpStatus.NOT_IMPLEMENTED, api.sessionsSessionIdSongsSkipPost(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_IMPLEMENTED, api.sessionsSessionIdSongsSongIdDelete(1L, 1L).getStatusCode());
        assertEquals(HttpStatus.NOT_IMPLEMENTED, api.sessionsSessionIdSongsSongIdPlayedPut(1L, 1L).getStatusCode());
        assertEquals(HttpStatus.NOT_IMPLEMENTED, api.songsSearchGet("query").getStatusCode());
    }

    @Test
    void usersApi_defaultMethods_returnNotImplemented() {
        UsersApi api = new UsersApi() {
            @Override
            public Optional<NativeWebRequest> getRequest() {
                return mockRequest();
            }
        };

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setOldPassword("old");
        userPutDTO.setNewPassword("new");

        assertEquals(HttpStatus.NOT_IMPLEMENTED, api.usersUserIdPut(1L, userPutDTO).getStatusCode());
        assertEquals(HttpStatus.NOT_IMPLEMENTED, api.usersUserIdSessionsGet(1L).getStatusCode());
    }

    @Test
    void votingApi_defaultMethods_returnNotImplemented() {
        VotingApi api = new VotingApi() {
            @Override
            public Optional<NativeWebRequest> getRequest() {
                return mockRequest();
            }
        };

        VotePostDTO votePostDTO = new VotePostDTO();
        votePostDTO.setSongId(1L);

        assertEquals(HttpStatus.NOT_IMPLEMENTED, api.sessionsSessionIdVotingRoundsGet(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_IMPLEMENTED, api.sessionsSessionIdVotingRoundsRoundIdGet(1L, 1L).getStatusCode());
        assertEquals(HttpStatus.NOT_IMPLEMENTED,
                api.sessionsSessionIdVotingRoundsRoundIdVotesPost(1L, 1L, votePostDTO).getStatusCode());
    }

    @Test
    void apiUtil_setExampleResponse_writesToResponse() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        ServletWebRequest webRequest = new ServletWebRequest(req, res);

        ApiUtil.setExampleResponse(webRequest, "application/json", "{\"test\":\"value\"}");

        assertNotNull(res.getHeader("Content-Type"));
        assertEquals("UTF-8", res.getCharacterEncoding());
    }
}
