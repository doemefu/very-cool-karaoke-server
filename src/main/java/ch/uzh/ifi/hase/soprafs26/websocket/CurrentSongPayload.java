package ch.uzh.ifi.hase.soprafs26.websocket;

import ch.uzh.ifi.hase.soprafs26.rest.dto.SongGetDTO;

public class CurrentSongPayload {
    private SongGetDTO song;

    public CurrentSongPayload() {
    }

    public CurrentSongPayload(SongGetDTO song) {
        this.song = song;
    }

    public SongGetDTO getSong() {
        return song;
    }

    public void setSong(SongGetDTO song) {
        this.song = song;
    }
}