package io.radio.android;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.json.JSONArray;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class RadioAutoCompleteProvider extends ContentProvider {

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] arg1, String arg2, String[] arg3,
			String arg4) {

		String keyword = uri.getLastPathSegment();
		String[] databaseColumns = { BaseColumns._ID,
				SearchManager.SUGGEST_COLUMN_TEXT_1,
				SearchManager.SUGGEST_COLUMN_QUERY};

		MatrixCursor suggestions = new MatrixCursor(databaseColumns);

		try {
			URL apiUrl = new URL(
					"https://r-a-d.io/search/autocomplete.php?query=" + keyword);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					apiUrl.openStream()));
			String input = "";
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				input += inputLine;
			in.close();

			JSONArray results = new JSONArray(input);

			for (int index = 0; index < results.length(); index++) {
				// System.out.println(results.getString(index));
				String[] row = { Integer.toString(index),
						results.getString(index),
						results.getString(index)};
				suggestions.addRow(row);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return suggestions;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return 0;
	}

}
