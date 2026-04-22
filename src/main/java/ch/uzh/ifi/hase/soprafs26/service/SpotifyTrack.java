package ch.uzh.ifi.hase.soprafs26.service;

public record SpotifyTrack(String spotifyId, String title, String artist, String albumName, String albumArt, int durationMs) {
}
