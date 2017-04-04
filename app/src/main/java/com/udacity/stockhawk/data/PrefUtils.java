package com.udacity.stockhawk.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;

import com.udacity.stockhawk.R;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public final class PrefUtils {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STOCK_STATUS_OK,
            STOCK_STATUS_STOCK_OUTUPDATED,
            STOCK_STATUS_UNKNOWN,
            STOCK_STATUS_INTERNET_DOWN,
            STOCK_STATUS_NO_STOCKS,
            STOCK_STATUS_INVALID_STOCK})

    public @interface StockStatus {}

    public static final int STOCK_STATUS_OK = 0;
    public static final int STOCK_STATUS_STOCK_OUTUPDATED = 1;
    public static final int STOCK_STATUS_UNKNOWN = 2;
    public static final int STOCK_STATUS_INTERNET_DOWN = 3;
    public static final int STOCK_STATUS_NO_STOCKS = 4;
    public static final int STOCK_STATUS_INVALID_STOCK= 5;

    private PrefUtils() {
    }

    public static Set<String> getStocks(Context context) {
        String stocksKey = context.getString(R.string.pref_stocks_key);
        String initializedKey = context.getString(R.string.pref_stocks_initialized_key);
        String[] defaultStocksList = context.getResources().getStringArray(R.array.default_stocks);

        HashSet<String> defaultStocks = new HashSet<>(Arrays.asList(defaultStocksList));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);


        boolean initialized = prefs.getBoolean(initializedKey, false);

        // check if the app has ran before by checking if default stocks have been
        // initialized.
        if (!initialized) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(initializedKey, true);
            editor.putStringSet(stocksKey, defaultStocks);
            editor.apply();
            return defaultStocks;
        }
        return prefs.getStringSet(stocksKey, new HashSet<String>());

    }

    // add or remove symbal from the sets in SharePreference
    private static void editStockPref(Context context, String symbol, Boolean add) {
        String key = context.getString(R.string.pref_stocks_key);
        Set<String> stocks = getStocks(context);

        if (add) {
            stocks.add(symbol);
        } else {
            stocks.remove(symbol);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(key, stocks);
        editor.apply();
    }

    public static void addStock(Context context, String symbol) {
        editStockPref(context, symbol, true);
    }

    public static void removeStock(Context context, String symbol) {
        editStockPref(context, symbol, false);
    }

    // displays the changes in percentage
    public static String getDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String defaultValue = context.getString(R.string.pref_display_mode_default);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, defaultValue);
    }

    // switch the display mode
    public static void toggleDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String absoluteKey = context.getString(R.string.pref_display_mode_absolute_key);
        String percentageKey = context.getString(R.string.pref_display_mode_percentage_key);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String displayMode = getDisplayMode(context);

        SharedPreferences.Editor editor = prefs.edit();

        if (displayMode.equals(absoluteKey)) {
            editor.putString(key, percentageKey);
        } else {
            editor.putString(key, absoluteKey);
        }

        editor.apply();
    }

    public static void setStockStatus (Context context, @PrefUtils.StockStatus int status) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(context.getString(R.string.pref_stock_status_key), status);
        spe.commit();
    }

    public static boolean isValidStock (String symbol){
        boolean isValid = false;
        Stock stockPair = null;
        if (!symbol.matches("[a-zA-Z.? ]*")) {
            return isValid;
        }
        try {
            stockPair = YahooFinance.get(symbol);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (stockPair == null) {
            return isValid;
        } else {
            isValid = stockPair.getQuote().getPrice() != null;
        }
        return isValid;
    }


    public static void resetStockStatus (Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(context.getString(R.string.pref_stock_status_key), PrefUtils.STOCK_STATUS_OK);
        spe.commit();
    }
}
