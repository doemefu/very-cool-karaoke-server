package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Vote;
import ch.uzh.ifi.hase.soprafs26.entity.VotingRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("voteRepository")
public interface VoteRepository extends JpaRepository<Vote, Long> {
    boolean existsByVotingRoundAndVoter(VotingRound votingRound, User voter);

    @Query("""
           SELECT v.votedSong.id AS songId, COUNT(v) AS count
           FROM Vote v
           WHERE v.votingRound = :round
           GROUP BY v.votedSong.id
           """)
    List<SongVoteCount> countVotesPerSong(@Param("round") VotingRound round);

    interface SongVoteCount {
        Long getSongId();
        long getCount();
    }
}