package ch.uzh.ifi.hase.soprafs26.entity;

import ch.uzh.ifi.hase.soprafs26.constant.SessionStatus;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, unique = true, length = 6)
    private String gamePin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.CREATED;

    @Column(nullable = false)
    private LocalDateTime createdAt;


    /*
     Admin - the user who created and controls this session.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User admin;

    /*
     to track which users are active participants.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "session_participants",
        joinColumns = @JoinColumn(name = "session_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )

    private Set<User> participants = new HashSet<>();

    /*
     ordered list of songs added to this session's queue.
     */
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Song> playlist = new ArrayList<>();

    /*
     the song currently being performed (null if none).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_song_id")
    private Song currentSong;


    /*
     all voting rounds that have taken place in this session.
    */
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VotingRound> votingRounds = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.gamePin == null) {
            this.gamePin = generateGamePin();
        }
    }

    public String generateGamePin() {
        // 6-digit random PIN
        int pin = (int)(Math.random() * 900_000) + 100_000;
        return String.valueOf(pin);
    }

    public void start() {
        this.status = SessionStatus.ACTIVE;
    }

    public void pause() {
        this.status = SessionStatus.PAUSED;
    }

    public void end() {
        this.status = SessionStatus.ENDED;
    }

    /**
    add a user to the participant set
     * @param user the user to add; must not be null
     */
    public void addParticipant(User user) {
        this.participants.add(user);
    }

    /**
    remove a user from the participant set (soft-leave).
    @param user the user to remove; no-op if not currently a participant
    */
    public void removeParticipant(User user) {
        this.participants.remove(user);
    }

    public void setCurrentSong(Song song) {
        this.currentSong = song;
    }


    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGamePin() { return gamePin; }
    public void setGamePin(String gamePin) { this.gamePin = gamePin; }

    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public User getAdmin() { return admin; }
    public void setAdmin(User admin) { this.admin = admin; }

    public Set<User> getParticipants() { return participants; }
    public void setParticipants(Set<User> participants) { this.participants = participants; }

    public List<Song> getPlaylist() { return playlist; }
    public void setPlaylist(List<Song> playlist) { this.playlist = playlist; }

    public Song getCurrentSong() { return currentSong; }

    public List<VotingRound> getVotingRounds() { return votingRounds; }
    public void setVotingRounds(List<VotingRound> votingRounds) { this.votingRounds = votingRounds; }
}
