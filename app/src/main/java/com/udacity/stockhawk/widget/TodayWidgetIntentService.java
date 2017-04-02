package com.udacity.stockhawk.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.MainActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Isaac on 4/1/17.
 */

public class TodayWidgetIntentService extends IntentService {

    private DecimalFormat dollarFormatWithPlus;
    private DecimalFormat dollarFormat;

    public TodayWidgetIntentService() {
        super("TodayWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String symbol, price;
        float change;

        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");

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

            symbol = data.getString(Contract.Quote.POSITION_SYMBOL);
            price = data.getString(Contract.Quote.POSITION_PRICE);
            change = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);

            if (change > 0) {
                views.setInt(R.id.widget_change, "setBackgroundResource", R.drawable.percent_change_pill_green);
            } else {
                views.setInt(R.id.widget_change, "setBackgroundResource", R.drawable.percent_change_pill_red);
            }

            views.setTextViewText(R.id.stock_symbol, symbol);
            views.setTextViewText(R.id.widge_price, price);
            views.setTextViewText(R.id.widget_change, dollarFormatWithPlus.format(change));
            
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgeId, views);
        }

        data.close();
    }
}
