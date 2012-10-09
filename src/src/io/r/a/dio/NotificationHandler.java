package io.r.a.dio;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class NotificationHandler {
	private Context context;
	private Bitmap currentImage;
	public final static int CONSTANTNOTIFICATION = 111;

	public NotificationHandler(Context context) {
		this.context = context;
	}

	public Notification constantNotification() {
		Notification n = new Notification.Builder(context)
				.setContentTitle("R/a/dio")
				.setContentText(" ")
				.setSmallIcon(R.drawable.ic_launcher)
				.setLargeIcon(
						BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher))
				.getNotification();
		return n;
	}

	public void updateNotificationImage(ApiPacket currentPacket, Bitmap image) {
		String ns = Context.NOTIFICATION_SERVICE;
		currentImage = image;
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
		Notification n = new Notification.Builder(context)
				.setContentTitle("R/a/dio" + " (" + currentPacket.dj + ")")
				.setContentText(currentPacket.np).setSmallIcon(R.drawable.ic_launcher)
				.setLargeIcon(image).getNotification();
		mNotificationManager.notify(CONSTANTNOTIFICATION, n);
	}

	public void updateNotificationWithInfo(ApiPacket currentPacket) {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
		Notification n = new Notification.Builder(context)
				.setContentTitle("R/a/dio" + " (" + currentPacket.dj + ")")
				.setContentText(currentPacket.np)
				.setSmallIcon(R.drawable.ic_launcher)
				.setLargeIcon(
						currentImage != null ? currentImage : BitmapFactory.decodeResource(
								context.getResources(), R.drawable.ic_launcher)).getNotification();
		mNotificationManager.notify(CONSTANTNOTIFICATION, n);
	}
}
