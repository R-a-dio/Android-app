package io.r.a.dio;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {
	RadioService service;
	private TextView songName;
	private TextView artistName;
	private TextView djName;
	private ProgressBar songProgressBar;
	private Timer progressBarTimer;
	private ImageView djImage;
	private TextView listeners;
	private TextView songLength;
	private int progress;
	private int length;

	private ServiceConnection serviceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName arg0, IBinder binder) {
			service = ((RadioService.LocalBinder) binder).getService();
			Message m = Message.obtain();
			m.what = ApiUtil.ACTIVITYCONNECTED;
			m.replyTo = mMessenger;
			try {
				service.getMessenger().send(m);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			service.updateApiData();
		}

		public void onServiceDisconnected(ComponentName name) {
			// Activity disconnected never sent
			service = null;
		}
	};
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == ApiUtil.NPUPDATE) {
				ApiPacket packet = (ApiPacket) msg.obj;
				MainActivity.this.updateNP(packet);
			}
		}
	};

	public Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 99) {
				progress++;
				songProgressBar.setProgress(progress);
				songLength.setText(formatSongLength(progress, length));
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_layout_scroll);
		songName = (TextView) findViewById(R.id.main_SongName);
		artistName = (TextView) findViewById(R.id.main_ArtistName);
		djName = (TextView) findViewById(R.id.main_DjName);
		djImage = (ImageView) findViewById(R.id.main_DjImage);
		songProgressBar = (ProgressBar) findViewById(R.id.main_SongProgress);
		listeners = (TextView) findViewById(R.id.main_Listeners);
		songLength = (TextView) findViewById(R.id.main_SongLength);
		startService();

		progressBarTimer = new Timer();
		progressBarTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				mHandler.obtainMessage(99).sendToTarget();
			}

		}, 0, 1000);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// unbindService(serviceConnection);
	}

	public void startService() {
		Intent servIntent = new Intent(this, RadioService.class);
		if (RadioService.serviceStarted == false) {
			startService(servIntent);
			bindService(servIntent, serviceConnection, Context.BIND_AUTO_CREATE);
			RadioService.serviceStarted = true;
			service = RadioService.service;
		} else {
			bindService(servIntent, serviceConnection, Context.BIND_AUTO_CREATE);
			service = RadioService.service;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_play:
			service.restartPlayer();
			service.updateApiData();
			return true;
		case R.id.menu_pause:
			service.stopPlayer();
			return true;
		case R.id.menu_share:
			shareTrack();
			return true;
		case R.id.menu_settings:
			// do nothing
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void shareTrack() {
		String shareHeading = "Share track title.";

		String shareText = songName.getText() + " - " + artistName.getText();
		Intent i = new Intent(android.content.Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_TEXT, shareText);

		startActivity(Intent.createChooser(i, shareHeading));

	}

	private String lastDjImg = "";

	private String formatSongLength(int progress, int length) {
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

	private void updateNP(ApiPacket packet) {
		progress = (int) (packet.cur - packet.start);
		length = (int) (packet.end - packet.start);
		songName.setText(packet.songName);
		artistName.setText(packet.artistName);
		djName.setText(packet.dj);
		songProgressBar.setMax((int) (packet.end - packet.start));
		songProgressBar.setProgress(progress);
		if (!lastDjImg.equals(packet.djimg)) {
			lastDjImg = packet.djimg;
			DJImageLoader imageLoader = new DJImageLoader();
			imageLoader.execute(packet);

		}
		listeners.setText("Listeners: " + packet.list);
		songLength.setText(formatSongLength(progress, length));

		LinearLayout queueLayout = (LinearLayout) findViewById(R.id.queueList);
		queueLayout.removeAllViews();
		LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (packet.queue != null) {
			for (Tracks t : packet.queue) {
				View v = vi.inflate(R.layout.track_tableview, null);
				TextView artistName = (TextView) v
						.findViewById(R.id.track_artistName);
				TextView songName = (TextView) v
						.findViewById(R.id.track_songName);
				artistName.setText(t.artistName);
				songName.setText(t.songName);
				if (t.isRequest) {
					artistName.setTypeface(null, Typeface.BOLD);
					songName.setTypeface(null, Typeface.BOLD);
				}
				queueLayout.addView(v);
			}
		} else {
			View v = vi.inflate(R.layout.track_tableview, null);
			TextView artistName = (TextView) v
					.findViewById(R.id.track_artistName);
			TextView songName = (TextView) v.findViewById(R.id.track_songName);
			artistName.setText("-");
			songName.setText("-");
			queueLayout.addView(v);
		}

		LinearLayout lpLayout = (LinearLayout) findViewById(R.id.lastPlayedList);
		lpLayout.removeAllViews();
		if (packet.lastPlayed != null) {
			for (Tracks t : packet.lastPlayed) {
				View v = vi.inflate(R.layout.track_tableview, null);
				TextView artistName = (TextView) v
						.findViewById(R.id.track_artistName);
				TextView songName = (TextView) v
						.findViewById(R.id.track_songName);
				artistName.setText(t.artistName);
				songName.setText(t.songName);
				if (t.isRequest) {
					artistName.setTypeface(null, Typeface.BOLD);
					songName.setTypeface(null, Typeface.BOLD);
				}
				lpLayout.addView(v);
			}
		} else {
			View v = vi.inflate(R.layout.track_tableview, null);
			TextView artistName = (TextView) v
					.findViewById(R.id.track_artistName);
			TextView songName = (TextView) v.findViewById(R.id.track_songName);
			artistName.setText("-");
			songName.setText("-");
			lpLayout.addView(v);
		}

	}

	private class DJImageLoader extends AsyncTask<ApiPacket, Void, Void> {
		private Bitmap image;

		@Override
		protected Void doInBackground(ApiPacket... params) {
			ApiPacket pack = params[0];
			URL url;
			try {
				url = new URL("http://r-a-d.io" + pack.djimg);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setDoInput(true);
				conn.connect();
				InputStream is = conn.getInputStream();
				image = BitmapFactory.decodeStream(is);
			} catch (Exception e) {
				e.printStackTrace();
				image = null;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (image != null) {
				djImage.setImageBitmap(image);
				service.updateNotificationImage(image);
			}
		}

	}
}
