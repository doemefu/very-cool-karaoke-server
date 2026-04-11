package ch.uzh.ifi.hase.soprafs26.entity;

import ch.uzh.ifi.hase.soprafs26.constant.SessionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "sessions")
public class Session {

    // getters and setters via Lombok

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
    @CreationTimestamp
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

    public void start() {
        if (this.status != SessionStatus.CREATED) {
            throw new IllegalStateException("Can only start from LOBBY");
        }
        this.status = SessionStatus.ACTIVE;
    }

    public void pause() {
        if (this.status != SessionStatus.ACTIVE) {
            throw new IllegalStateException("Can only pause from ACTIVE");
        }
        this.status = SessionStatus.PAUSED;
    }

    public void resume() {
        if (this.status != SessionStatus.PAUSED) {
            throw new IllegalStateException("Can only resume from PAUSED");
        }
        this.status = SessionStatus.ACTIVE;
    }

    public void end() {
        if (this.status != SessionStatus.ACTIVE && this.status != SessionStatus.PAUSED) {
            throw new IllegalStateException("Can only end from ACTIVE or PAUSED");
        }
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


}
