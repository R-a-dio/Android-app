package io.radio.android;

import java.net.URLDecoder;

import org.json.JSONException;
import org.json.JSONObject;

public class ApiMain {
	
	public String metadata;
	public String thread;
	public String name;
	public String title;
	public String artist;
	
	public int id;
	public int listeners;
	public boolean requesting;
	
	public long current;
	public long start;
	public long end;
	
	public int progress;
	public int length;
	
	public Track[] queue;
	public Track[] lp;
	
	public DJ dj;
	
	public ApiMain(JSONObject json) throws JSONException
	{
		this.metadata = json.getString("np");
		
		this.thread = json.getString("thread");
		this.name = json.getString("djname");
		this.dj = new DJ(json.getJSONObject("dj"));
		
		this.id = json.getInt("trackid");
		this.listeners = json.getInt("listeners");
		this.requesting = json.getInt("requesting") == 1 ? true : false;
		
		this.current = json.getLong("current");
		this.start = json.getLong("start_time");
		this.end = json.getLong("end_time");
		
		this.parseMetadata();
		this.parseTimes();
	}
	
	public void parseMetadata()
	{
		int pos = this.metadata.indexOf("-");
		
		if (pos == -1)
		{
			this.title = this.metadata;
			this.artist = "";
		}
		else
		{
			try
			{
				this.title = URLDecoder.decode(
						this.metadata.substring(pos + 1),
						"UTF-8");
				this.artist = URLDecoder.decode(
						this.metadata.substring(0, pos),
						"UTF-8");
			}
			catch (Exception e)
			{
				this.artist = "";
				this.title = "";
			}
		}
	}
	
	public void parseTimes()
	{
		this.progress = (int)(this.current - this.start);
		this.length = (int)(this.end - this.start);
	}
}
