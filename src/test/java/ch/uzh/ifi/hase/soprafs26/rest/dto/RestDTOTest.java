package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.ReactionType;
import ch.uzh.ifi.hase.soprafs26.constant.SessionStatus;
import ch.uzh.ifi.hase.soprafs26.constant.VotingStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RestDTOTest {

    // ── JoinSessionDTO ──────────────────────────────────────────────────────

    @Test
    void joinSessionDTO_getterSetter() {
        JoinSessionDTO dto = new JoinSessionDTO();
        dto.setGamePin("abc123");
        assertEquals("abc123", dto.getGamePin());
    }

    @Test
    void joinSessionDTO_requiredConstructor() {
        JoinSessionDTO dto = new JoinSessionDTO("xyz");
        assertEquals("xyz", dto.getGamePin());
    }

    @Test
    void joinSessionDTO_fluentBuilder() {
        JoinSessionDTO dto = new JoinSessionDTO().gamePin("pin99");
        assertEquals("pin99", dto.getGamePin());
    }

    @Test
    void joinSessionDTO_equalsHashCodeToString() {
        JoinSessionDTO a = new JoinSessionDTO("pin1");
        JoinSessionDTO b = new JoinSessionDTO("pin1");
        JoinSessionDTO c = new JoinSessionDTO("other");

        assertEquals(a, a);
        assertEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a, null);
        assertNotEquals(a, new Object());
        assertEquals(a.hashCode(), b.hashCode());

        assertNotNull(a.toString());
        assertNotNull(new JoinSessionDTO().toString());
    }

    // ── ReactionGetDTO ──────────────────────────────────────────────────────

    @Test
    void reactionGetDTO_getterSetter() {
        ReactionGetDTO dto = new ReactionGetDTO();
        dto.setId(1L);
        dto.setType(ReactionType.HEART);
        dto.setSentAt(Instant.now());
        dto.setSender(new UserGetDTO());
        dto.setDuringPerformance(new SongGetDTO());

        assertEquals(1L, dto.getId());
        assertEquals(ReactionType.HEART, dto.getType());
        assertNotNull(dto.getSentAt());
        assertNotNull(dto.getSender());
        assertNotNull(dto.getDuringPerformance());
    }

    // ── ReactionPostDTO ─────────────────────────────────────────────────────

    @Test
    void reactionPostDTO_getterSetter() {
        ReactionPostDTO dto = new ReactionPostDTO();
        dto.setType(ReactionType.FIRE);
        assertEquals(ReactionType.FIRE, dto.getType());
    }

    @Test
    void reactionPostDTO_requiredConstructor() {
        ReactionPostDTO dto = new ReactionPostDTO(ReactionType.HEART);
        assertEquals(ReactionType.HEART, dto.getType());
    }

    @Test
    void reactionPostDTO_fluentBuilder() {
        ReactionPostDTO dto = new ReactionPostDTO().type(ReactionType.FIRE);
        assertEquals(ReactionType.FIRE, dto.getType());
    }

    @Test
    void reactionPostDTO_equalsHashCodeToString() {
        ReactionPostDTO a = new ReactionPostDTO(ReactionType.HEART);
        ReactionPostDTO b = new ReactionPostDTO(ReactionType.HEART);
        ReactionPostDTO c = new ReactionPostDTO(ReactionType.FIRE);

        assertEquals(a, a);
        assertEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a, null);
        assertNotEquals(a, new Object());
        assertEquals(a.hashCode(), b.hashCode());

        assertNotNull(a.toString());
        assertNotNull(new ReactionPostDTO().toString());
    }

    // ── SessionPutDTO ───────────────────────────────────────────────────────

    @Test
    void sessionPutDTO_getterSetter() {
        SessionPutDTO dto = new SessionPutDTO();
        dto.setStatus(SessionStatus.CREATED);
        assertEquals(SessionStatus.CREATED, dto.getStatus());
    }

    @Test
    void sessionPutDTO_requiredConstructor() {
        SessionPutDTO dto = new SessionPutDTO(SessionStatus.CREATED);
        assertEquals(SessionStatus.CREATED, dto.getStatus());
    }

    @Test
    void sessionPutDTO_fluentBuilder() {
        SessionPutDTO dto = new SessionPutDTO().status(SessionStatus.CREATED);
        assertEquals(SessionStatus.CREATED, dto.getStatus());
    }

    @Test
    void sessionPutDTO_equalsHashCodeToString() {
        SessionPutDTO a = new SessionPutDTO(SessionStatus.CREATED);
        SessionPutDTO b = new SessionPutDTO(SessionStatus.CREATED);

        assertEquals(a, a);
        assertEquals(a, b);
        assertNotEquals(a, null);
        assertNotEquals(a, new Object());
        assertEquals(a.hashCode(), b.hashCode());

        assertNotNull(a.toString());
        assertNotNull(new SessionPutDTO().toString());
    }

    // ── UserPutDTO ──────────────────────────────────────────────────────────

    @Test
    void userPutDTO_getterSetter() {
        UserPutDTO dto = new UserPutDTO();
        dto.setOldPassword("old");
        dto.setNewPassword("new");
        assertEquals("old", dto.getOldPassword());
        assertEquals("new", dto.getNewPassword());
    }

    @Test
    void userPutDTO_fluentBuilder() {
        UserPutDTO dto = new UserPutDTO()
                .oldPassword("old")
                .newPassword("new");
        assertEquals("old", dto.getOldPassword());
        assertEquals("new", dto.getNewPassword());
    }

    @Test
    void userPutDTO_equalsHashCodeToString() {
        UserPutDTO a = new UserPutDTO().oldPassword("old").newPassword("new");
        UserPutDTO b = new UserPutDTO().oldPassword("old").newPassword("new");
        UserPutDTO c = new UserPutDTO().oldPassword("other").newPassword("pass");

        assertEquals(a, a);
        assertEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a, null);
        assertNotEquals(a, new Object());
        assertEquals(a.hashCode(), b.hashCode());

        assertNotNull(a.toString());
        assertNotNull(new UserPutDTO().toString());
    }

    // ── SessionGetDTO ───────────────────────────────────────────────────────

    @Test
    void sessionGetDTO_allSettersAndGetters() {
        SessionGetDTO dto = new SessionGetDTO();
        dto.setId(1L);
        dto.setName("Session");
        dto.setDescription("Desc");
        dto.setGamePin("pin1");
        dto.setStatus(SessionStatus.CREATED);
        dto.setCreatedAt(OffsetDateTime.now());
        dto.setAdmin(new UserGetDTO());
        dto.setParticipants(List.of(new UserGetDTO()));
        dto.setRequiresSongSelection(true);

        assertEquals(1L, dto.getId());
        assertEquals("Session", dto.getName());
        assertEquals("Desc", dto.getDescription());
        assertEquals("pin1", dto.getGamePin());
        assertEquals(SessionStatus.CREATED, dto.getStatus());
        assertNotNull(dto.getCreatedAt());
        assertNotNull(dto.getAdmin());
        assertEquals(1, dto.getParticipants().size());
        assertTrue(dto.getRequiresSongSelection());
    }

    @Test
    void sessionGetDTO_equalsHashCodeToString() {
        SessionGetDTO a = new SessionGetDTO();
        a.setId(1L);
        a.setName("S");
        SessionGetDTO b = new SessionGetDTO();
        b.setId(1L);
        b.setName("S");

        assertEquals(a, a);
        assertEquals(a, b);
        assertNotEquals(a, null);
        assertNotEquals(a, new Object());
        assertEquals(a.hashCode(), b.hashCode());

        assertNotNull(a.toString());
        assertNotNull(new SessionGetDTO().toString());
    }

    // ── SessionPostDTO ──────────────────────────────────────────────────────

    @Test
    void sessionPostDTO_equalsHashCodeToString() {
        SessionPostDTO a = new SessionPostDTO("MySession");
        a.setDescription("desc");
        SessionPostDTO b = new SessionPostDTO("MySession");
        b.setDescription("desc");

        assertEquals(a, a);
        assertEquals(a, b);
        assertNotEquals(a, null);
        assertNotEquals(a, new Object());
        assertEquals(a.hashCode(), b.hashCode());

        assertNotNull(a.toString());
        assertNotNull(new SessionPostDTO().toString());
    }

    // ── SongGetDTO ──────────────────────────────────────────────────────────

    @Test
    void songGetDTO_allSettersAndGetters() {
        SongGetDTO dto = new SongGetDTO();
        dto.setId(1L);
        dto.setSpotifyId("spotify1");
        dto.setTitle("Song");
        dto.setArtist("Artist");
        dto.setAlbumArt("http://art");
        dto.setDurationMs(3000);
        dto.setLyrics("La la");
        dto.setCurrentVoteCount(5);
        dto.setPerformed(true);
        dto.setAddedBy(new UserGetDTO());

        assertEquals(1L, dto.getId());
        assertEquals("spotify1", dto.getSpotifyId());
        assertEquals("Song", dto.getTitle());
        assertEquals("Artist", dto.getArtist());
        assertEquals("http://art", dto.getAlbumArt());
        assertEquals(3000, dto.getDurationMs());
        assertEquals("La la", dto.getLyrics());
        assertEquals(5, dto.getCurrentVoteCount());
        assertTrue(dto.getPerformed());
        assertNotNull(dto.getAddedBy());
    }

    @Test
    void songGetDTO_equalsHashCodeToString() {
        SongGetDTO a = new SongGetDTO();
        a.setId(1L);
        a.setTitle("T");
        SongGetDTO b = new SongGetDTO();
        b.setId(1L);
        b.setTitle("T");

        assertEquals(a, a);
        assertEquals(a, b);
        assertNotEquals(a, null);
        assertNotEquals(a, new Object());
        assertEquals(a.hashCode(), b.hashCode());

        assertNotNull(a.toString());
        assertNotNull(new SongGetDTO().toString());
    }

    // ── SongPostDTO ─────────────────────────────────────────────────────────

    @Test
    void songPostDTO_allSettersAndGetters() {
        SongPostDTO dto = new SongPostDTO();
        dto.setSpotifyId("sp1");
        dto.setTitle("Title");
        dto.setArtist("Artist");
        dto.setAlbumArt("http://art");
        dto.setDurationMs(2000);

        assertEquals("sp1", dto.getSpotifyId());
        assertEquals("Title", dto.getTitle());
        assertEquals("Artist", dto.getArtist());
        assertEquals("http://art", dto.getAlbumArt());
        assertEquals(2000, dto.getDurationMs());
    }

    @Test
    void songPostDTO_equalsHashCodeToString() {
        SongPostDTO a = new SongPostDTO();
        a.setSpotifyId("sp1");
        a.setTitle("T");
        SongPostDTO b = new SongPostDTO();
        b.setSpotifyId("sp1");
        b.setTitle("T");

        assertEquals(a, a);
        assertEquals(a, b);
        assertNotEquals(a, null);
        assertNotEquals(a, new Object());
        assertEquals(a.hashCode(), b.hashCode());

        assertNotNull(a.toString());
        assertNotNull(new SongPostDTO().toString());
    }

    // ── SongSearchResultDTO ─────────────────────────────────────────────────

    @Test
    void songSearchResultDTO_allSettersAndGetters() {
        SongSearchResultDTO dto = new SongSearchResultDTO();
        dto.setSpotifyId("sp1");
        dto.setTitle("Title");
        dto.setArtist("Artist");
        dto.setAlbumArt("http://art");
        dto.setDurationMs(2000);
        dto.setLyricsAvailable(true);

        assertEquals("sp1", dto.getSpotifyId());
        assertEquals("Title", dto.getTitle());
        assertEquals("Artist", dto.getArtist());
        assertEquals("http://art", dto.getAlbumArt());
        assertEquals(2000, dto.getDurationMs());
        assertTrue(dto.getLyricsAvailable());
    }

    @Test
    void songSearchResultDTO_equalsHashCodeToString() {
        SongSearchResultDTO a = new SongSearchResultDTO();
        a.setSpotifyId("sp1");
        SongSearchResultDTO b = new SongSearchResultDTO();
        b.setSpotifyId("sp1");

        assertEquals(a, a);
        assertEquals(a, b);
        assertNotEquals(a, null);
        assertNotEquals(a, new Object());
        assertEquals(a.hashCode(), b.hashCode());

        assertNotNull(a.toString());
        assertNotNull(new SongSearchResultDTO().toString());
    }

    // ── VotePostDTO ─────────────────────────────────────────────────────────

    @Test
    void votePostDTO_equalsHashCodeToString() {
        VotePostDTO a = new VotePostDTO();
        a.setSongId(1L);
        VotePostDTO b = new VotePostDTO();
        b.setSongId(1L);
        VotePostDTO c = new VotePostDTO();
        c.setSongId(2L);

        assertEquals(a, a);
        assertEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a, null);
        assertNotEquals(a, new Object());
        assertEquals(a.hashCode(), b.hashCode());

        assertNotNull(a.toString());
        assertNotNull(new VotePostDTO().toString());
    }

    // ── VotingRoundGetDTO ───────────────────────────────────────────────────

    @Test
    void votingRoundGetDTO_allSettersAndGetters() {
        VotingRoundGetDTO dto = new VotingRoundGetDTO();
        dto.setId(1L);
        dto.setRoundNumber(2);
        dto.setStatus(VotingStatus.OPEN);
        dto.setStartedAt(OffsetDateTime.now());
        dto.setEndsAt(OffsetDateTime.now().plusMinutes(5));
        dto.setCandidates(List.of(new SongGetDTO()));

        assertEquals(1L, dto.getId());
        assertEquals(2, dto.getRoundNumber());
        assertEquals(VotingStatus.OPEN, dto.getStatus());
        assertNotNull(dto.getStartedAt());
        assertNotNull(dto.getEndsAt());
        assertEquals(1, dto.getCandidates().size());
    }

    @Test
    void votingRoundGetDTO_equalsHashCodeToString() {
        VotingRoundGetDTO a = new VotingRoundGetDTO();
        a.setId(1L);
        VotingRoundGetDTO b = new VotingRoundGetDTO();
        b.setId(1L);

        assertEquals(a, a);
        assertEquals(a, b);
        assertNotEquals(a, null);
        assertNotEquals(a, new Object());
        assertEquals(a.hashCode(), b.hashCode());

        assertNotNull(a.toString());
        assertNotNull(new VotingRoundGetDTO().toString());
    }

    // ── UserPostDTO ─────────────────────────────────────────────────────────

    @Test
    void userPostDTO_equalsHashCodeToString() {
        UserPostDTO a = new UserPostDTO("user", "pass");
        UserPostDTO b = new UserPostDTO("user", "pass");
        UserPostDTO c = new UserPostDTO("other", "pass");

        assertEquals(a, a);
        assertEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a, null);
        assertNotEquals(a, new Object());
        assertEquals(a.hashCode(), b.hashCode());

        assertNotNull(a.toString());
        assertNotNull(new UserPostDTO().toString());
    }
}
