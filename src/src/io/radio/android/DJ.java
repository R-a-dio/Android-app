package io.radio.android;

import org.json.JSONException;
import org.json.JSONObject;

public class DJ {
	public String name;
	public int id;
	
	public DJ(JSONObject json) throws JSONException
	{
		this.name = json.getString("djname");
		this.id = json.getInt("id");
	}
}
