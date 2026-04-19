package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.rest.dto.SongSearchResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SongServiceTest {

    @Mock
    private SpotifyService spotifyService;

    @Mock
    private LyricsService lyricsService;

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
}
