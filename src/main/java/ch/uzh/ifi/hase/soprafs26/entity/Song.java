package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    private String albumArt;

    @Column(nullable = false)
    private Integer durationMs;

    @Column(columnDefinition = "TEXT") // To escape char limit
    private String lyrics;

    // to track the status of the song
    @Column(nullable = false)
    private Boolean performed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    // to mark the song as performed
    public void markPerformed() {
        this.performed = true;
    }
}
