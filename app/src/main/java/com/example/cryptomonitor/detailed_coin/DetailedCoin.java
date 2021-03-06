package com.example.cryptomonitor.detailed_coin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.cryptomonitor.R;
import com.example.cryptomonitor.database.coins.CoinInfo;
import com.example.cryptomonitor.home.CoinAdapterHome;
import com.example.cryptomonitor.model_cryptocompare.model_chart.ChartData;
import com.example.cryptomonitor.model_cryptocompare.model_chart.ModelChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;

import static com.example.cryptomonitor.Utilities.formatToMillion;

public class DetailedCoin extends AppCompatActivity implements View.OnClickListener {
    public static final String EXTRA_INDEX_KEY = "INDEX";
    public static final String EXTRA_POSITION_KEY = "POSITION";
    public static final String THEME = "theme";
    private LineChart lineChart;
    private String mIndex;
    private String mCurrency;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private TextView full_name;
    private TextView price;
    private TextView change;
    private TextView supply;
    private TextView mkt;
    private TextView volume;
    private TextView total_volume;
    private TextView high;
    private TextView low;
    private ImageView infoURL;
    private ImageView icon;
    private TextView textView_1D;
    private TextView textView_1W;
    private TextView textView_1M;
    private TextView textView_3M;
    private DetailedViewModel mDetailedViewModel;
    private Observer<ModelChart> modelChartObserver = this::setChartData;
    private Observer<CoinInfo> coinInfoObserver = this::bindViews;
    private Observer<Integer> coinSelectedObserver = integer -> {
        switch (integer) {
            case R.id.detailed_1D:
                textView_1D.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_text_view_selected));
                textView_1W.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_text_view));
                textView_1M.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_text_view));
                textView_3M.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_text_view));
                textView_1D.setClickable(false);
                textView_1W.setClickable(true);
                textView_1M.setClickable(true);
                textView_3M.setClickable(true);
                break;
            case R.id.detailed_1W:
                textView_1W.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_text_view_selected));
                textView_1D.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_text_view));
                textView_1M.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_text_view));
                textView_3M.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_text_view));
                textView_1W.setClickable(false);
                textView_1D.setClickable(true);
                textView_1M.setClickable(true);
                textView_3M.setClickable(true);
                break;
            case R.id.detailed_1M:
                textView_1M.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_text_view_selected));
                textView_1D.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_text_view));
                textView_1W.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_text_view));
                textView_3M.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_text_view));
                textView_1M.setClickable(false);
                textView_1D.setClickable(true);
                textView_1W.setClickable(true);
                textView_3M.setClickable(true);
                break;
            case R.id.detailed_3M:
                textView_3M.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_text_view_selected));
                textView_1D.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_text_view));
                textView_1W.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_text_view));
                textView_1M.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_text_view));
                textView_3M.setClickable(false);
                textView_1D.setClickable(true);
                textView_1W.setClickable(true);
                textView_1M.setClickable(true);
                break;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences mPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        Boolean mTheme = mPreferences.getBoolean(THEME, false);
        if (mTheme.equals(true)) {
            setTheme(R.style.AppThemeDarkPurple);
        } else {
            setTheme(R.style.AppThemeDark);
        }
        getTheme().applyStyle(R.style.DetailedTheme, true);
        setContentView(R.layout.activity_detailed_coin);
        full_name = findViewById(R.id.detailed_fullname);
        price = findViewById(R.id.detailed_price);
        change = findViewById(R.id.detailed_change);
        mkt = findViewById(R.id.detailed_mkt);
        supply = findViewById(R.id.detailed_SPLY);
        volume = findViewById(R.id.detailed_volume);
        total_volume = findViewById(R.id.detailed_total_volume);
        TextView rank = findViewById(R.id.detailed_RANK);
        high = findViewById(R.id.detailed_high);
        low = findViewById(R.id.detailed_low);
        infoURL = findViewById(R.id.detailed_infoURL);
        ImageView backButton = findViewById(R.id.detailed_back);
        backButton.setOnClickListener(v -> {
            finish();
        });
        icon = findViewById(R.id.detailed_icon);
        textView_1D = findViewById(R.id.detailed_1D);
        textView_1D.setOnClickListener(this);
        textView_1W = findViewById(R.id.detailed_1W);
        textView_1W.setOnClickListener(this);
        textView_1M = findViewById(R.id.detailed_1M);
        textView_1M.setOnClickListener(this);
        textView_3M = findViewById(R.id.detailed_3M);
        textView_3M.setOnClickListener(this);

        mCurrency = PreferenceManager.getDefaultSharedPreferences(this).getString("currency", "USD");
        Intent intent = getIntent();
        if (intent != null) {
            mIndex = intent.getStringExtra(EXTRA_INDEX_KEY);
            int position = 0;
            String mRank = "#" + intent.getIntExtra(EXTRA_POSITION_KEY, position);
            rank.setText(mRank);
            initChart();
            Log.e("Detailed", mIndex + " " + mCurrency);
        }

        mDetailedViewModel = ViewModelProviders.of(this).get(DetailedViewModel.class);
        mDetailedViewModel.getChartLiveData().observe(this, modelChartObserver);
        mDetailedViewModel.getCoinLiveData(mIndex).observe(this, coinInfoObserver);
        mDetailedViewModel.getSelectedCoinLiveData().observe(this, coinSelectedObserver);
        if (savedInstanceState == null)
            mDetailedViewModel.setChartLiveData(mIndex, mCurrency);
    }

    private void initChart() {
        lineChart = findViewById(R.id.chart);
        lineChart.setTouchEnabled(false);
        lineChart.setDragEnabled(false);
        lineChart.setScaleYEnabled(false);
        lineChart.setScaleXEnabled(true);
        lineChart.getLegend().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
    }

    private void setChartData(ModelChart modelChart) {
        ArrayList<Entry> dataVal1 = new ArrayList<Entry>();
        List<ChartData> coinData = modelChart.getData();
        List<String> dateXvalues = new ArrayList<>();
        int i = 0;
        for (ChartData data : coinData) {
            dataVal1.add(new Entry((float) i, data.getOpen().floatValue()));
            Date date = new Date();
            date.setTime(modelChart.getData().get(i).getTime().longValue() * 1000);
            SimpleDateFormat sm = new SimpleDateFormat("MMM-d", Locale.US);
            String strDate = sm.format(date);
            dateXvalues.add(strDate);
            i++;
        }
        LineDataSet set = new LineDataSet(dataVal1, "");
        set.setDrawFilled(true);
        set.setColor(Color.parseColor("#ffffff"));
        set.setFillColor(Color.parseColor("#007ff2"));
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set);
        LineData data = new LineData(dataSets);
        XAxis xAxis = lineChart.getXAxis();
        YAxis yAxisLeft = lineChart.getAxisLeft();
        YAxis yAxisRight = lineChart.getAxisRight();
        xAxis.setTextColor(Color.parseColor("#c7c7c7"));
        yAxisLeft.setTextColor(Color.parseColor("#c7c7c7"));
        yAxisRight.setTextColor(Color.parseColor("#c7c7c7"));
        xAxis.setLabelCount(6, true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        ValueFormatter valueFormatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return dateXvalues.get((int) value);
            }
        };
        xAxis.setValueFormatter(valueFormatter);
        lineChart.animateXY(2000, 2000);
        lineChart.setData(data);
        lineChart.invalidate();
    }

    private void bindViews(CoinInfo coinInfo) {
        full_name.setText(coinInfo.getFullName());
        Picasso.get()
                .load(coinInfo.getImageURL())
                .transform(new CoinAdapterHome.PicassoCircleTransformation())
                .into(icon);
        price.setText(coinInfo.getPriceDisplay());
        setChangeColor(coinInfo.getChangeDay());
        String changeConcat = coinInfo.getChangeDayDispaly() + " (" + coinInfo.getChangePctDay() + "%)";
        change.setText(changeConcat);
        mkt.setText(coinInfo.getMktcap());
        supply.setText(formatToMillion(coinInfo.getSupply()));
        volume.setText(formatToMillion(coinInfo.getVolume()));
        total_volume.setText(coinInfo.getTotalVolume24hTo());
        high.setText(coinInfo.getHigh());
        low.setText(coinInfo.getLow());
        infoURL.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(coinInfo.getInfoURL()));
            startActivity(browserIntent);
        });
    }

    private void setChangeColor(Double d) {
        if (d > 0)
            change.setTextColor(ContextCompat.getColor(this, R.color.green1_dark_theme));
        else if (d < 0)
            change.setTextColor(ContextCompat.getColor(this, R.color.red1));
        else if (d == 0)
            change.setTextColor(Color.GRAY);
    }

    @NonNull
    @Override
    protected void onDestroy() {
        mDetailedViewModel = null;
        lineChart = null;
        mCompositeDisposable.dispose();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.detailed_1D:
                mDetailedViewModel.setChartLiveData(mIndex, mCurrency, v.getId());
                break;
            case R.id.detailed_1W:
                mDetailedViewModel.setChartLiveData(mIndex, mCurrency, v.getId());
                break;
            case R.id.detailed_1M:
                mDetailedViewModel.setChartLiveData(mIndex, mCurrency, v.getId());
                break;
            case R.id.detailed_3M:
                mDetailedViewModel.setChartLiveData(mIndex, mCurrency, v.getId());
                break;
        }
    }
}
