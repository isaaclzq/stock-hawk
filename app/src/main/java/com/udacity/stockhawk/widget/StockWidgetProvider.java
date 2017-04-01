package com.udacity.stockhawk.ui;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;

/**
 * Created by Isaac on 3/31/17.
 */

public class StockWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId: appWidgetIds) {
            int layoutId = R.layout.widget_today_small;
            RemoteViews views = new RemoteViews(context.getPackageName(),
                                                layoutId);

            views.setTextViewText(R.id.stock_symbol, "GOOG");
            views.setTextViewText(R.id.widge_price,
                                "800");
            views.setTextViewText(R.id.widget_change,
                                "5%");

            Intent launchIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
