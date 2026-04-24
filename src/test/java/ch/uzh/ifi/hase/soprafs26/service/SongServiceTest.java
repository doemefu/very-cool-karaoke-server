package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.Song;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.SongRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SongGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SongPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SongSearchResultDTO;
import ch.uzh.ifi.hase.soprafs26.websocket.SongWebSocketPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.isNull;

class SongServiceTest {

    @Mock
    private SpotifyService spotifyService;

    @Mock
    private LyricsService lyricsService;

    @Mock
    private SongRepository songRepository;

    @Mock
    private SessionService sessionService;

    @Mock
    private SongWebSocketPublisher songWebSocketPublisher;

    @InjectMocks
    private SongService songService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void search_lyricsAvailable_setsLyricsAvailableTrue() {
        SpotifyTrack track = new SpotifyTrack("id1", "Dancing Queen", "ABBA", "ABBA Gold", "http://img/art.jpg", 230000);
        when(spotifyService.search("ABBA")).thenReturn(List.of(track));
        when(lyricsService.fetchLyrics("ABBA", "Dancing Queen")).thenReturn("Here I go again...");

        List<SongSearchResultDTO> results = songService.search("ABBA");

        assertEquals(1, results.size());
        SongSearchResultDTO dto = results.get(0);
        assertEquals("id1", dto.getSpotifyId());
        assertEquals("Dancing Queen", dto.getTitle());
        assertEquals("ABBA", dto.getArtist());
        assertEquals("http://img/art.jpg", dto.getAlbumArt());
        assertEquals(230000, dto.getDurationMs());
        assertTrue(dto.getLyricsAvailable());
    }

    @Test
    void search_lyricsNotFound_setsLyricsAvailableFalse() {
        SpotifyTrack track = new SpotifyTrack("id2", "Mystery Song", "Unknown", null, "http://img/art.jpg", 180000);
        when(spotifyService.search("Mystery")).thenReturn(List.of(track));
        when(lyricsService.fetchLyrics("Unknown", "Mystery Song")).thenReturn(null);

        List<SongSearchResultDTO> results = songService.search("Mystery");

        assertEquals(1, results.size());
        assertFalse(results.get(0).getLyricsAvailable());
    }

    @Test
    void search_lyricsAreCachedBySpotifyId() {
        SpotifyTrack track = new SpotifyTrack("id3", "Waterloo", "ABBA", "Waterloo", "http://img/art.jpg", 170000);
        when(spotifyService.search("Waterloo")).thenReturn(List.of(track));
        when(lyricsService.fetchLyrics("ABBA", "Waterloo")).thenReturn("My my, at Waterloo Napoleon did surrender");

        songService.search("Waterloo");

        assertEquals("My my, at Waterloo Napoleon did surrender", songService.getCachedLyrics("id3"));
    }

    @Test
    void search_noLyrics_cachedAsNull() {
        SpotifyTrack track = new SpotifyTrack("id4", "Unknown Song", "Unknown", null, "http://img/art.jpg", 200000);
        when(spotifyService.search("Unknown")).thenReturn(List.of(track));
        when(lyricsService.fetchLyrics("Unknown", "Unknown Song")).thenReturn(null);

        songService.search("Unknown");

        assertNull(songService.getCachedLyrics("id4"));
    }

    @Test
    void addToQueue_persistsSongAndBroadcastsQueue() {
        Session session = new Session();
        SongPostDTO dto = new SongPostDTO("track123", "Dancing Queen", "ABBA", 230000);

        // Pre-populate lyrics cache
        songService.cacheLyrics("track123", "Here I go again...");

        when(sessionService.getSessionById(1L)).thenReturn(session);
        when(songRepository.save(any(Song.class))).thenAnswer(inv -> {
            Song s = inv.getArgument(0);
            s.setId(10L);
            return s;
        });

        SongGetDTO result = songService.addToQueue(1L, dto);

        verify(songRepository).save(any(Song.class));
        verify(songWebSocketPublisher).broadcastQueue(eq(1L), anyList());
        assertEquals("Dancing Queen", result.getTitle());
        assertEquals("ABBA", result.getArtist());
        assertEquals("Here I go again...", result.getLyrics());
    }

    @Test
    void addToQueue_noLyricsCache_persistsSongWithNullLyrics() {
        Session session = new Session();
        SongPostDTO dto = new SongPostDTO("uncached", "Unknown Song", "Unknown", 180000);

        when(sessionService.getSessionById(2L)).thenReturn(session);
        when(songRepository.save(any(Song.class))).thenAnswer(inv -> inv.getArgument(0));

        SongGetDTO result = songService.addToQueue(2L, dto);

        assertNull(result.getLyrics());
        verify(songWebSocketPublisher).broadcastQueue(eq(2L), anyList());
    }

    @Test
    void getQueue_excludesPerformedSongs() {
        Session session = new Session();

        Song performed = new Song();
        performed.setId(1L);
        performed.setSpotifyId("s1");
        performed.setTitle("Done");
        performed.setArtist("A");
        performed.setDurationMs(100);
        performed.setPerformed(true);
        performed.setSession(session);

        Song unperformed = new Song();
        unperformed.setId(2L);
        unperformed.setSpotifyId("s2");
        unperformed.setTitle("Next");
        unperformed.setArtist("B");
        unperformed.setDurationMs(200);
        unperformed.setPerformed(false);
        unperformed.setSession(session);

        session.getPlaylist().add(performed);
        session.getPlaylist().add(unperformed);

        when(sessionService.getSessionById(99L)).thenReturn(session);

        List<SongGetDTO> queue = songService.getQueue(99L);

        assertEquals(1, queue.size());
        assertEquals("Next", queue.get(0).getTitle());
    }

    @Test
    void nextSong_advancesToNextUnperformedSong() {
        Session session = new Session();

        Song first = new Song();
        first.setId(1L);
        first.setSpotifyId("s1");
        first.setTitle("First");
        first.setArtist("Artist");
        first.setDurationMs(180000);
        first.setPerformed(false);
        first.setSession(session);

        Song second = new Song();
        second.setId(2L);
        second.setSpotifyId("s2");
        second.setTitle("Second");
        second.setArtist("Artist");
        second.setDurationMs(200000);
        second.setPerformed(false);
        second.setSession(session);

        session.getPlaylist().add(first);
        session.getPlaylist().add(second);

        when(sessionService.getSessionById(1L)).thenReturn(session);
        when(songRepository.save(any(Song.class))).thenAnswer(inv -> inv.getArgument(0));

        songService.nextSong(1L);

        assertTrue(first.getPerformed(), "first song should be marked performed");
        assertFalse(second.getPerformed(), "second song should still be unperformed");

        verify(songWebSocketPublisher).broadcastCurrentSong(eq(1L),
                argThat(dto -> dto != null && "Second".equals(dto.getTitle())));
        verify(songWebSocketPublisher).broadcastQueue(eq(1L),
                argThat(queue -> queue.size() == 1 && "Second".equals(queue.get(0).getTitle())));
    }

    @Test
    void nextSong_lastSong_broadcastsNullCurrentSong() {
        Session session = new Session();

        Song only = new Song();
        only.setId(3L);
        only.setSpotifyId("s3");
        only.setTitle("Last");
        only.setArtist("Artist");
        only.setDurationMs(200000);
        only.setPerformed(false);
        only.setSession(session);

        session.getPlaylist().add(only);

        when(sessionService.getSessionById(2L)).thenReturn(session);
        when(songRepository.save(any(Song.class))).thenAnswer(inv -> inv.getArgument(0));

        songService.nextSong(2L);

        assertTrue(only.getPerformed());
        verify(songWebSocketPublisher).broadcastCurrentSong(eq(2L), isNull());
        verify(songWebSocketPublisher).broadcastQueue(eq(2L), argThat(List::isEmpty));
    }

    @Test
    void nextSong_emptyPlaylist_broadcastsNullWithoutError() {
        Session session = new Session(); // empty playlist
        when(sessionService.getSessionById(3L)).thenReturn(session);

        songService.nextSong(3L);

        verify(songRepository, never()).save(any());
        verify(songWebSocketPublisher).broadcastCurrentSong(eq(3L), isNull());
        verify(songWebSocketPublisher).broadcastQueue(eq(3L), argThat(List::isEmpty));
    }

    @Test
    void deleteSongFromQueue_success_removesQueuedSongAndBroadcasts() {
        User admin = new User();
        admin.setId(1L);

        Session session = new Session();
        session.setId(1L);
        session.setAdmin(admin);

        Song currentSong = new Song();
        currentSong.setId(10L);
        currentSong.setTitle("Current Song");
        currentSong.setPerformed(false);
        currentSong.setSession(session);

        Song songToDelete = new Song();
        songToDelete.setId(20L);
        songToDelete.setTitle("Queued Song");
        songToDelete.setPerformed(false);
        songToDelete.setSession(session);

        session.getPlaylist().add(currentSong);
        session.getPlaylist().add(songToDelete);

        when(sessionService.getSessionById(1L)).thenReturn(session);
        when(songRepository.findById(20L)).thenReturn(Optional.of(songToDelete));

        songService.deleteSongFromQueue(1L, 20L, "test-token");

        assertEquals(1, session.getPlaylist().size());
        assertFalse(session.getPlaylist().contains(songToDelete));
        verify(songRepository).delete(songToDelete);

        // Broadcast updated queue
        verify(songWebSocketPublisher).broadcastQueue(eq(1L), anyList());
        // Should not broadcast new song to not disturb the one still playing
        verify(songWebSocketPublisher, never()).broadcastCurrentSong(anyLong(), any());
    }

    @Test
    void deleteSongFromQueue_success_deletesCurrentSongAndBroadcastsNew() {
        User admin = new User();
        admin.setId(1L);

        Session session = new Session();
        session.setId(1L);
        session.setAdmin(admin);

        Song currentSong = new Song();
        currentSong.setId(10L);
        currentSong.setTitle("Current Song");
        currentSong.setPerformed(false);
        currentSong.setSession(session);

        Song nextSong = new Song();
        nextSong.setId(20L);
        nextSong.setTitle("Next Song");
        nextSong.setPerformed(false);
        nextSong.setSession(session);

        session.getPlaylist().add(currentSong);
        session.getPlaylist().add(nextSong);

        when(sessionService.getSessionById(1L)).thenReturn(session);
        when(songRepository.findById(10L)).thenReturn(Optional.of(currentSong));
        songService.deleteSongFromQueue(1L, 10L, "test-token");

        assertEquals(1, session.getPlaylist().size());
        verify(songRepository).delete(currentSong);

        // Broadcast the updated queue and the new current song
        verify(songWebSocketPublisher).broadcastQueue(eq(1L), anyList());
        verify(songWebSocketPublisher).broadcastCurrentSong(eq(1L),
                argThat(dto -> dto != null && "Next Song".equals(dto.getTitle())));
    }

    @Test
    void deleteSongFromQueue_throws404_whenSessionNotFound() {
        when(sessionService.getSessionById(99L)).thenThrow(
                new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND)
        );

        ResponseStatusException exception = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> songService.deleteSongFromQueue(99L, 10L, "test-token")
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void deleteSongFromQueue_throws404_whenSongNotInPlaylist() {
        User admin = new User();
        admin.setId(1L);

        Session session = new Session();
        session.setAdmin(admin);

        Song someSong = new Song();
        someSong.setId(10L);
        session.getPlaylist().add(someSong);

        when(sessionService.getSessionById(1L)).thenReturn(session);

        // Delete song which is not in playlist
        ResponseStatusException exception = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> songService.deleteSongFromQueue(1L, 99L, "test-token")
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
