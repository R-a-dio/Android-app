package io.radio.android;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by andcolem on 7/3/13.
 */
public class LastPlayedActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lp_layout);
        ((ProgressBar) findViewById(R.id.lp_progressBar)).setVisibility(View.VISIBLE);
        new LoadTask().execute();
    }

    private class LoadTask extends AsyncTask<Void, Void, Void> {
        ArrayList<Track> list;
        public Void doInBackground(Void... params) {
            // load queue from api
            list = new ArrayList<Track>();
            try {
                URL apiURl = new URL(getString(R.string.lastPlayedApiURL));
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        apiURl.openStream()));
                String input = "";
                String inputLine;
                while ((inputLine = in.readLine())!=null)
                    input += inputLine;
                in.close();

                JSONArray json = new JSONArray(input);
                for (int i = 0; i < json.length(); i++) {
                    JSONArray trackObj = json.getJSONArray(i);
                    String track = trackObj.getString(1);
                    String songName = "-";
                    String artistName = "-";
                    int hyphenPos = track.indexOf(" - ");
                    if (hyphenPos==-1) {
                        songName = track;
                    } else {
                        try {
                            songName = Html.fromHtml(track.substring(hyphenPos+3)).toString();
                            artistName = Html.fromHtml(track.substring(0,hyphenPos)).toString();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    list.add(new Track(songName, artistName, false));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public void onPostExecute(Void vo) {
            ((ProgressBar) findViewById(R.id.lp_progressBar)).setVisibility(View.INVISIBLE);
            LinearLayout lpList = (LinearLayout) findViewById(R.id.lp_list);

            LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            for (Track t : list) {
                View v = vi.inflate(R.layout.track_tableview, null);
                TextView artistName = (TextView) v
                        .findViewById(R.id.track_artistName);
                TextView songName = (TextView) v
                        .findViewById(R.id.track_songName);
                artistName.setText(Html.fromHtml(t.artistName));
                songName.setText(Html.fromHtml(t.songName));          
                if (t.isRequest) {
                    artistName.setTypeface(null, Typeface.BOLD);
                    songName.setTypeface(null, Typeface.BOLD);
                }
                lpList.addView(v);
            }
        }
    }

}