package com.udacity.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static com.udacity.stockhawk.data.PrefUtils.STOCK_STATUS_INTERNET_DOWN;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener,
        StockAdapter.StockAdapterOnClickHandler,
        SharedPreferences.OnSharedPreferenceChangeListener{

    private static final int STOCK_LOADER = 0;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.recycler_view)
    RecyclerView stockRecyclerView;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.error)
    TextView error;
    private StockAdapter adapter;

    @Override
    public void onClick(String symbol) {

        Intent intent = new Intent(MainActivity.this, DetailStockActivity.class);
        intent.putExtra(getString(R.string.intent_stock_key), symbol);
        intent.putExtra(getString(R.string.intent_stock_history), adapter.getHistory(symbol));
        startActivity(intent);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        adapter = new StockAdapter(this, this);
        stockRecyclerView.setAdapter(adapter);
        stockRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);

        PrefUtils.setStockStatus(this, PrefUtils.STOCK_STATUS_OK);
        Log.v("zizi", "change to ok");
        PreferenceManager.getDefaultSharedPreferences(this)
                        .registerOnSharedPreferenceChangeListener(this);

        onRefresh();

        QuoteSyncJob.initialize(this);
        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String symbol = adapter.getSymbolAtPosition(viewHolder.getAdapterPosition());
                PrefUtils.removeStock(MainActivity.this, symbol);
                getContentResolver().delete(Contract.Quote.makeUriForStock(symbol), null, null);
            }
        }).attachToRecyclerView(stockRecyclerView);


    }

    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onRefresh() {

        QuoteSyncJob.syncImmediately(this);

        if (!networkUp() && adapter.getItemCount() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            PrefUtils.setStockStatus(this, PrefUtils.STOCK_STATUS_INTERNET_DOWN);
        } else if (!networkUp()) {
            swipeRefreshLayout.setRefreshing(false);
            PrefUtils.setStockStatus(this, PrefUtils.STOCK_STATUS_STOCK_OUTUPDATED);
        } else if (PrefUtils.getStocks(this).size() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            PrefUtils.setStockStatus(this, PrefUtils.STOCK_STATUS_NO_STOCKS);
        } else {
            error.setVisibility(View.INVISIBLE);
            PrefUtils.setStockStatus(this, PrefUtils.STOCK_STATUS_OK);
        }
    }

    // onClick listener for floating button
    public void button(@SuppressWarnings("UnusedParameters") View view) {
        new AddStockDialog().show(getFragmentManager(), "StockDialogFragment");
    }

    void addStock(final String symbol) {
        if (symbol != null && !symbol.isEmpty()) {

            if (networkUp()) {
                new AsyncTask<String, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(String... params) {
                        String symbol = params[0];
                        return PrefUtils.isValidStock(symbol);
                    }

                    @Override
                    protected void onPostExecute(Boolean isValid) {
                        super.onPostExecute(isValid);
                        if (!isValid) {
                            PrefUtils.setStockStatus(MainActivity.this, PrefUtils.STOCK_STATUS_INVALID_STOCK);
                        } else {
                            swipeRefreshLayout.setRefreshing(true);
                            PrefUtils.addStock(MainActivity.this, symbol);
                            QuoteSyncJob.syncImmediately(MainActivity.this);
                        }
                    }
                }.execute(symbol);
            } else {
//                String message = getString(R.string.toast_stock_added_no_connectivity, symbol);
                String message = getString(R.string.toast_stock_cannot_be_added, symbol);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        swipeRefreshLayout.setRefreshing(false);

        if (data.getCount() != 0) {
            error.setVisibility(GONE);
        }
        adapter.setCursor(data);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        swipeRefreshLayout.setRefreshing(false);
        adapter.setCursor(null);
    }


    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (PrefUtils.getDisplayMode(this)
                .equals(getString(R.string.pref_display_mode_absolute_key))) {
            item.setIcon(R.drawable.ic_percentage);
        } else {
            item.setIcon(R.drawable.ic_dollar);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_settings, menu);
        MenuItem item = menu.findItem(R.id.action_change_units);
        setDisplayModeMenuItemIcon(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_change_units) {
            PrefUtils.toggleDisplayMode(this);
            setDisplayModeMenuItemIcon(item);
            adapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_stock_status_key))) {
            updateEmptyViewOrToast();
        }
    }

    private void updateEmptyViewOrToast() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        @PrefUtils.StockStatus int status = sp.getInt(getString(R.string.pref_stock_status_key),
                                                        PrefUtils.STOCK_STATUS_UNKNOWN);

        switch (status) {
            case PrefUtils.STOCK_STATUS_OK:
                error.setVisibility(GONE);
                break;
            case STOCK_STATUS_INTERNET_DOWN:
                error.setText(getString(R.string.error_no_network));
                error.setVisibility(View.VISIBLE);
                break;
            case PrefUtils.STOCK_STATUS_STOCK_OUTUPDATED:
                error.setText(getString(R.string.stock_out_of_dated));
                error.setVisibility(View.VISIBLE);
                break;
            case PrefUtils.STOCK_STATUS_NO_STOCKS:
                error.setText(getString(R.string.error_no_stocks));
                error.setVisibility(View.VISIBLE);
                break;
            case PrefUtils.STOCK_STATUS_UNKNOWN:
                Toast.makeText(this,
                        R.string.stock_unknown_error,
                        Toast.LENGTH_SHORT).show();
                PrefUtils.resetStockStatus(this);
                break;
            case PrefUtils.STOCK_STATUS_INVALID_STOCK:
                Toast.makeText(this,
                        R.string.stock_invalid_symbol,
                        Toast.LENGTH_SHORT).show();
                PrefUtils.resetStockStatus(this);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

}
