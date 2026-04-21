package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "votes",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_vote_round_voter",
                columnNames = {"voting_round_id", "voter_id"}
        )
)
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voting_round_id", nullable = false)
    private VotingRound votingRound;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voter_id", nullable = false)
    private User voter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voted_song_id", nullable = false)
    private Song votedSong;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime castAt;
}