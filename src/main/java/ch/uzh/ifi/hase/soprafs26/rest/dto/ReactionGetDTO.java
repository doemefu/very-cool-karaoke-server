package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.ReactionType;
import lombok.Getter;

import java.time.Instant;

@Getter
public class ReactionGetDTO {

    private Long id;
    private ReactionType type;
    private Instant sentAt;
    private UserGetDTO sender;
    private SongGetDTO duringPerformance;

    public void setId(Long id) {
        this.id = id;
    }

    public void setType(ReactionType type) {
        this.type = type;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }

    public void setSender(UserGetDTO sender) {
        this.sender = sender;
    }

    public void setDuringPerformance(SongGetDTO duringPerformance) {
        this.duringPerformance = duringPerformance;
    }
}
