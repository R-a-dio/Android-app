package io.r.a.dio;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

public class ApiUtil {
	public static final int NPUPDATE = 0;
	public static final int ACTIVITYCONNECTED = 1;
	public static final int ACTIVITYDISCONNECTED = 2;
	public static final int PROGRESSUPDATE = 3;

	public static ApiPacket parseJSON(String JSON) throws Exception {
		ApiPacket pack = new ApiPacket();

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
		pack.thread = jObj.getString("thread");

		JSONArray lpArray = jObj.getJSONArray("lp");

		ArrayList<Tracks> lastPlayedList = new ArrayList<Tracks>();
		for (int i = 0; i < lpArray.length(); i++) {
			JSONArray lpObj = (JSONArray) lpArray.get(i);
			String track = lpObj.getString(1);
			String[] details = track.split(" - ");
			String songName = "-";
			String artistName = "-";

			// checking against songs
			if (details.length == 2) {
				songName = details[0];
				artistName = details[1];
			} else if (details.length == 1) {
				songName = details[0];
			}
			boolean isRequest = lpObj.getInt(2) == 1 ? true : false;
			lastPlayedList.add(new Tracks(songName, artistName, isRequest));
		}
		Object[] array = lastPlayedList.toArray();
		pack.lastPlayed = Arrays.copyOf(array, array.length, Tracks[].class);

		// queue is null during a DJ session
		if (jObj.has("queue")) {
			JSONArray queueArray = jObj.getJSONArray("queue");

			ArrayList<Tracks> queueList = new ArrayList<Tracks>();
			for (int i = 0; i < queueArray.length(); i++) {
				JSONArray lpObj = (JSONArray) queueArray.get(i);
				String track = lpObj.getString(1);
				String[] details = track.split(" - ");
				String songName = "-";
				String artistName = "-";
				if (details.length == 2) {
					songName = details[0];
					artistName = details[1];
				} else if (details.length == 1) {
					songName = details[0];
				}

				boolean isRequest = lpObj.getInt(2) == 1 ? true : false;
				queueList.add(new Tracks(songName, artistName, isRequest));
			}
			array = queueList.toArray();
			pack.queue = Arrays.copyOf(array, array.length, Tracks[].class);
		}

		pack.progress = (int)(pack.cur - pack.start);
		pack.length = (int)(pack.end - pack.start);
		return pack;
	}
	
	static public String formatSongLength(int progress, int length) {
		StringBuilder sb = new StringBuilder();

		int progMins = progress / 60;
		int progSecs = progress % 60;
		if (progMins < 10)
			sb.append("0");
		sb.append(progMins);
		sb.append(":");
		if (progSecs < 10)
			sb.append("0");
		sb.append(progSecs);

		sb.append(" / ");

		int lenMins = length / 60;
		int lenSecs = length % 60;
		if (lenMins < 10)
			sb.append("0");
		sb.append(lenMins);
		sb.append(":");
		if (lenSecs < 10)
			sb.append("0");
		sb.append(lenSecs);

		return sb.toString();
	}
}
