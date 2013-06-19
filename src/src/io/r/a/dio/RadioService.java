package io.r.a.dio;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
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

public class RadioService extends Service implements OnPreparedListener, MediaPlayer.OnErrorListener {
	private final IBinder binder = new LocalBinder();
	private Messenger messenger;
	private boolean activityConnected;
	private Messenger activityMessenger;
	private ApiPacket currentApiPacket;
	private NotificationHandler notificationManager;
	private Timer apiDataTimer;
	MediaPlayer radioPlayer;
    public static boolean serviceStarted = false;
    public static RadioService service;

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		notificationManager = new NotificationHandler(this);
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
		apiDataTimer = new Timer();
		apiDataTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				updateApiData();
			}
		}, 0, 10000);
		radioPlayer = new MediaPlayer();
		radioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		radioPlayer.setOnPreparedListener(this);
		try {
		radioPlayer.setDataSource("http://r-a-d.io/lb/load-balance.php");
		} catch (Exception e) {
			e.printStackTrace();
		}
		radioPlayer.prepareAsync();
        service = this;
	}

	public void onPrepared(MediaPlayer mp) {
		radioPlayer.start();
	}

    public boolean onError(MediaPlayer mp, int what, int extra) {
        return true;
    }

    public void stopPlayer() {
        if (radioPlayer != null) {
            radioPlayer.stop();
            radioPlayer.reset();
            radioPlayer.release();
            radioPlayer = null;
        }
    }

    // call
    public void restartPlayer() {
        radioPlayer = new MediaPlayer();
        radioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        radioPlayer.setOnPreparedListener(this);
        try {
            radioPlayer.setDataSource("http://r-a-d.io/lb/load-balance.php");
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
				URL apiURl = new URL("http://r-a-d.io/api.php");
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
                    resultPacket.artistName = songParts[0];
                    resultPacket.songName = "-";
                }

            } catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			currentApiPacket = resultPacket;
			Message m = Message.obtain();
			m.what = ApiUtil.NPUPDATE;
			m.obj = currentApiPacket;
			if (activityConnected) {
				try {
					activityMessenger.send(m);
				} catch (RemoteException e) {
					// Whatever...
				}
			}
			notificationManager.updateNotificationWithInfo(currentApiPacket);
		}

	}

	public void updateNotificationImage(Bitmap image) {
		notificationManager.updateNotificationImage(currentApiPacket, image);
	}

}
