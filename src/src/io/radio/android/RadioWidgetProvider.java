package io.radio.android;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.widget.RemoteViews;

public class RadioWidgetProvider extends AppWidgetProvider {
    public static RemoteViews views;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            updateWidget(context, appWidgetManager, false, null, 0, 0);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        updateWidget(context, AppWidgetManager.getInstance(context), false, null, 0, 0);
    }

    public static void updateWidget(Context context, AppWidgetManager appWidgetManager, boolean updateInfo, String title
            , int songLength, int songProgress) {
        if (views == null) {
            views = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);
        }

        Intent intent = new Intent();
        if (RadioService.currentlyPlaying) {
            views.setImageViewResource(R.id.widget_icon, R.drawable.ic_launcher);
            intent.setAction("stop");
        } else {
            views.setImageViewResource(R.id.widget_icon, R.drawable.ic_launcher_disabled);
            intent.setAction("restart");
        }
        if (updateInfo) {
            views.setTextViewText(R.id.widget_NowPlaying, Html.fromHtml(title));
            views.setProgressBar(R.id.widget_ProgressBar,
                    songLength, songProgress, false);
            views.setTextViewText(R.id.widget_SongLength, (songLength == -1 && songProgress == -1) ? "" : ApiUtil
                    .intTimeDurationToString(songLength));
            views.setTextViewText(R.id.widget_SongElapsed, (songLength == -1 && songProgress == -1) ? "" : ApiUtil
                    .intTimeDurationToString(songProgress));
        }

        PendingIntent pendingIntent;
        if (!RadioService.isRunning && !RadioService.currentlyPlaying) {
            intent = new Intent(context, MainActivity.class);
            pendingIntent = PendingIntent.getActivity(
                    context.getApplicationContext(), 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            pendingIntent = PendingIntent.getBroadcast(
                    context.getApplicationContext(), 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
        views.setOnClickPendingIntent(R.id.widget_icon, pendingIntent);

        ComponentName thisWidget = new ComponentName(context,
                RadioWidgetProvider.class);
        appWidgetManager.updateAppWidget(thisWidget,
                views);
    }

}