package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.Song;
import ch.uzh.ifi.hase.soprafs26.repository.SongRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SongGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SongPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SongSearchResultDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.websocket.SongWebSocketPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Service
public class SongService {

    private final SpotifyService spotifyService;
    private final LyricsService lyricsService;
    private final SongRepository songRepository;
    private final SessionService sessionService;
    private final SongWebSocketPublisher songWebSocketPublisher;

    // ConcurrentHashMap doesn't allow null values, so we wrap in Optional
    private final Map<String, Optional<String>> lyricsCache = new ConcurrentHashMap<>();

    public SongService(SpotifyService spotifyService, LyricsService lyricsService,
                       SongRepository songRepository, SessionService sessionService,
                       SongWebSocketPublisher songWebSocketPublisher) {
        this.spotifyService = spotifyService;
        this.lyricsService = lyricsService;
        this.songRepository = songRepository;
        this.sessionService = sessionService;
        this.songWebSocketPublisher = songWebSocketPublisher;
    }

    /**
     * Searches Spotify for tracks matching the query, checks lyrics availability
     * for all results in parallel, caches the lyrics, and returns the result list.
     * If ALL results have no lyrics, triggers a recommendations fallback: keeps
     * the first result at position 0 (to signal unavailability) and fills
     * positions 1–3 with up to 3 recommendations that have lyrics.
     */
    public List<SongSearchResultDTO> search(String query) {
        List<SpotifyTrack> tracks = spotifyService.search(query);
        List<SongSearchResultDTO> results = checkLyricsAndBuildDtos(tracks);

        boolean noneHaveLyrics = results.stream().noneMatch(SongSearchResultDTO::getLyricsAvailable);
        if (noneHaveLyrics && !results.isEmpty()) {
            SongSearchResultDTO firstResult = results.get(0);
            // Fetch recommendations with lyrics; if none found, degraded response is [firstResult] only
            List<SongSearchResultDTO> recommendations =
                    fetchRecommendationsWithMinLyrics(firstResult.getSpotifyId(), 2, 3);
            List<SongSearchResultDTO> combined = new ArrayList<>();
            combined.add(firstResult);
            combined.addAll(recommendations);
            return combined;
        }
        return results;
    }

    /**
     * Fans out lyrics checks in parallel for each track, caches results,
     * and builds SongSearchResultDTOs. Shared by search() and getRecommendationsForSong().
     */
    private List<SongSearchResultDTO> checkLyricsAndBuildDtos(List<SpotifyTrack> tracks) {
        List<CompletableFuture<String>> futures = tracks.stream()
                .map(t -> CompletableFuture.supplyAsync(
                        () -> lyricsService.fetchLyrics(t.artist(), t.title())))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return IntStream.range(0, tracks.size()).mapToObj(i -> {
            SpotifyTrack t = tracks.get(i);
            String lyrics = futures.get(i).join();
            lyricsCache.put(t.spotifyId(), Optional.ofNullable(lyrics));

            SongSearchResultDTO dto = new SongSearchResultDTO();
            dto.setSpotifyId(t.spotifyId());
            dto.setTitle(t.title());
            dto.setArtist(t.artist());
            dto.setAlbumName(t.albumName());
            dto.setAlbumArt(t.albumArt());
            dto.setDurationMs(t.durationMs());
            dto.setDurationSeconds(t.durationMs() / 1000);
            dto.setLyricsAvailable(lyrics != null);
            return dto;
        }).toList();
    }

    /**
     * Returns up to 5 Spotify recommendations for the given seed track that have lyrics available.
     * Throws 404 if no recommendations with lyrics are found.
     */
    public List<SongSearchResultDTO> getRecommendationsForSong(String spotifyId) {
        List<SpotifyTrack> tracks = spotifyService.getRecommendations(spotifyId);
        List<SongSearchResultDTO> withLyrics = checkLyricsAndBuildDtos(tracks).stream()
                .filter(SongSearchResultDTO::getLyricsAvailable)
                .limit(5)
                .toList();
        if (withLyrics.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No recommendations with lyrics found for spotifyId: " + spotifyId);
        }
        return withLyrics;
    }

    /**
     * Iteratively fetches recommendations (up to 3 rounds of up to 10 each) until
     * at least {@code minWithLyrics} results with lyrics are collected, deduplicating
     * across rounds. Returns only results with lyrics, limited to {@code maxResults}.
     */
    private List<SongSearchResultDTO> fetchRecommendationsWithMinLyrics(String seedTrackId, int minWithLyrics, int maxResults) {
        List<SongSearchResultDTO> collected = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();
        int lyricsCount = 0;
        int attempts = 0;
        while (lyricsCount < minWithLyrics && attempts < 3) {
            List<SpotifyTrack> batch = spotifyService.getRecommendations(seedTrackId);
            for (SongSearchResultDTO dto : checkLyricsAndBuildDtos(batch)) {
                if (seenIds.add(dto.getSpotifyId())) {
                    collected.add(dto);
                    if (Boolean.TRUE.equals(dto.getLyricsAvailable())) lyricsCount++;
                }
            }
            attempts++;
        }
        return collected.stream()
                .filter(SongSearchResultDTO::getLyricsAvailable)
                .limit(maxResults)
                .toList();
    }

    @Transactional
    public SongGetDTO addToQueue(Long sessionId, SongPostDTO dto) {
        Session session = sessionService.getSessionById(sessionId); // throws 404

        Song song = new Song();
        song.setSpotifyId(dto.getSpotifyId());
        song.setTitle(dto.getTitle());
        song.setArtist(dto.getArtist());
        song.setAlbumName(dto.getAlbumName());
        song.setAlbumArt(dto.getAlbumArt());
        song.setDurationMs(dto.getDurationMs());
        song.setLyrics(getCachedLyrics(dto.getSpotifyId())); // nullable
        song.setSession(session);
        song = songRepository.save(song);

        session.addSong(song); // update in-memory list for broadcast

        Map<Long, Long> emptyVotes = Collections.emptyMap();

        // Broadcast updated queue (no votes yet → empty counts map)
        List<SongGetDTO> queue = session.getPlaylist().stream()
                .filter(s -> !Boolean.TRUE.equals(s.getPerformed()))
                .map(s -> DTOMapper.INSTANCE.toSongGetDTO(s, emptyVotes))
                .toList();
        songWebSocketPublisher.broadcastQueue(sessionId, queue);

        return DTOMapper.INSTANCE.toSongGetDTO(song, emptyVotes);
    }

    /**
     * Returns the cached lyrics for a given Spotify track ID, or null if not cached
     * or if lyrics were not available.
     */
    public String getCachedLyrics(String spotifyId) {
        return lyricsCache.getOrDefault(spotifyId, Optional.empty()).orElse(null);
    }

    Map<String, Optional<String>> getLyricsCache() {
        return Map.copyOf(lyricsCache);
    }

    void cacheLyrics(String spotifyId, String lyrics) {
        lyricsCache.put(spotifyId, Optional.ofNullable(lyrics));
    }

    @Transactional(readOnly = true)
    public Optional<SongGetDTO> getCurrentSong(Long sessionId) {
        Session session = sessionService.getSessionById(sessionId);
        Map<Long, Long> emptyVotes = Collections.emptyMap();

        return session.getPlaylist().stream()
                .filter(s -> !Boolean.TRUE.equals(s.getPerformed()))
                .findFirst()
                .map(s -> DTOMapper.INSTANCE.toSongGetDTO(s, emptyVotes));
    }

    @Transactional(readOnly = true)
    public List<SongGetDTO> getQueue(Long sessionId) {
        Session session = sessionService.getSessionById(sessionId);
        Map<Long, Long> emptyVotes = Collections.emptyMap();

        return session.getPlaylist().stream()
                .filter(s -> !Boolean.TRUE.equals(s.getPerformed()))
                .map(s -> DTOMapper.INSTANCE.toSongGetDTO(s, emptyVotes))
                .toList();
    }

    @Transactional
    public void nextSong(Long sessionId) {
        Session session = sessionService.getSessionById(sessionId);
        List<Song> playlist = session.getPlaylist();
        Map<Long, Long> emptyVotes = Collections.emptyMap();

        playlist.stream()
                .filter(s -> !Boolean.TRUE.equals(s.getPerformed()))
                .findFirst()
                .ifPresent(s -> {
                    s.markPerformed();
                    songRepository.save(s);
                });

        List<SongGetDTO> updatedQueue = playlist.stream()
                .filter(s -> !Boolean.TRUE.equals(s.getPerformed()))
                .map(s -> DTOMapper.INSTANCE.toSongGetDTO(s, emptyVotes))
                .toList();

        SongGetDTO next = updatedQueue.isEmpty() ? null : updatedQueue.get(0);
        songWebSocketPublisher.broadcastCurrentSong(sessionId, next);
        songWebSocketPublisher.broadcastQueue(sessionId, updatedQueue);
    }
}
