package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.constant.VotingStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.VotingRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("votingRoundRepository")
public interface VotingRoundRepository extends JpaRepository<VotingRound, Long> {
    List<VotingRound> findBySessionAndStatus(Session session, VotingStatus status);
    @Query("SELECT DISTINCT vr FROM VotingRound vr LEFT JOIN FETCH vr.candidates WHERE vr.id = :id")
    Optional<VotingRound> findByIdWithCandidates(@Param("id") Long id);
    List<VotingRound> findBySessionOrderByStartsAtAsc(Session session);

    @Modifying
    @Query(value = "DELETE FROM voting_round_candidates WHERE song_id = :songId", nativeQuery = true)
    void deleteCandidatesBySongId(@Param("songId") Long songId);
}
