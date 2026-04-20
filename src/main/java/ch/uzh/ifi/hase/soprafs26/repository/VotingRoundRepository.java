package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.entity.VotingRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("votingRoundRepository")
public interface VotingRoundRepository extends JpaRepository<VotingRound, Long> {
}
