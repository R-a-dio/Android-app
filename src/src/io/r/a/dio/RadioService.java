package io.r.a.dio;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.RemoteViews;
import android.widget.Toast;

public class RadioService extends Service implements OnPreparedListener,
		MediaPlayer.OnErrorListener {
	private final IBinder binder = new LocalBinder();
	private Messenger messenger;
	private boolean activityConnected;
	private Messenger activityMessenger;
	private ApiPacket currentPacket = new ApiPacket();
	private NotificationHandler notificationManager;
	private Timer apiDataTimer;
	private Timer widgetTimer;
	MediaPlayer radioPlayer;
	public static boolean serviceStarted = false;
	public static RadioService service;
	AppWidgetManager widgetManager;

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("restart")) {
				restartPlayer();
			}
			if (intent.getAction().equals("stop")) {
				stopPlayer();
			}
			if (intent.getAction().equals("api fail")) {
				CharSequence text = "The R/a/dio server doesn't seem to be responding. You may have to update the app";
				int duration = Toast.LENGTH_SHORT;

				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		notificationManager = new NotificationHandler(this);
		widgetManager = AppWidgetManager.getInstance(this);
		service = this;

		registerBroadcasts();
		initializeTimers();

		messenger = new Messenger(new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case ApiUtil.ACTIVITYCONNECTED:
					activityConnected = true;
					activityMessenger = msg.replyTo;
					break;
				case ApiUtil.ACTIVITYDISCONNECTED:
					activityConnected = false;
					break;
				}
			}
		});
		this.startForeground(NotificationHandler.CONSTANTNOTIFICATION,
				notificationManager.constantNotification());

		radioPlayer = new MediaPlayer();
		radioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		radioPlayer.setOnPreparedListener(this);
		try {
			radioPlayer.setDataSource(getString(R.string.streamURL));
		} catch (Exception e) {
			e.printStackTrace();
		}
		radioPlayer.prepareAsync();

	}

	public void onPrepared(MediaPlayer mp) {
		radioPlayer.start();
	}

	public void registerBroadcasts() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("restart");
		filter.addAction("stop");
		filter.addAction("api fail");
		registerReceiver(receiver, filter);
	}

	public void initializeTimers() {
		apiDataTimer = new Timer();
		apiDataTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				updateApiData();
			}
		}, 0, 10000);
		widgetTimer = new Timer();
		widgetTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				RemoteViews view = new RemoteViews(getPackageName(),
						R.layout.widget_layout);
				currentPacket.progress++;
				view.setTextViewText(R.id.widget_NowPlaying, currentPacket.np);
				view.setProgressBar(R.id.widget_ProgressBar,
						currentPacket.length, currentPacket.progress, false);
				view.setTextViewText(R.id.widget_SongLength, ApiUtil
						.formatSongLength(currentPacket.progress,
								currentPacket.length));

				// Push update for this widget to the home screen
				ComponentName thisWidget = new ComponentName(
						getApplicationContext(), RadioWidgetProvider.class);
				AppWidgetManager manager = AppWidgetManager
						.getInstance(getApplicationContext());
				manager.updateAppWidget(thisWidget, view);
			}
		}, 0, 1000);
	}

	public boolean onError(MediaPlayer mp, int what, int extra) {
		return true;
	}

	public void stopPlayer() {
		radioPlayer.reset();
	}

	// call
	public void restartPlayer() {
		radioPlayer.reset();
		try {
			radioPlayer.setDataSource(getString(R.string.streamURL));
		} catch (Exception e) {
			e.printStackTrace();
		}
		radioPlayer.prepareAsync();

	}

	public Messenger getMessenger() {
		return this.messenger;
	}

	public class LocalBinder extends Binder {
		public RadioService getService() {
			return RadioService.this;
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		activityConnected = false;
		return super.onUnbind(intent);
	}

	public void updateApiData() {
		ApiDataGetter apiGetter = new ApiDataGetter();
		apiGetter.execute();
	}

	private class ApiDataGetter extends AsyncTask<Void, Void, Void> {
		ApiPacket resultPacket;

		@Override
		protected Void doInBackground(Void... params) {
			resultPacket = new ApiPacket();
			try {
				URL apiURl = new URL(getString(R.string.mainApiURL));
				BufferedReader in = new BufferedReader(new InputStreamReader(
						apiURl.openStream()));
				String inputLine = in.readLine();
				in.close();
				resultPacket = ApiUtil.parseJSON(inputLine);
				String[] songParts = resultPacket.np.split(" - ");
				if (songParts.length == 2) {
					resultPacket.artistName = songParts[0];
					resultPacket.songName = songParts[1];
				} else {
					resultPacket.songName = songParts[0];
					resultPacket.artistName = "-";
				}

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Api getter failed");
				Intent intent = new Intent();
				intent.setAction("api fail");
				sendBroadcast(intent);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			currentPacket = resultPacket;
			Message m = Message.obtain();
			m.what = ApiUtil.NPUPDATE;
			m.obj = currentPacket;
			if (activityConnected) {
				try {
					activityMessenger.send(m);
				} catch (RemoteException e) {
					// Whatever...
				}
			}
			notificationManager.updateNotificationWithInfo(currentPacket);
		}

	}

	public void updateNotificationImage(Bitmap image) {
		notificationManager.updateNotificationImage(currentPacket, image);
	}

	public void updateWidgetImage(Bitmap image) {
		RemoteViews view = new RemoteViews(getPackageName(),
				R.layout.widget_layout);
		view.setImageViewBitmap(R.id.widget_djImage, image);

		// Push update for this widget to the home screen
		ComponentName thisWidget = new ComponentName(getApplicationContext(),
				RadioWidgetProvider.class);
		AppWidgetManager manager = AppWidgetManager
				.getInstance(getApplicationContext());
		manager.updateAppWidget(thisWidget, view);
	}

	@Override
	public void onDestroy() {
		radioPlayer.release();
		unregisterReceiver(receiver);
		widgetTimer.cancel();
		apiDataTimer.cancel();
	}

}
