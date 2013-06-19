package io.r.a.dio;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by andcolem on 6/19/13.
 */

public class TracksAdapter extends ArrayAdapter<Tracks> {
    private Tracks[] tracks;
    private Context c = null;

    public TracksAdapter(Context context, int textViewResourceId, Tracks[] tracks) {
        super(context, textViewResourceId, tracks);
        this.tracks = tracks;
        this.c = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.track_textview, null);
        }
        TextView songName = null;
        TextView artistName = null;
        //TextView isRequest = null;
        Tracks t = tracks[position];

        artistName = (TextView) v.findViewById(R.id.track_artistName);
        songName = (TextView) v.findViewById(R.id.track_songName);
        //isRequest = (TextView) v.findViewById(R.id.track_isRequest);
        artistName.setText(t.artistName);
        songName.setText(t.songName);
        if (t.isRequest) {
            artistName.setTypeface(null, Typeface.BOLD);
            songName.setTypeface(null, Typeface.BOLD);
        }
        return v;
    }
}
