package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.Song;
import ch.uzh.ifi.hase.soprafs26.repository.SongRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SongGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SongPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SongSearchResultDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.websocket.SongWebSocketPublisher;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
     */
    public List<SongSearchResultDTO> search(String query) {
        List<SpotifyTrack> tracks = spotifyService.search(query);

        // Fan out lyrics checks in parallel
        List<CompletableFuture<String>> lyricsFutures = tracks.stream()
                .map(track -> CompletableFuture.supplyAsync(
                        () -> lyricsService.fetchLyrics(track.artist(), track.title())))
                .collect(Collectors.toList());

        CompletableFuture.allOf(lyricsFutures.toArray(new CompletableFuture[0])).join();

        // Build DTOs and populate cache
        return tracks.stream().map(track -> {
            String lyrics = lyricsFutures.get(tracks.indexOf(track)).join();
            lyricsCache.put(track.spotifyId(), Optional.ofNullable(lyrics));

            SongSearchResultDTO dto = new SongSearchResultDTO();
            dto.setSpotifyId(track.spotifyId());
            dto.setTitle(track.title());
            dto.setArtist(track.artist());
            dto.setAlbumArt(track.albumArt());
            dto.setDurationMs(track.durationMs());
            dto.setLyricsAvailable(lyrics != null);
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public SongGetDTO addToQueue(Long sessionId, SongPostDTO dto) {
        Session session = sessionService.getSessionById(sessionId); // throws 404

        Song song = new Song();
        song.setSpotifyId(dto.getSpotifyId());
        song.setTitle(dto.getTitle());
        song.setArtist(dto.getArtist());
        song.setAlbumArt(dto.getAlbumArt());
        song.setDurationMs(dto.getDurationMs());
        song.setLyrics(getCachedLyrics(dto.getSpotifyId())); // nullable
        song.setSession(session);
        songRepository.save(song);

        session.addSong(song); // update in-memory list for broadcast

        // Broadcast updated queue (no votes yet → empty counts map)
        List<SongGetDTO> queue = session.getPlaylist().stream()
                .map(s -> DTOMapper.INSTANCE.toSongGetDTO(s, new HashMap<>()))
                .toList();
        songWebSocketPublisher.broadcastQueue(sessionId, queue);

        return DTOMapper.INSTANCE.toSongGetDTO(song, new HashMap<>());
    }

    /**
     * Returns the cached lyrics for a given Spotify track ID, or null if not cached
     * or if lyrics were not available.
     */
    public String getCachedLyrics(String spotifyId) {
        return lyricsCache.getOrDefault(spotifyId, Optional.empty()).orElse(null);
    }

    Map<String, Optional<String>> getLyricsCache() {
        return lyricsCache;
    }
}
