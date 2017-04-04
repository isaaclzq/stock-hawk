package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.udacity.stockhawk.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Isaac on 3/31/17.
 */

public class DetailStockActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    private static final String YEARFORMAT = "yyyy";
    private static final String DATEFORMAT = "dd/MM/yyyy";

    @BindView(R.id.line_chart)
    LineChart mChart;
    @BindView(R.id.title_view)
    TextView mTitle;

    private String mSymbol;
    private LinkedList<Entry> mHistoryList;
    private LinkedList<String> mHistroyDate;
    private LineDataSet mLineDataSet;

    private float LINE_WIDTH = 1.5f;
    private float WORD_SIZE = 6f;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail_stock);
        ButterKnife.bind(this);

        retrieveData();

        mLineDataSet = new LineDataSet(mHistoryList, "Stock");
        mLineDataSet.setLineWidth(LINE_WIDTH);
        mLineDataSet.setValueTextSize(WORD_SIZE);


        mChart.setOnChartValueSelectedListener(this);

        mChart.getDescription().setEnabled(true);

        mChart.setTouchEnabled(true);

        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        mChart.setPinchZoom(true);

        mChart.setBackgroundColor(getResources().getColor(R.color.blue_gray_700));
        mChart.getDescription().setText(String.format("%s in 2017", mSymbol));
        mChart.getDescription().setTextColor(Color.WHITE);



        LineData data = new LineData(mLineDataSet);
        data.setValueTextColor(Color.RED);
        mChart.setData(data);

        mChart.invalidate();
        mTitle.setText(mSymbol);

    }

    private void retrieveData() {
        Intent received = getIntent();
        SimpleDateFormat sdf = new SimpleDateFormat(YEARFORMAT);
        Date today = new Date();

        mHistoryList = new LinkedList<>();
        mHistroyDate = new LinkedList<>();

        String[] tmpHistory;
        if (null != received) {
            mSymbol = received.getStringExtra(getString(R.string.intent_stock_key));
            tmpHistory = received.getStringExtra(getString(R.string.intent_stock_history))
                                .split("\n");
            int x = 0;
            for (int i = 0; i < tmpHistory.length; i++) {
                String history = tmpHistory[tmpHistory.length-1-i];
                String[] tmp = history.split(",");

                Date date = new Date(Long.parseLong(tmp[0]));
                String price = tmp[1];
                String formatedDate = sdf.format(date);

                if (!formatedDate.equals(sdf.format(today))) {
                    continue;
                }

                mHistroyDate.offer((new SimpleDateFormat(DATEFORMAT)).format(date));
                mHistoryList.offer(new Entry(x++, Float.parseFloat(price)));
            }
        }
    }


    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
}
