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
}
