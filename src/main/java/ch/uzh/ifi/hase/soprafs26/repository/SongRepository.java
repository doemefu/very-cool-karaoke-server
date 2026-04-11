package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("songRepository")
public interface SongRepository extends JpaRepository<Song, Long> {
    Song findBySpotifyId(String spotifyId);
    Song findByTitle(String title);
}
