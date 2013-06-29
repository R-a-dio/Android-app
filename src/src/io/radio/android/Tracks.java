package io.radio.android;

public class Tracks {
    public String songName;
    public String artistName;
    public boolean isRequest;
    public Tracks(String songName, String artistName, boolean isRequest) {
        this.songName = songName;
        this.artistName = artistName;
        this.isRequest = isRequest;
    }
}