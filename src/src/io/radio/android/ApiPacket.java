package io.radio.android;

import android.graphics.Bitmap;
import android.graphics.Color;

public class ApiPacket {
	public boolean online;
	public String np;
	public String list;
	public int kbps;
	public long start;
	public long end;
	public long cur;
	public String dj;
	public String djimg;
	public String djtext;
	public String thread;
	public Bitmap djImage;
	public String songName;
	public String artistName;
    public Tracks[] queue;
    public Tracks[] lastPlayed;
    public int progress;
    public int length;
    public int djColor;

    public ApiPacket() {
		online = false;
		np = "unknown";
		list = "";
		kbps = 0;
		start = 0;
		end = 1;
		cur = 0;
		dj = "";
		djimg = "";
		djtext = "";
		thread = "";
		songName = "";
		artistName = "";
        djColor = Color.parseColor("#00DDFF");
	}

}