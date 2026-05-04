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
        // All results have no lyrics → fallback triggers; return empty recommendations
        when(spotifyService.getRecommendations("id2")).thenReturn(List.of());

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
        // All results have no lyrics → fallback triggers; return empty recommendations
        when(spotifyService.getRecommendations("id4")).thenReturn(List.of());

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
    void getRecommendationsForSong_returnsTracksWithLyrics() {
        SpotifyTrack rec1 = new SpotifyTrack("rec1", "Waterloo", "ABBA", "Waterloo", "http://img/w.jpg", 170000);
        SpotifyTrack rec2 = new SpotifyTrack("rec2", "SOS", "ABBA", "ABBA Gold", "http://img/s.jpg", 200000);
        when(spotifyService.getRecommendations("seed1")).thenReturn(List.of(rec1, rec2));
        when(lyricsService.fetchLyrics("ABBA", "Waterloo")).thenReturn("My my at Waterloo...");
        when(lyricsService.fetchLyrics("ABBA", "SOS")).thenReturn("Where are those happy days...");

        List<SongSearchResultDTO> result = songService.getRecommendationsForSong("seed1");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(SongSearchResultDTO::getLyricsAvailable));
        assertEquals("rec1", result.get(0).getSpotifyId());
        assertEquals("rec2", result.get(1).getSpotifyId());
    }

    @Test
    void getRecommendationsForSong_filtersOutTracksWithNoLyrics() {
        SpotifyTrack rec1 = new SpotifyTrack("rec1", "Waterloo", "ABBA", "Waterloo", "http://img/w.jpg", 170000);
        SpotifyTrack rec2 = new SpotifyTrack("rec2", "Unknown", "Mystery", null, "http://img/u.jpg", 180000);
        when(spotifyService.getRecommendations("seed2")).thenReturn(List.of(rec1, rec2));
        when(lyricsService.fetchLyrics("ABBA", "Waterloo")).thenReturn("My my at Waterloo...");
        when(lyricsService.fetchLyrics("Mystery", "Unknown")).thenReturn(null);

        List<SongSearchResultDTO> result = songService.getRecommendationsForSong("seed2");

        assertEquals(1, result.size());
        assertEquals("rec1", result.get(0).getSpotifyId());
        assertTrue(result.get(0).getLyricsAvailable());
    }

    @Test
    void getRecommendationsForSong_noLyricsForAny_throwsNotFound() {
        SpotifyTrack rec = new SpotifyTrack("rec1", "Mystery", "Unknown", null, "http://img/u.jpg", 180000);
        when(spotifyService.getRecommendations("seed3")).thenReturn(List.of(rec));
        when(lyricsService.fetchLyrics(any(), any())).thenReturn(null);

        var ex = assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> songService.getRecommendationsForSong("seed3"));
        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getRecommendationsForSong_limitsToFiveResults() {
        List<SpotifyTrack> recs = java.util.stream.IntStream.range(0, 8)
                .mapToObj(i -> new SpotifyTrack("rec" + i, "Song" + i, "Artist", null, "http://img.jpg", 180000))
                .toList();
        when(spotifyService.getRecommendations("seedMany")).thenReturn(recs);
        when(lyricsService.fetchLyrics(any(), any())).thenReturn("lyrics here");

        List<SongSearchResultDTO> result = songService.getRecommendationsForSong("seedMany");

        assertEquals(5, result.size());
        assertTrue(result.stream().allMatch(SongSearchResultDTO::getLyricsAvailable));
    }

    @Test
    void search_allResultsHaveNoLyrics_triggersRecommendationsFallback() {
        SpotifyTrack searchTrack = new SpotifyTrack("orig1", "No Lyrics Song", "Artist", null, "http://img.jpg", 180000);
        SpotifyTrack rec1 = new SpotifyTrack("rec1", "Waterloo", "ABBA", "Waterloo", "http://img/w.jpg", 170000);
        SpotifyTrack rec2 = new SpotifyTrack("rec2", "SOS", "ABBA", "ABBA Gold", "http://img/s.jpg", 200000);
        SpotifyTrack rec3 = new SpotifyTrack("rec3", "Mamma Mia", "ABBA", "ABBA Gold", "http://img/m.jpg", 210000);

        when(spotifyService.search("no lyrics")).thenReturn(List.of(searchTrack));
        when(lyricsService.fetchLyrics("Artist", "No Lyrics Song")).thenReturn(null);
        when(spotifyService.getRecommendations("orig1")).thenReturn(List.of(rec1, rec2, rec3));
        when(lyricsService.fetchLyrics("ABBA", "Waterloo")).thenReturn("My my at Waterloo...");
        when(lyricsService.fetchLyrics("ABBA", "SOS")).thenReturn("Where are those happy days...");
        when(lyricsService.fetchLyrics("ABBA", "Mamma Mia")).thenReturn("I've been cheated...");

        List<SongSearchResultDTO> results = songService.search("no lyrics");

        assertEquals(4, results.size());
        assertEquals("orig1", results.get(0).getSpotifyId());
        assertFalse(results.get(0).getLyricsAvailable());
        assertTrue(results.subList(1, results.size()).stream().allMatch(SongSearchResultDTO::getLyricsAvailable));
    }

    @Test
    void search_allResultsHaveNoLyrics_iteratesUntilTwoLyricsFound() {
        SpotifyTrack searchTrack = new SpotifyTrack("orig1", "No Lyrics Song", "Artist", null, "http://img.jpg", 180000);
        SpotifyTrack rec1noLyrics = new SpotifyTrack("rec1", "Also No Lyrics", "Nobody", null, "http://img/n.jpg", 170000);
        SpotifyTrack rec2withLyrics = new SpotifyTrack("rec2", "Has Lyrics", "Someone", null, "http://img/h.jpg", 200000);
        SpotifyTrack rec3withLyrics = new SpotifyTrack("rec3", "Also Has Lyrics", "Other", null, "http://img/a.jpg", 210000);

        when(spotifyService.search("scarce lyrics")).thenReturn(List.of(searchTrack));
        when(lyricsService.fetchLyrics("Artist", "No Lyrics Song")).thenReturn(null);
        when(spotifyService.getRecommendations("orig1"))
                .thenReturn(List.of(rec1noLyrics, rec2withLyrics))
                .thenReturn(List.of(rec3withLyrics));
        when(lyricsService.fetchLyrics("Nobody", "Also No Lyrics")).thenReturn(null);
        when(lyricsService.fetchLyrics("Someone", "Has Lyrics")).thenReturn("lyrics1");
        when(lyricsService.fetchLyrics("Other", "Also Has Lyrics")).thenReturn("lyrics2");

        List<SongSearchResultDTO> results = songService.search("scarce lyrics");

        long withLyricsCount = results.subList(1, results.size()).stream()
                .filter(SongSearchResultDTO::getLyricsAvailable).count();
        assertTrue(withLyricsCount >= 2);
        assertEquals("orig1", results.get(0).getSpotifyId());
        assertFalse(results.get(0).getLyricsAvailable());
        verify(spotifyService, org.mockito.Mockito.times(2)).getRecommendations("orig1");
    }

    @Test
    void search_someResultsHaveLyrics_noFallbackTriggered() {
        SpotifyTrack track1 = new SpotifyTrack("id1", "Has Lyrics", "Artist", null, "http://img.jpg", 180000);
        SpotifyTrack track2 = new SpotifyTrack("id2", "No Lyrics", "Artist2", null, "http://img2.jpg", 200000);

        when(spotifyService.search("mixed")).thenReturn(List.of(track1, track2));
        when(lyricsService.fetchLyrics("Artist", "Has Lyrics")).thenReturn("some lyrics");
        when(lyricsService.fetchLyrics("Artist2", "No Lyrics")).thenReturn(null);

        List<SongSearchResultDTO> results = songService.search("mixed");

        assertEquals(2, results.size());
        verify(spotifyService, never()).getRecommendations(any());
    }
}
