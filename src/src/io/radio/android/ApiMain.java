package io.radio.android;

import org.json.JSONException;
import org.json.JSONObject;

public class ApiMain implements ApiDetails {
	
	public String metadata;
	public String thread;
	public String djName;
	
	public int id;
	public int listeners;
	public boolean requesting;
	
	public long current;
	public long start;
	public long end;
	
	
	public ApiMain(JSONObject json) throws JSONException
	{
		this.metadata = json.getString("np");
		this.thread = json.getString("thread");
		this.djName = json.getString("djname");
		this.id = json.getInt("trackid");
		this.listeners = json.getInt("listeners");
		this.requesting = json.getInt("requests") == 1 ? true : false;
		this.current = json.getLong("current");
		this.start = json.getLong("start_time");
		this.end = json.getLong("end_time");
	}
}
