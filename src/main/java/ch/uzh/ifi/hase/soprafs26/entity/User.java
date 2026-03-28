package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDate;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import java.util.Objects;
import jakarta.persistence.*;


@Entity
@Table(name = "users")
public class User implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDate createdAt;


    // equals / hashCode on id 
    // required for Set<User> (used in Session.participants) to correctly 
    // identify duplicates and make addParticipant() idempotent.
    // two User objects with the same database id are the same participant.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User other = (User) o;
        // compare by id. Returns false for transient entities (id == null)
        // so they don't accidentally collide before being persisted.
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        // a fixed constant for transient entities (id == null) ensures they
        // can safely live in a HashSet before being saved. Once persisted,
        // id is stable and Objects.hash(id) is consistent across calls.
        return id == null ? 31 : Objects.hash(id);
    }


    // getters and setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }
}
