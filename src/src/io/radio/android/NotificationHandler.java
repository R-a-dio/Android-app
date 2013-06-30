package io.radio.android;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.RemoteViews;


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
        PendingIntent intent = PendingIntent.getActivity(context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(context)
                .setContentTitle("R/a/dio")
                .setContentText(" ")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(intent)
                .addAction(R.drawable.ic_media_play, "Play", null)
                .addAction(R.drawable.ic_media_stop, "Stop", null)
                .setLargeIcon(
                        BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));
    }

    public Notification constantNotification() {
        Notification n = baseNotification().build();
        return n;
    }

	public void updateNotificationImage(ApiPacket currentPacket, Bitmap image) {

        String ns = Context.NOTIFICATION_SERVICE;
		currentImage = image;
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
		Notification n = baseNotification()
				.setContentTitle("R/a/dio" + " (" + currentPacket.dj + ")")
				.setContentText(currentPacket.np).setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(image).build();
		mNotificationManager.notify(CONSTANTNOTIFICATION, n);
	}

	public void updateNotificationWithInfo(ApiPacket currentPacket) {

        String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
		Notification n = baseNotification()
				.setContentTitle("R/a/dio" + " (" + currentPacket.dj + ")")
				.setContentText(currentPacket.np)
				.setLargeIcon(
						currentImage != null ? currentImage : BitmapFactory.decodeResource(
								context.getResources(), R.drawable.ic_launcher)).build();
		mNotificationManager.notify(CONSTANTNOTIFICATION, n);
	}
}