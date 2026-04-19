package ch.uzh.ifi.hase.soprafs26.websocket;

public class LyricsPayload {
    private String lyrics;

    public LyricsPayload() {}
    public LyricsPayload(String lyrics) { this.lyrics = lyrics; }

    public String getLyrics() { return lyrics; }
    public void setLyrics(String lyrics) { this.lyrics = lyrics; }
}