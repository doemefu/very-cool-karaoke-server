package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.Song;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.VotingRound;
import ch.uzh.ifi.hase.soprafs26.rest.dto.*;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically
 * transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g.,
 * UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for
 * creating information (POST).
 */
@Mapper
public interface DTOMapper {

    DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

    @Mapping(source = "username", target = "username")
    @Mapping(source = "password", target = "password")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "token", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "createdAt", target = "createdAt")
    UserGetDTO convertEntityToUserGetDTO(User user);

    @Mapping(source = "admin", target = "admin")
    @Mapping(source = "participants", target = "participants")
    @Mapping(source = "createdAt", target = "createdAt")
    SessionGetDTO convertEntityToSessionGetDTO(Session session);

    default OffsetDateTime map(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atOffset(ZoneOffset.UTC);
    }

    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "token", target = "token")
    UserTokenDTO convertEntityToUserTokenDTO(User user);

    @Mapping(target = "candidates", source = "candidates", qualifiedByName = "sortedByVotes")
    VotingRoundGetDTO toVotingRoundGetDTO(VotingRound round, @Context Map<Long, Long> counts);

    @Mapping(target = "currentVoteCount", expression = "java(counts.getOrDefault(song.getId(), 0L).intValue())")
    SongGetDTO toSongGetDTO(Song song, @Context Map<Long, Long> counts);

    @Named("sortedByVotes")
    default List<SongGetDTO> sortedByVotes(List<Song> songs, @Context Map<Long, Long> counts) {
        return songs.stream()
                .map(s -> toSongGetDTO(s, counts))
                .sorted(Comparator.comparingInt(SongGetDTO::getCurrentVoteCount).reversed())
                .toList();
    }
}
