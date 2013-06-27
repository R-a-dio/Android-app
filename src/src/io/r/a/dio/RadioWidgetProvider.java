package io.r.a.dio;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class RadioWidgetProvider extends AppWidgetProvider {
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		final int N = appWidgetIds.length;

		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];
			Intent intent = new Intent();
			intent.setAction("restart");

			PendingIntent pendingIntent = PendingIntent.getBroadcast(
					context.getApplicationContext(), 0, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);

			RemoteViews views;

			views = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout);

			views.setOnClickPendingIntent(R.id.widget_PlayButton, pendingIntent);

			intent.setAction("stop");

			pendingIntent = PendingIntent.getBroadcast(
					context.getApplicationContext(), 0, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			views.setOnClickPendingIntent(R.id.widget_PauseButton, pendingIntent);
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}

}
