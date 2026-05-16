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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final VotingService votingService;
    private final UserService userService;

    private final Map<String, Optional<String>> lyricsCache = new ConcurrentHashMap<>();

    public SongService(SpotifyService spotifyService, LyricsService lyricsService,
                       SongRepository songRepository, SessionService sessionService,
                       SongWebSocketPublisher songWebSocketPublisher,
                       VotingService votingService, UserService userService) {
        this.spotifyService = spotifyService;
        this.lyricsService = lyricsService;
        this.songRepository = songRepository;
        this.sessionService = sessionService;
        this.songWebSocketPublisher = songWebSocketPublisher;
        this.votingService = votingService;
        this.userService = userService;
    }

    public List<SongSearchResultDTO> search(String query) {
        List<SpotifyTrack> tracks = spotifyService.search(query);
        return checkLyricsAndBuildDtos(tracks);
    }

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

    @Transactional
    public SongGetDTO addToQueue(Long sessionId, SongPostDTO dto, String token) {
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
        song.setAddedBy(userService.getUserByToken(token));
        song = songRepository.save(song);

        session.addSong(song); // update in-memory list for broadcast

        Map<Long, Long> emptyVotes = Collections.emptyMap();

        // Broadcast updated queue (no votes yet → empty counts map)
        List<SongGetDTO> queue = session.getPlaylist().stream()
                .filter(s -> !Boolean.TRUE.equals(s.getPerformed()))
                .map(s -> DTOMapper.INSTANCE.toSongGetDTO(s, emptyVotes))
                .toList();
        songWebSocketPublisher.broadcastQueue(sessionId, queue);

        // If this is the only unplayed song, the queue was empty before — promote it as currentSong
        if (queue.size() == 1) {
            SongGetDTO songDTO = DTOMapper.INSTANCE.toSongGetDTO(song, emptyVotes);
            songWebSocketPublisher.broadcastCurrentSong(sessionId, songDTO);
            songWebSocketPublisher.broadcastLyrics(sessionId, song.getLyrics());
        }

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
    public void deleteSongFromQueue(Long sessionId, Long songId, String token) {
        sessionService.verifyIsAdmin(sessionId, token);
        Session session = sessionService.getSessionById(sessionId);

        Song songToDelete = songRepository.findById(songId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Song not found"));

        if (!songToDelete.getSession().getId().equals(sessionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Song not found in this session");
        }

        boolean isCurrentSong = session.getPlaylist().stream()
                .filter(s -> !Boolean.TRUE.equals(s.getPerformed()))
                .findFirst()
                .map(s -> s.getId().equals(songId))
                .orElse(false);

        votingService.removeSongFromCandidates(songToDelete.getId());
        session.removeSong(songToDelete);
        songRepository.delete(songToDelete);

        if (isCurrentSong) {
            promoteNextSong(sessionId, session);
        } else {
            Map<Long, Long> emptyVotes = Collections.emptyMap();
            List<SongGetDTO> remainingQueue = session.getPlaylist().stream()
                    .filter(s -> !Boolean.TRUE.equals(s.getPerformed()))
                    .map(s -> DTOMapper.INSTANCE.toSongGetDTO(s, emptyVotes))
                    .toList();
            songWebSocketPublisher.broadcastQueue(sessionId, remainingQueue);
        }
    }

    @Transactional
    public void nextSong(Long sessionId) {
        Session session = sessionService.getSessionById(sessionId);

        session.getPlaylist().stream()
                .filter(s -> !Boolean.TRUE.equals(s.getPerformed()))
                .findFirst()
                .ifPresent(current -> {
                    current.markPerformed();
                    songRepository.save(current);
                });

        promoteNextSong(sessionId, session);
    }

    @Transactional
    public void promoteNextSong(Long sessionId, Session session) {
        Map<Long, Long> emptyVotes = Collections.emptyMap();
        List<Song> unplayedSongs = session.getPlaylist().stream()
                .filter(s -> !Boolean.TRUE.equals(s.getPerformed()))
                .toList();

        if (unplayedSongs.size() >= 2) {
            votingService.createVotingRound(sessionId);
        } else if (unplayedSongs.size() == 1) {
            Song lastSong = unplayedSongs.get(0);
            SongGetDTO nextSongDTO = DTOMapper.INSTANCE.toSongGetDTO(lastSong, emptyVotes);
            songWebSocketPublisher.broadcastCurrentSong(sessionId, nextSongDTO);
            songWebSocketPublisher.broadcastQueue(sessionId, List.of(nextSongDTO));
            songWebSocketPublisher.broadcastLyrics(sessionId, lastSong.getLyrics());
        } else {
            songWebSocketPublisher.broadcastCurrentSong(sessionId, null);
            songWebSocketPublisher.broadcastQueue(sessionId, Collections.emptyList());
            songWebSocketPublisher.broadcastLyrics(sessionId, null);
        }
    }

    @Transactional(readOnly = true)
    public void broadcastVotingRoundSongWinner(Long sessionId, Song winner, Map<Long, Long> voteCounts) {
        Session session = sessionService.getSessionById(sessionId);
        Map<Long, Long> emptyVotes = Collections.emptyMap();
        SongGetDTO nextSong = DTOMapper.INSTANCE.toSongGetDTO(winner, voteCounts);

        List<SongGetDTO> remainingQueue = session.getPlaylist().stream()
                .filter(song -> !Boolean.TRUE.equals(song.getPerformed()))
                .map(song -> DTOMapper.INSTANCE.toSongGetDTO(song, emptyVotes))
                .toList();

        songWebSocketPublisher.broadcastCurrentSong(sessionId, nextSong);
        songWebSocketPublisher.broadcastQueue(sessionId, remainingQueue);
        songWebSocketPublisher.broadcastLyrics(sessionId, winner.getLyrics());
    }
}
