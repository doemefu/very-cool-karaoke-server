package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.Song;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
        SpotifyTrack track = new SpotifyTrack("id1", "Dancing Queen", "ABBA", "http://img/art.jpg", 230000);
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
        SpotifyTrack track = new SpotifyTrack("id2", "Mystery Song", "Unknown", "http://img/art.jpg", 180000);
        when(spotifyService.search("Mystery")).thenReturn(List.of(track));
        when(lyricsService.fetchLyrics("Unknown", "Mystery Song")).thenReturn(null);

        List<SongSearchResultDTO> results = songService.search("Mystery");

        assertEquals(1, results.size());
        assertFalse(results.get(0).getLyricsAvailable());
    }

    @Test
    void search_lyricsAreCachedBySpotifyId() {
        SpotifyTrack track = new SpotifyTrack("id3", "Waterloo", "ABBA", "http://img/art.jpg", 170000);
        when(spotifyService.search("Waterloo")).thenReturn(List.of(track));
        when(lyricsService.fetchLyrics("ABBA", "Waterloo")).thenReturn("My my, at Waterloo Napoleon did surrender");

        songService.search("Waterloo");

        assertEquals("My my, at Waterloo Napoleon did surrender", songService.getCachedLyrics("id3"));
    }

    @Test
    void search_noLyrics_cachedAsNull() {
        SpotifyTrack track = new SpotifyTrack("id4", "Unknown Song", "Unknown", "http://img/art.jpg", 200000);
        when(spotifyService.search("Unknown")).thenReturn(List.of(track));
        when(lyricsService.fetchLyrics("Unknown", "Unknown Song")).thenReturn(null);

        songService.search("Unknown");

        assertNull(songService.getCachedLyrics("id4"));
    }

    @Test
    void addToQueue_persistsSongAndBroadcastsQueue() {
        Session session = new Session();
        SongPostDTO dto = new SongPostDTO("track123", "Dancing Queen", "ABBA");
        dto.setDurationMs(230000);

        // Pre-populate lyrics cache
        songService.getLyricsCache().put("track123", java.util.Optional.of("Here I go again..."));

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
        SongPostDTO dto = new SongPostDTO("uncached", "Unknown Song", "Unknown");
        dto.setDurationMs(180000);

        when(sessionService.getSessionById(2L)).thenReturn(session);
        when(songRepository.save(any(Song.class))).thenAnswer(inv -> inv.getArgument(0));

        SongGetDTO result = songService.addToQueue(2L, dto);

        assertNull(result.getLyrics());
        verify(songWebSocketPublisher).broadcastQueue(eq(2L), anyList());
    }
}
