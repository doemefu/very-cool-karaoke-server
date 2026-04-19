package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.rest.dto.SongGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SongPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SongSearchResultDTO;
import ch.uzh.ifi.hase.soprafs26.service.SongService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
public class SongsController implements SongsApi {

    private final SongService songService;

    SongsController(SongService songService) {
        this.songService = songService;
    }

    // GET /songs/search?query= — Search songs via Spotify Web API (S11)
    // Returns 200 + list of SongSearchResultDTO, 400 if query is blank
    @Override
    public ResponseEntity<List<SongSearchResultDTO>> songsSearchGet(String query) {
        if (query.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query must not be blank");
        }
        return ResponseEntity.ok(songService.search(query));
    }

    // GET /sessions/{sessionId}/songs — Get the ordered song queue
    // Returns 200 + list of SongGetDTO
    @Override
    public ResponseEntity<List<SongGetDTO>> sessionsSessionIdSongsGet(Long sessionId) {
        // TODO: delegate to songService.getQueue(sessionId)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // POST /sessions/{sessionId}/songs — Add a song to the queue (S6, S10)
    // Returns 201 + SongGetDTO, 404 if session not found, 422 if lyrics unavailable
    @Override
    public ResponseEntity<SongGetDTO> sessionsSessionIdSongsPost(Long sessionId, SongPostDTO songPostDTO) {
        // TODO: delegate to songService.addToQueue(sessionId, songPostDTO)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // GET /sessions/{sessionId}/songs/current — Get the currently playing song with lyrics (S8)
    // Returns 200 + SongGetDTO, 204 if nothing is playing, 404 if session not found
    @Override
    public ResponseEntity<SongGetDTO> sessionsSessionIdSongsCurrentGet(Long sessionId) {
        // TODO: delegate to songService.getCurrentSong(sessionId)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // POST /sessions/{sessionId}/songs/skip — Skip current song, admin only (S7)
    // Also triggers WebSocket broadcasts for currentSong and votingRound topics
    // Returns 200 + next SongGetDTO, 403 if not admin, 404 if session not found
    @Override
    public ResponseEntity<SongGetDTO> sessionsSessionIdSongsSkipPost(Long sessionId) {
        // TODO: verify caller is admin, delegate to songService.skipCurrent(sessionId)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // DELETE /sessions/{sessionId}/songs/{songId} — Remove a song from the queue, admin only (S7)
    // Returns 204 on success, 403 if not admin, 404 if session or song not found
    @Override
    public ResponseEntity<Void> sessionsSessionIdSongsSongIdDelete(Long sessionId, Long songId) {
        // TODO: verify caller is admin, delegate to songService.removeFromQueue(sessionId, songId)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // PUT /sessions/{sessionId}/songs/{songId}/played — Mark a song as played, admin only
    // Returns 200 + updated SongGetDTO, 403 if not admin, 404 if session or song not found
    @Override
    public ResponseEntity<SongGetDTO> sessionsSessionIdSongsSongIdPlayedPut(Long sessionId, Long songId) {
        // TODO: verify caller is admin, delegate to songService.markAsPlayed(sessionId, songId)
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
