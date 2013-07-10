package io.radio.android;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.gesture.GestureOverlayView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.RemoteControlClient;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class MainActivity extends Activity {
	public static String PREFS_FILENAME = "RADIOPREFS";

	RadioService service;
	private TextView songName;
	private TextView artistName;
	private TextView djName;
	private ProgressBar songProgressBar;
	private Timer progressTimer;
	private ImageView djImage;
	private TextView listeners;
	private TextView songLength;
	private int progress;
	private int length;
	private ImageButton playButton;
	private ImageButton shareButton;
	private ImageButton searchButton;
	private ImageButton faveButton;
	private ViewFlipper viewFlipper;
	private GestureOverlayView gestureOverlay;
	private ScrollView queueScroll;
	private ScrollView lpScroll;

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
			if (msg.what == ApiUtil.PROGRESSUPDATE) {
				progress++;
				songProgressBar.setProgress(progress);
				songLength.setText(ApiUtil.formatSongLength(progress, length));
			}
			if (msg.what == ApiUtil.MUSICSTOP || msg.what == ApiUtil.MUSICSTART) {
				updatePlayButton();
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player_layout);
		getActionBar().hide();

		// Allow keys to change volume without playing
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// Find and get all the layout items
		songName = (TextView) findViewById(R.id.main_SongName);
		artistName = (TextView) findViewById(R.id.main_ArtistName);
		djName = (TextView) findViewById(R.id.main_DjName);
		djImage = (ImageView) findViewById(R.id.main_DjImage);
		songProgressBar = (ProgressBar) findViewById(R.id.main_SongProgress);
		listeners = (TextView) findViewById(R.id.main_Listeners);
		songLength = (TextView) findViewById(R.id.main_SongLength);

		// Set up drawer
		final ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);
		final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.player_drawer);

		LayoutInflater inflater = getLayoutInflater();
		String[] names = new String[] { "Now Playing", "Last Played", "Queue",
				"Favorites", "Settings" };

		// Set the adapter for the list view
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item, names));
		mDrawerList
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> adapterView,
							View view, int i, long l) {
						Intent intent;
						switch (i) {
						case 0:
							drawer.closeDrawers();
							break;
						case 1:
							intent = new Intent(getApplicationContext(),
									LastPlayedActivity.class);
							startActivity(intent);
							break;
						case 2:
							intent = new Intent(getApplicationContext(),
									QueueActivity.class);
							startActivity(intent);
							break;
						case 3:
							break;
						case 4:
							startActivity(new Intent(getApplicationContext(),
									SettingsActivity.class));
							break;
						default:
							drawer.closeDrawers();
						}
					}
				});

		// Set up controls
		playButton = (ImageButton) findViewById(R.id.player_play);
		playButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (service.currentlyPlaying) {
					service.stopPlayer();
				} else {
					service.restartPlayer();
					service.updateApiData();
					// need indication to the user that the stream is loading ie
					// progressbar
				}
				// Service will notify play button status
			}
		});
		shareButton = (ImageButton) findViewById(R.id.player_share);
		shareButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				shareTrack();
			}
		});
		searchButton = (ImageButton) findViewById(R.id.player_search);
		searchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				startActivity(new Intent(getApplicationContext(),
						RequestActivity.class));
			}
		});
		faveButton = (ImageButton) findViewById(R.id.player_fave);
		faveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

			}
		});
		viewFlipper = (ViewFlipper) findViewById(R.id.player_flipper);
		queueScroll = (ScrollView) findViewById(R.id.player_queuescroll);
		lpScroll = (ScrollView) findViewById(R.id.player_lpscroll);
		gestureOverlay = (GestureOverlayView) findViewById(R.id.player_gestureoverlay);
		final GestureDetector gestureDetector = new GestureDetector(this,
				new Detector());
		View.OnTouchListener gestureListener = new View.OnTouchListener() {
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (!gestureDetector.onTouchEvent(motionEvent))
					return viewFlipper.getCurrentView().onTouchEvent(motionEvent);
				return true;
			}
		};
		gestureOverlay.setOnTouchListener(gestureListener);

		// Start Radio service
		startService();

		// Start progress timer to estimate progress between api updates
		progressTimer = new Timer();
		progressTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				Message msg = Message.obtain();
				msg.what = ApiUtil.PROGRESSUPDATE;
				try {
					mMessenger.send(msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}, 0, 1000);

		SharedPreferences pref = getSharedPreferences(PREFS_FILENAME,
				Context.MODE_PRIVATE);
		if (!pref.getBoolean("player_firstrun_hint_shown", false)) {
			View view = inflater.inflate(R.layout.player_intro_toast_layout,
					(ViewGroup) findViewById(R.id.relativeLayout1));
			Toast toast = new Toast(this);
			toast.setDuration(Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.setView(view);
			toast.show();
			SharedPreferences.Editor editor = pref.edit();
			editor.putBoolean("player_firstrun_hint_shown", true);
			editor.apply();
		}

		// Initialize Remote Controls if SDK Version >=14
		// initializeRemoteControls();
	}

	@TargetApi(14)
	private void initializeRemoteControls() {
		if (Build.VERSION.SDK_INT >= 14) {
			ComponentName eventRecevier = new ComponentName(getPackageName(),
					LockscreenReceiver.class.getName());
			AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			audioManager.registerMediaButtonEventReceiver(eventRecevier);
			Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
			mediaButtonIntent.setComponent(eventRecevier);
			PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(
					getApplicationContext(), 0, mediaButtonIntent, 0);
			RemoteControlClient remoteControlClient = new RemoteControlClient(
					mediaPendingIntent);
			remoteControlClient
					.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
			remoteControlClient
					.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY
							| RemoteControlClient.FLAG_KEY_MEDIA_STOP);
			audioManager.registerRemoteControlClient(remoteControlClient);
			audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
					AudioManager.AUDIOFOCUS_GAIN);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unbindService(serviceConnection);
		stopService(new Intent(MainActivity.this, RadioService.class));
		progressTimer.cancel();
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

	private class Detector extends GestureDetector.SimpleOnGestureListener {
		private static final String TAG = "Detector";
		private static final int SWIPE_MIN_DISTANCE = 120;
		private static final int SWIPE_MAX_OFF_PATH = 250;
		private static final int SWIPE_THRESHOLD_VELOCITY = 200;

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			try {
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
					return false;

				// Swipe to the Left <-O
				if (goRight(e1, e2, velocityX)) {
					switch (viewFlipper.getCurrentView().getId()) {
					case R.id.player_page_current:
					case R.id.player_lpscroll:
						viewFlipper.setInAnimation(inFromRightAnimation());
						viewFlipper.setOutAnimation(outToLeftAnimation());
						viewFlipper.showNext();
						break;
					}
				}
				// Swipe to the right O->
				else if (goLeft(e1, e2, velocityX)) {
					switch (viewFlipper.getCurrentView().getId()) {
					case R.id.player_page_current:
					case R.id.player_queuescroll:
						viewFlipper.setInAnimation(inFromLeftAnimation());
						viewFlipper.setOutAnimation(outToRightAnimation());
						viewFlipper.showPrevious();
						break;
					}
				}
			} catch (Exception ex) {
				Log.e(this.TAG, ex.getMessage());
			}

			return true;
		}

		private boolean goRight(MotionEvent e1, MotionEvent e2, float v) {
			if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(v) > SWIPE_THRESHOLD_VELOCITY)
				return true;
			return false;
		}

		private boolean goLeft(MotionEvent e1, MotionEvent e2, float v) {
			if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(v) > SWIPE_THRESHOLD_VELOCITY)
				return true;
			return false;
		}

		private Animation inFromRightAnimation() {

			Animation inFromRight = new TranslateAnimation(
					Animation.RELATIVE_TO_PARENT, +1.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f);
			inFromRight.setDuration(350);
			inFromRight.setInterpolator(new AccelerateInterpolator());
			return inFromRight;
		}

		private Animation outToLeftAnimation() {
			Animation outtoLeft = new TranslateAnimation(
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, -1.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f);
			outtoLeft.setDuration(350);
			outtoLeft.setInterpolator(new AccelerateInterpolator());
			return outtoLeft;
		}

		private Animation inFromLeftAnimation() {
			Animation inFromLeft = new TranslateAnimation(
					Animation.RELATIVE_TO_PARENT, -1.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f);
			inFromLeft.setDuration(350);
			inFromLeft.setInterpolator(new AccelerateInterpolator());
			return inFromLeft;
		}

		private Animation outToRightAnimation() {
			Animation outtoRight = new TranslateAnimation(
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, +1.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f);
			outtoRight.setDuration(350);
			outtoRight.setInterpolator(new AccelerateInterpolator());
			return outtoRight;
		}
	}

	private void updatePlayButton() {
		if (service.currentlyPlaying) {
			playButton.setImageResource(R.drawable.av_stop);
		} else {
			playButton.setImageResource(R.drawable.av_play);
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

	private void updateNP(ApiPacket packet) {
		progress = (int) (packet.cur - packet.start);
		length = (int) (packet.end - packet.start);
		songName.setText(Html.fromHtml(packet.songName));
		artistName.setText(Html.fromHtml(packet.artistName));
		djName.setText(Html.fromHtml(packet.dj));
		songProgressBar.setMax(length);
		songProgressBar.setProgress(progress);
		if (!lastDjImg.equals(packet.djimg)) {
			lastDjImg = packet.djimg;
			DJImageLoader imageLoader = new DJImageLoader();
			imageLoader.execute(packet);

		}
		listeners.setText("Listeners: " + packet.list);
		songLength.setText(ApiUtil.formatSongLength(progress, length));

		// Display the Last Played and Queue from API Packet
		LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		LinearLayout lpLayout = (LinearLayout) findViewById(R.id.lastPlayedList);
		lpLayout.removeAllViews();
		if (packet.lastPlayed != null) {
			for (Tracks t : packet.lastPlayed) {
				View v = vi.inflate(R.layout.track_tableview, null);
				TextView artistName = (TextView) v
						.findViewById(R.id.track_artistName);
				TextView songName = (TextView) v
						.findViewById(R.id.track_songName);
				configureQueueTextView(artistName);
				configureQueueTextView(songName);
				artistName.setText(Html.fromHtml(t.artistName));
				songName.setText(Html.fromHtml(t.songName));
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
			configureQueueTextView(artistName);
			configureQueueTextView(songName);
			artistName.setText("-");
			songName.setText("-");
			lpLayout.addView(v);
		}

		LinearLayout queueLayout = (LinearLayout) findViewById(R.id.queueList);
		queueLayout.removeAllViews();

		if (packet.queue != null) {
			for (Tracks t : packet.queue) {
				View v = vi.inflate(R.layout.track_tableview, null);
				TextView artistName = (TextView) v
						.findViewById(R.id.track_artistName);
				TextView songName = (TextView) v
						.findViewById(R.id.track_songName);
				configureQueueTextView(artistName);
				configureQueueTextView(songName);
				artistName.setText(Html.fromHtml(t.artistName));
				songName.setText(Html.fromHtml(t.songName));
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
			configureQueueTextView(artistName);
			configureQueueTextView(songName);
			queueLayout.addView(v);
		}

	}

	private void configureQueueTextView(TextView v) {
		v.setTextColor(Color.WHITE);
		v.setShadowLayer(3.0f, 3.0f, 3.0f, Color.parseColor("#9f000000"));
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
				service.updateWidgetImage(image);
			}
		}

	}
}