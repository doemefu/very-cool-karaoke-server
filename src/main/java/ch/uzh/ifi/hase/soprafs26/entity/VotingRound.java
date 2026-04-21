package ch.uzh.ifi.hase.soprafs26.entity;

import ch.uzh.ifi.hase.soprafs26.constant.VotingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "voting_rounds")
public class VotingRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VotingStatus status = VotingStatus.OPEN;

    @Column(nullable = false)
    private LocalDateTime startsAt;

    @Column(nullable = true)
    private LocalDateTime endsAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "voting_round_candidates",
            joinColumns = @JoinColumn(name = "voting_round_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    private List<Song> candidates = new ArrayList<>();

    @OneToMany(mappedBy = "votingRound", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vote> votes = new ArrayList<>();

}