package io.radio.android;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Color;
import android.text.Html;

public class ApiUtil {
	public static final int NPUPDATE = 0;
	public static final int ACTIVITYCONNECTED = 1;
	public static final int ACTIVITYDISCONNECTED = 2;
	public static final int PROGRESSUPDATE = 3;
    public static final int MUSICSTART = 4;
    public static final int MUSICSTOP = 5;


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
        String[] cs = jObj.getString("djcolor").split(" ");
        pack.djColor = Color.argb(255,Integer.parseInt(cs[0]),Integer.parseInt(cs[1]),Integer.parseInt(cs[2]));

		JSONArray lpArray = jObj.getJSONArray("lp");
        pack.lastPlayed = getTracks(lpArray);

		// queue is null during a DJ session
		if (jObj.has("queue")) {
			JSONArray queueArray = jObj.getJSONArray("queue");
            pack.queue = getTracks(queueArray);
		}

		pack.progress = (int)(pack.cur - pack.start);
		pack.length = (int)(pack.end - pack.start);
		return pack;
	}

    private static Tracks[] getTracks(JSONArray JSONarray) {
        ArrayList<Tracks> list = new ArrayList<Tracks>();
            for (int i = 0; i < JSONarray.length(); i++) {
                JSONArray obj = null;
                String track = "";
                boolean isRequest = false;
                try {
                    obj = (JSONArray) JSONarray.get(i);
                    track = obj.getString(1);
                    isRequest = obj.getInt(2) == 1 ? true : false;
                } catch (Exception e) {}
                String songName = "-";
                String artistName = "-";
                int hyphenPos = track.indexOf(" - ");
                
                if (hyphenPos==-1)
                {
                    songName = track;
                }
                else
                {
                    try {
                    	songName = Html.fromHtml(track.substring(hyphenPos+3)).toString();
                        artistName = Html.fromHtml(track.substring(0,hyphenPos)).toString();
                    } catch (Exception e) {}
                }
                list.add(new Tracks(songName, artistName, isRequest));
            }
            Object[] array = list.toArray();
            return Arrays.copyOf(array, array.length, Tracks[].class);
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