package io.radio.android;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
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
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.RemoteViews;
import android.widget.Toast;

public class RadioService extends Service implements OnPreparedListener,
		MediaPlayer.OnErrorListener {
	public static boolean isRunning;
	private final IBinder binder = new LocalBinder();
	private Messenger messenger;
	private boolean activityConnected;
	private Messenger activityMessenger;
	private ApiPacket current;
	private NotificationHandler notificationManager;
	private Timer updateTimer;
	private MediaPlayer radioPlayer;
	public static boolean serviceStarted = false;
	public static RadioService service;
	AppWidgetManager widgetManager;

	public DJ dj;
	public int djId;
	public String title = "";
	public String artist = "";

	public static boolean currentlyPlaying = false;
	public static boolean incomingOrDialingCall = false;

	public boolean apiToastDisplayedOnce = false;
	
	public BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("restart")) {
				restartPlayer();
			}
			if (intent.getAction().equals("stop")) {
				stopPlayer();
			}
			if (intent.getAction().equals("api update")) {
				updateApiData();
			}
			if (intent.getAction().equals("api fail") && !apiToastDisplayedOnce) {
				CharSequence text = "The R/a/dio server doesn't seem to be responding. Check your internet connection or update the app";
				int duration = Toast.LENGTH_LONG;

				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
				
				apiToastDisplayedOnce = true;
			}
			if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
				int state = intent.getIntExtra("state", -1);
				if (state == 0)
					stopPlayer();
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		isRunning = true;
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

		// API call

		
		
		// Create the Mediaplayer and setup to play stream
		radioPlayer = new MediaPlayer();
		radioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		radioPlayer.setOnPreparedListener(this);
		try {
			radioPlayer.setDataSource(getString(R.string.streamURL));
		} catch (Exception e) {
			e.printStackTrace();
		}

		PhoneStateListener phoneStateListener = new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				if (((state == TelephonyManager.CALL_STATE_RINGING) || (state == TelephonyManager.CALL_STATE_OFFHOOK))
						&& currentlyPlaying) {
					stopPlayer();
					incomingOrDialingCall = true;
				} else if (state == TelephonyManager.CALL_STATE_IDLE) {
					if (incomingOrDialingCall) {
						restartPlayer();
						incomingOrDialingCall = false;
					}
				}
				super.onCallStateChanged(state, incomingNumber);
			}
		};
		TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		if (mgr != null) {
			mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		handleIntent(intent);
		return START_STICKY;
	}

	private void handleIntent(Intent intent) {
		switch (intent.getIntExtra("Remote Command", -1)) {
		case -1:
			return;
		case ApiUtil.REMOTEMUSICPLAY:
			restartPlayer();
			break;
		case ApiUtil.REMOTEMUSICSTOP:
			stopPlayer();
			break;
		case ApiUtil.REMOTEMUSICPLAYPAUSE:
			if (currentlyPlaying)
				stopPlayer();
			else
				restartPlayer();
			break;
		}
	}

	public void onPrepared(MediaPlayer mp) {
		radioPlayer.start();
	}

	public void registerBroadcasts() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("restart");
		filter.addAction("stop");
		filter.addAction("api update");
		filter.addAction("api fail");
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		registerReceiver(receiver, filter);
	}

	public void initializeTimers() {
		updateTimer = new Timer();

		// Schedule API updates every 10 seconds
		updateTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				Intent intent = new Intent();
				intent.setAction("api update");
				sendBroadcast(intent);
			}
		}, 0, 10000);

		// Schedule widget update every second
		updateTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				RemoteViews view = new RemoteViews(getPackageName(),
						R.layout.widget_layout);
                try {
                    current.main.progress++;

                    // Push update for this widget to the home screen
                    RadioWidgetProvider.updateWidget(
                            getApplicationContext(),
                            AppWidgetManager.getInstance(getApplicationContext()),
                            true, current.main.metadata, current.main.length,
                            current.main.progress
                    );
                }
                catch (Exception E){
                    System.out.println("Error in initializeTimers");
                }
			}
		}, 0, 1000);
	}

	public boolean onError(MediaPlayer mp, int what, int extra) {
		return true;
	}

	public void stopPlayer() {
		// updateTimer.cancel();
		radioPlayer.reset();
		currentlyPlaying = false;

		Message m = Message.obtain();
		m.what = ApiUtil.MUSICSTOP;
		m.obj = current;
		if (activityConnected) {
			try {
				activityMessenger.send(m);
			} catch (RemoteException e) {
				// Whatever...
			}
		}
	}

	public static void sendCommand(Context context, int command) {
		Intent intent = new Intent(context, RadioService.class);
		intent.putExtra("Remote Command", command);
		context.startService(intent);
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
		currentlyPlaying = true;

		Message m = Message.obtain();
		m.what = ApiUtil.MUSICSTART;
		m.obj = current;
		if (activityConnected) {
			try {
				activityMessenger.send(m);
			} catch (RemoteException e) {
				// Whatever...
			}
		}
	}

	public Messenger getMessenger() {
		return this.messenger;
	}

	public int getAudioStreamId() {
		return radioPlayer.getAudioSessionId();
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
			try {
                System.out.println(R.string.mainApiURL);
				URL apiURl = new URL(getString(R.string.mainApiURL));
				BufferedReader in = new BufferedReader(new InputStreamReader(
						apiURl.openStream()));
				String input = "";
				String inputLine;
				while ((inputLine = in.readLine()) != null)
					input += inputLine;
				in.close();
				resultPacket = ApiUtil.getMain(input);

				apiToastDisplayedOnce = false;
			} catch (Exception e) {
				e.printStackTrace();
				Intent intent = new Intent();
				intent.setAction("api fail");
				sendBroadcast(intent);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			current = resultPacket;
			
			if (current == null)
				return;
			
			Message m = Message.obtain();
			m.what = ApiUtil.NPUPDATE;
			m.obj = current;
			if (activityConnected) {
				try {
					activityMessenger.send(m);
				} catch (RemoteException e) {
					// Whatever...
				}
			}
			if (!artist.equals(current.main.artist)
					&& !title.equals(current.main.title)) {
				notificationManager.newSongTickerNotification(
						current.main.title, current.main.artist);
				artist = current.main.artist;
				title = current.main.title;
			}
			
			if (djId != current.main.dj.id) {
				notificationManager.newDjTickerNotification(current.main.dj);
				dj = current.main.dj;
				djId = current.main.dj.id;
			}
			notificationManager.updateNotificationWithInfo(current);
		}

	}

	public void updateNotificationImage(Bitmap image) {
		notificationManager.updateNotificationImage(current, image);
	}

	public void updateWidgetImage(Bitmap image) {
		RemoteViews view = new RemoteViews(getPackageName(),
				R.layout.widget_layout);
		view.setImageViewBitmap(R.id.widget_bg, image);

		// Push update for this widget to the home screen
		RadioWidgetProvider.updateWidget(getApplicationContext(),
				AppWidgetManager.getInstance(getApplicationContext()), false,
				null, 0, 0);
	}

	@Override
	public void onDestroy() {
		stopPlayer();
		radioPlayer.release();
		unregisterReceiver(receiver);
		updateTimer.cancel();
		isRunning = false;
		RadioWidgetProvider.updateWidget(getApplicationContext(),
				AppWidgetManager.getInstance(getApplicationContext()), true,
				"", -1, -1);
	}

}