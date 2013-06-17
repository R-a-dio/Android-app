package io.r.a.dio;

import org.json.JSONObject;

public class ApiUtil {
	public static final int NPUPDATE = 0;
	public static final int ACTIVITYCONNECTED = 1;
	public static final int ACTIVITYDISCONNECTED = 2;

	public static ApiPacket parseJSON(String JSON) {
		ApiPacket pack = new ApiPacket();
		try {
			JSONObject jObj = new JSONObject(JSON);
			pack.online = jObj.getInt("online") == 1 ? true : false;
			pack.np = jObj.getString("np");
			pack.list = jObj.getString("list");
			pack.kbps = jObj.getInt("kbps");
			pack.start = jObj.getLong("start");
			pack.end = jObj.getLong("end");
			pack.cur = jObj.getLong("cur");
			pack.dj = jObj.getString("dj");
			pack.djimg = jObj.getString("djimg");
			pack.djtext = jObj.getString("djtext");
			pack.thread = jObj.getInt("thread");
		} catch (Exception e) {
			// ApiPack already initialized with defaults
		}

		return pack;
	}
}
