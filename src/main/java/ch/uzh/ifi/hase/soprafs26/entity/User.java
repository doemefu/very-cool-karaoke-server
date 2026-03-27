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
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.OFFLINE;


    // equals / hashCode on id 
    // required for Set<User> (used in Session.participants) to correctly 
    // identify duplicates and make addParticipant() idempotent.
    // two User objects with the same database id are the same participant.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User other = (User) o;
        // Use id-based equality only when both ids are set (post-persist).
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        // Constant hash for transient entities (id == null) so they can
        // still live in a Set before being persisted, without breaking
        // the contract.  Once persisted the id is stable.
        return id == null ? 31 : Objects.hash(id);
    }

    // domain method
    public void changePassword(String newPassword) {
        this.password = newPassword;
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

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
}
