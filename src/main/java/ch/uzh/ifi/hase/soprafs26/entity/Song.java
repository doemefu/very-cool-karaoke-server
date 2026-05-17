package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Setter
@Getter
@Entity
@Table(name = "songs")
public class Song {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String spotifyId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String artist;

    @Column()
    private String albumName;

    @Column()
    private String albumArt;

    @Column(nullable = false)
    private Integer durationMs;

    @Column(columnDefinition = "TEXT") // To escape char limit
    private String lyrics;

    // to track the status of the song
    @Column(nullable = false)
    private Boolean performed = false;

    @Column
    private LocalDateTime playedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by_id")
    private User addedBy;

    // to mark the song as performed
    public void markPerformed() {
        this.performed = true;
        this.playedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
