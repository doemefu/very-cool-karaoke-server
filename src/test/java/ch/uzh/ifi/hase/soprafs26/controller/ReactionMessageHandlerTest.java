package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.constant.ReactionType;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ReactionGetDTO;
import ch.uzh.ifi.hase.soprafs26.service.SessionService;
import ch.uzh.ifi.hase.soprafs26.websocket.ReactionWebSocketPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactionMessageHandlerTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private ReactionWebSocketPublisher reactionPublisher;

    @InjectMocks
    private ReactionMessageHandler handler;

    private User participant;

    @BeforeEach
    void setUp() {
        participant = new User();
        participant.setId(42L);
        participant.setUsername("singer1");
    }

    @Test
    void handleReaction_validPayload_broadcastsReactionWithCorrectFields() {
        when(sessionService.getParticipants(1L)).thenReturn(Set.of(participant));

        Map<String, Object> payload = Map.of("userId", 42, "type", "FIRE");
        handler.handleReaction(1L, payload);

        ArgumentCaptor<ReactionGetDTO> captor = ArgumentCaptor.forClass(ReactionGetDTO.class);
        verify(reactionPublisher).broadcastReaction(eq(1L), captor.capture());

        ReactionGetDTO dto = captor.getValue();
        assertEquals(ReactionType.FIRE, dto.getType());
        assertEquals(42L, dto.getSender().getId());
        assertEquals("singer1", dto.getSender().getUsername());
        assertNotNull(dto.getSentAt());
    }

    @Test
    void handleReaction_allReactionTypes_broadcast() {
        when(sessionService.getParticipants(1L)).thenReturn(Set.of(participant));

        for (ReactionType type : ReactionType.values()) {
            Map<String, Object> payload = Map.of("userId", 42, "type", type.getValue());
            handler.handleReaction(1L, payload);
        }

        verify(reactionPublisher, times(ReactionType.values().length))
                .broadcastReaction(eq(1L), any(ReactionGetDTO.class));
    }

    @Test
    void handleReaction_missingUserId_throwsIllegalArgument() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "FIRE");
        // no userId

        assertThrows(IllegalArgumentException.class,
                () -> handler.handleReaction(1L, payload));

        verify(reactionPublisher, never()).broadcastReaction(any(), any());
    }

    @Test
    void handleReaction_missingType_throwsIllegalArgument() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", 42);
        // no type

        assertThrows(IllegalArgumentException.class,
                () -> handler.handleReaction(1L, payload));

        verify(reactionPublisher, never()).broadcastReaction(any(), any());
    }

    @Test
    void handleReaction_userNotParticipant_throwsIllegalArgument() {
        when(sessionService.getParticipants(1L)).thenReturn(Set.of(participant));

        Map<String, Object> payload = Map.of("userId", 999, "type", "FIRE");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> handler.handleReaction(1L, payload));

        assertTrue(ex.getMessage().contains("not a participant"));
        verify(reactionPublisher, never()).broadcastReaction(any(), any());
    }

    @Test
    void handleReaction_invalidReactionType_throwsIllegalArgument() {
        when(sessionService.getParticipants(1L)).thenReturn(Set.of(participant));

        Map<String, Object> payload = Map.of("userId", 42, "type", "INVALID_TYPE");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> handler.handleReaction(1L, payload));

        assertTrue(ex.getMessage().contains("Unknown reaction type"));
        verify(reactionPublisher, never()).broadcastReaction(any(), any());
    }

    @Test
    void handleReaction_userIdAsString_parsedCorrectly() {
        when(sessionService.getParticipants(1L)).thenReturn(Set.of(participant));

        Map<String, Object> payload = Map.of("userId", "42", "type", "CLAP");
        handler.handleReaction(1L, payload);

        ArgumentCaptor<ReactionGetDTO> captor = ArgumentCaptor.forClass(ReactionGetDTO.class);
        verify(reactionPublisher).broadcastReaction(eq(1L), captor.capture());
        assertEquals(ReactionType.CLAP, captor.getValue().getType());
    }

    @Test
    void handleReaction_emptyParticipants_throwsIllegalArgument() {
        when(sessionService.getParticipants(1L)).thenReturn(Set.of());

        Map<String, Object> payload = Map.of("userId", 42, "type", "HEART");

        assertThrows(IllegalArgumentException.class,
                () -> handler.handleReaction(1L, payload));

        verify(reactionPublisher, never()).broadcastReaction(any(), any());
    }
}
