package com.udacity.stockhawk.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.MainActivity;

/**
 * Created by Isaac on 4/1/17.
 */

public class TodayWidgetIntentService extends IntentService {

    public TodayWidgetIntentService() {
        super("TodayWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String symbol, price, change;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, StockWidgetProvider.class));

        Cursor data = getContentResolver().query(Contract.Quote.URI,
                                        Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                                        null, null, Contract.Quote.COLUMN_SYMBOL);

        if (data == null) {
            return;
        }

        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        for (int appWidgeId : appWidgetIds) {
            int layoutId = R.layout.widget_today_small;
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            if (!data.moveToNext()) {
                data.moveToFirst();
            }

            symbol = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_SYMBOL));
            price = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_PRICE));
            change = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_ABSOLUTE_CHANGE));

            views.setTextViewText(R.id.stock_symbol, symbol);
            views.setTextViewText(R.id.widge_price, price);
            views.setTextViewText(R.id.widget_change, change);

            if (Float.parseFloat(change) > 0) {
                views.setInt(R.id.widget_change, "setBackgroundResource", R.drawable.percent_change_pill_green);
            } else {
                views.setInt(R.id.widget_change, "setBackgroundResource", R.drawable.percent_change_pill_red);
            }

            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgeId, views);
        }

        data.close();
    }

    private void setupViews(RemoteViews views, Cursor cursor) {
        if (!cursor.moveToNext()) {
            cursor.moveToFirst();
            setupViews(views, cursor);
        } else {
            views.setTextViewText(R.id.stock_symbol, cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL)));
            views.setTextViewText(R.id.widge_price, cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_PRICE)));
            views.setTextViewText(R.id.widget_change, cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_PERCENTAGE_CHANGE)));
            Log.v("widget", cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL)) + " "
                        + cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_PRICE)) + " "
                        + cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_PERCENTAGE_CHANGE)) + " ");
        }
    }
}
