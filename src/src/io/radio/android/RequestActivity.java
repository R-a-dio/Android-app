package io.radio.android;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by aki on 1/07/13.
 */
public class RequestActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_layout);

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            new SearchTask().execute(query);
        }
    }

    protected class SearchPage {
        public boolean status;
        public String cooldown;
        public RequestSong[] results;
        public int pages;
        public int page;
        public boolean hasResults;

        public SearchPage(String json) {
            try {
                System.out.println(json);
                JSONObject obj = new JSONObject(json);
                pages = obj.getInt("pages");

                if (pages > 0) {
                    status = obj.getBoolean("status");
                    cooldown = obj.getString("cooldown");
                    page = obj.getInt("page");

                    JSONArray songArray = obj.getJSONArray("result");
                    ArrayList<RequestSong> requestSongs = new ArrayList<RequestSong>();
                    for (int i = 0; i < songArray.length(); i++) {
                        JSONArray songObj = (JSONArray) songArray.get(i);
                        requestSongs.add(new RequestSong(songObj));
                    }

                    Object[] array = requestSongs.toArray();
                    results =  Arrays.copyOf(array, array.length, RequestSong[].class);

                    hasResults = true;
                } else {
                    hasResults = false;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    protected class RequestSong {
        public String artistName;
        public String songName;
        public long lastRequestedStart;
        public long lastRequestedEnd;
        public int songId;
        public boolean isRequestable;

        public RequestSong(JSONArray array) {
            try {
                artistName = array.getString(0);
                songName = array.getString(1);
                lastRequestedStart = array.getLong(2);
                lastRequestedEnd = array.getLong(3);
                songId = array.getInt(4);
                isRequestable = array.getBoolean(5);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    private class SearchTask extends AsyncTask<String, Void, Void> {

        ArrayList<SearchPage> searchPages;

        protected Void doInBackground(String... query) {
            searchPages = new ArrayList<SearchPage>();
            try {
                SearchPage searchPage = new SearchPage(readJSON(query[0], 1));
                searchPages.add(searchPage);
                if (searchPage.hasResults)
                if (searchPage.pages > 1) {
                    for (int i = 2; i <= searchPage.pages; i++) {
                        searchPages.add(new SearchPage(readJSON(query[0], i)));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected String readJSON(String query, int page) {
            String result = "";
            try {
                String urlEncodedQuery = URLEncoder.encode(query, "UTF-8");
                URL apiURl = new URL(getString(R.string.searchApiURL) + "?query=" + urlEncodedQuery + "&page=" + Integer.toString(page));
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        apiURl.openStream()));
                result = in.readLine();
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        protected void onPostExecute(Void v) {
            TextView textView = (TextView) findViewById(R.id.textView);
            for (SearchPage page : searchPages) {
                if (page.hasResults)
                    for (RequestSong song : page.results) {
                        textView.setText(textView.getText() + "\n" + Integer.toString(song.songId) + " " + song.artistName + " " + song.songName);
                    }
            }
        }
    }
}