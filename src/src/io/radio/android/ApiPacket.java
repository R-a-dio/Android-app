package io.radio.android;

import org.json.JSONException;
import org.json.JSONObject;

public class ApiPacket {
	public ApiDetails main;
	public ApiMeta meta;
	
	public ApiPacket(JSONObject packet) throws JSONException
	{
		this.main = new ApiMain(packet.getJSONObject("main"));
		this.meta = new ApiMeta(packet.getJSONObject("meta"));
	}
}