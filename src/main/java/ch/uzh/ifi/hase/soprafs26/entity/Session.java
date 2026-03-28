package ch.uzh.ifi.hase.soprafs26.entity;

import ch.uzh.ifi.hase.soprafs26.constant.SessionStatus;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

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

    @Column(nullable = false, updatable = false)
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

}
