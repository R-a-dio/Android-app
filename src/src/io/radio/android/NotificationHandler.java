package io.radio.android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.text.Html;


public class NotificationHandler {
	private Context context;
	private Bitmap currentImage;
	public final static int CONSTANTNOTIFICATION = 111;

	public NotificationHandler(Context context) {
		this.context = context;
	}

    private NotificationCompat.Builder baseNotification() {
        Intent notifyIntent = new Intent(Intent.ACTION_MAIN);
        notifyIntent.setClass(context, MainActivity.class);
        PendingIntent openActivityIntent = PendingIntent.getActivity(context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playIntent = new Intent();
        playIntent.setAction("restart");

        PendingIntent playPendingIntent = PendingIntent.getBroadcast(
                context.getApplicationContext(), 0, playIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stopIntent = new Intent();
        stopIntent.setAction("stop");

        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(
                context.getApplicationContext(), 0, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);



        return new NotificationCompat.Builder(context)
                .setContentTitle("R/a/dio")
                .setContentText(" ")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(openActivityIntent)
                .setOngoing(true)
                .addAction(R.drawable.ic_media_play, "Play", playPendingIntent)
                .addAction(R.drawable.ic_media_stop, "Stop", stopPendingIntent)
                .setLargeIcon(
                        BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));
    }

    public Notification constantNotification() {
        Notification n = baseNotification().build();
        return n;
    }

    public void newSongTickerNotification(String songName, String artistName) {
        String ns = Context.NOTIFICATION_SERVICE;
        Notification n = baseNotification().setTicker("Now playing: " + songName + " - " + artistName).build();
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
        mNotificationManager.notify(CONSTANTNOTIFICATION, n);
    }

    public void newDjTickerNotification(DJ dj) {
        String ns = Context.NOTIFICATION_SERVICE;
        Notification n = baseNotification().setTicker(dj.name + " is now streaming!").build();
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
        mNotificationManager.notify(CONSTANTNOTIFICATION, n);
    }

	public void updateNotificationImage(ApiPacket currentPacket, Bitmap image) {

        String ns = Context.NOTIFICATION_SERVICE;
		currentImage = image;
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
		Notification n = baseNotification()
				.setContentTitle("R/a/dio" + " (" + currentPacket.main.dj.name + ")")
				.setContentText(Html.fromHtml(currentPacket.main.metadata)).setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(image).build();
		mNotificationManager.notify(CONSTANTNOTIFICATION, n);
	}

	public void updateNotificationWithInfo(ApiPacket currentPacket) {
        String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
		Notification n = baseNotification()
				.setContentTitle("R/a/dio" + " (" + currentPacket.main.dj.name + ")")
				.setContentText(Html.fromHtml(currentPacket.main.metadata))
                        .setLargeIcon(
                                currentImage != null ? currentImage : BitmapFactory.decodeResource(
                                        context.getResources(), R.drawable.ic_launcher))
                        .build();
		mNotificationManager.notify(CONSTANTNOTIFICATION, n);
	}
}