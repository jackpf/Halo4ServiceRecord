package com.jackpf.halo4servicerecord2;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

import com.jackpf.halo4servicerecord2.R;

public class MainWidget extends AppWidgetProvider
{
	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        RemoteViews remoteViews;
        ComponentName widget;

        remoteViews = new RemoteViews(context.getPackageName(), R.layout.halo4servicerecord_appwidget);
        widget = new ComponentName(context, MainWidget.class);
        
        remoteViews.setTextViewText(R.id.widget_gamertag, "Not implemented yet");
        
        //File spartan = new File(context.getCacheDir(), "zEldaRRR");
        //remoteViews.setImageViewUri(R.id.widget_spartan_image, Uri.fromFile(spartan));
        
        appWidgetManager.updateAppWidget(widget, remoteViews);
    }
}