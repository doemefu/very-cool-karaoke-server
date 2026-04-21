package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("sessionRepository")
public interface SessionRepository extends JpaRepository<Session, Long> {
    Session findByGamePin(String gamePin);

    boolean existsByGamePin(String gamePin);

    List<Session> findByAdminId(Long adminId);

    @Query("SELECT s FROM Session s JOIN s.participants p WHERE p.id = :userId")
    List<Session> findByParticipantId(@Param("userId") Long userId);
}
