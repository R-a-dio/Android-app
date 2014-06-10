package io.radio.android;

public class Track {
    public String songName;
    public String artistName;
    public boolean isRequest;
    public Track(String songName, String artistName, boolean isRequest) {
        this.songName = songName;
        this.artistName = artistName;
        this.isRequest = isRequest;
    }
}