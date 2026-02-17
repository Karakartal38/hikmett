package com.hadis.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.SystemClock;
import android.widget.RemoteViews;

public class HadisWidgetProvider extends AppWidgetProvider {

    private static final String ACTION_NEXT = "com.hadis.widget.ACTION_NEXT";
    private static final String PREFS = "hadis_prefs";
    private static final String KEY_INDEX = "current_index";

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) {
            updateWidget(context, manager, id);
        }
        setAlarm(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_NEXT.equals(intent.getAction())) {
            nextHadis(context);
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        setAlarm(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        cancelAlarm(context);
    }

    private void nextHadis(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int old = prefs.getInt(KEY_INDEX, 0);
        int next = HadisData.getRandomIndex();
        while (next == old && HadisData.getCount() > 1) {
            next = HadisData.getRandomIndex();
        }
        prefs.edit().putInt(KEY_INDEX, next).apply();

        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, HadisWidgetProvider.class));
        manager.notifyAppWidgetViewDataChanged(ids, R.id.hadis_list);
        for (int id : ids) {
            updateWidget(context, manager, id);
        }
    }

    private void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int index = prefs.getInt(KEY_INDEX, 0);
        if (index >= HadisData.getCount()) {
            index = 0;
            prefs.edit().putInt(KEY_INDEX, 0).apply();
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        // Set up ListView with RemoteViewsService
        Intent serviceIntent = new Intent(context, HadisRemoteViewsService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.hadis_list, serviceIntent);

        // Update badge, source, counter, narrator
        views.setTextViewText(R.id.badge, HadisData.getBadgeText(index));
        views.setTextViewText(R.id.source, HadisData.getSource(index));
        views.setTextViewText(R.id.ravi, HadisData.getNarrator(index));
        views.setTextViewText(R.id.counter, (index + 1) + " / " + HadisData.getCount());

        // Set badge color based on category
        int badgeColor = HadisData.getBadgeColor(index);
        views.setTextColor(R.id.badge, badgeColor);

        // Set badge background based on category
        String cat = HadisData.getCategory(index);
        if (cat.equals("YG")) {
            views.setInt(R.id.badge, "setBackgroundResource", R.drawable.badge_ayet);
        } else if (cat.equals("SKIP_HD")) {
            views.setInt(R.id.badge, "setBackgroundResource", R.drawable.badge_hadis);
        } else if (cat.equals("KK")) {
            views.setInt(R.id.badge, "setBackgroundResource", R.drawable.badge_risale);
        } else if (cat.equals("SKIP_DU")) {
            views.setInt(R.id.badge, "setBackgroundResource", R.drawable.badge_dua);
        } else {
            views.setInt(R.id.badge, "setBackgroundResource", R.drawable.badge_hikmet);
        }

        // Next button
        Intent intent = new Intent(context, HadisWidgetProvider.class);
        intent.setAction(ACTION_NEXT);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.btn_next, pi);

        manager.updateAppWidget(widgetId, views);
        manager.notifyAppWidgetViewDataChanged(widgetId, R.id.hadis_list);
    }

    private void setAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, HadisWidgetProvider.class);
        intent.setAction(ACTION_NEXT);
        PendingIntent pi = PendingIntent.getBroadcast(context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 300000, 300000, pi);
    }

    private void cancelAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, HadisWidgetProvider.class);
        intent.setAction(ACTION_NEXT);
        PendingIntent pi = PendingIntent.getBroadcast(context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.cancel(pi);
    }
}
