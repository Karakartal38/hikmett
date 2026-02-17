package com.hadis.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class HadisRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new HadisRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}
