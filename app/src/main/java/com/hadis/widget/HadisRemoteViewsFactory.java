package com.hadis.widget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class HadisRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context context;
    private String text = "";

    public HadisRemoteViewsFactory(Context context, Intent intent) {
        this.context = context;
    }

    @Override
    public void onCreate() {}

    @Override
    public void onDataSetChanged() {
        SharedPreferences prefs = context.getSharedPreferences("hadis_prefs", Context.MODE_PRIVATE);
        int index = prefs.getInt("current_index", 0);
        if (index >= HadisData.getCount()) index = 0;
        text = HadisData.getText(index);
    }

    @Override
    public void onDestroy() {}

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_item);
        views.setTextViewText(R.id.item_text, text);
        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
