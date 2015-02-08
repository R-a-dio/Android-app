package io.radio.android;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

import android.text.Html;

public class ApiUtil {
	public static final int NPUPDATE = 0;
	public static final int ACTIVITYCONNECTED = 1;
	public static final int ACTIVITYDISCONNECTED = 2;
	public static final int PROGRESSUPDATE = 3;
    public static final int MUSICSTART = 4;
    public static final int MUSICSTOP = 5;
    
    public static final int REMOTEMUSICSTOP = 1;
    public static final int REMOTEMUSICPLAY = 2;
    public static final int REMOTEMUSICPLAYPAUSE = 3;

    /**
     * Gets the primary API response: /
     * @param jsonString a JSON API string
     * @return
     * @throws Exception
     */
    public static ApiPacket getMain(String jsonString) throws Exception {
		
    	JSONObject json = new JSONObject(jsonString);
    	ApiPacket packet = new ApiPacket(json);
		
		return packet;
	}

    private static Track[] getTracks(JSONArray JSONarray) {
        ArrayList<Track> list = new ArrayList<Track>();
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
                list.add(new Track(songName, artistName, isRequest));
            }
            Object[] array = list.toArray();
            return Arrays.copyOf(array, array.length, Track[].class);
    }
	static public String intTimeDurationToString (int duration) {
        StringBuilder sb = new StringBuilder();
        int minutes = duration / 60;
        int seconds = duration % 60;

        if (minutes < 10)
            sb.append("0");
        sb.append(minutes);
        sb.append(":");
        if (seconds < 10)
            sb.append("0");
        sb.append(seconds);

        return sb.toString();
    }
}