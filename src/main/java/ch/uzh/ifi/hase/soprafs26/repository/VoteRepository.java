package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Vote;
import ch.uzh.ifi.hase.soprafs26.entity.VotingRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("voteRepository")
public interface VoteRepository extends JpaRepository<Vote, Long> {
    boolean existsByVotingRoundAndVoter(VotingRound votingRound, User voter);
}