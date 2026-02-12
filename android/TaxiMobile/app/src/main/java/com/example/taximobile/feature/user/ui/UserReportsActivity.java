package com.example.taximobile.feature.user.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.util.Pair;
import androidx.core.content.ContextCompat;

import com.example.taximobile.R;
import com.example.taximobile.feature.user.data.UserReportsRepository;
import com.example.taximobile.feature.user.data.dto.response.RideStatsPointDto;
import com.example.taximobile.feature.user.data.dto.response.RideStatsReportResponseDto;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class UserReportsActivity extends UserBaseActivity {

    private enum Metric { RIDES, KILOMETERS, MONEY }

    private UserReportsRepository repo;

    private EditText etFrom;
    private EditText etTo;
    private Button btnGenerate;
    private ProgressBar progress;
    private TextView tvError;

    private TextView tvTotalRides;
    private TextView tvAvgRidesPerDay;

    private TextView tvTotalKm;
    private TextView tvAvgKmPerDay;

    private TextView tvMoneyLabel;
    private TextView tvTotalMoney;
    private TextView tvAvgMoneyPerDay;

    private TextView tvAvgKmPerRide;
    private TextView tvAvgMoneyPerRide;
    private TextView tvDays;

    private TextView tvChartTitle;
    private Button btnTabRides;
    private Button btnTabKm;
    private Button btnTabMoney;

    private LinearLayout barsContainer;
    private TextView tvChartEmpty;

    private TextView tvFootCumulative;
    private TextView tvFootAverageDay;
    private TextView tvFootUnit;

    private String fromIso;
    private String toIso;

    private RideStatsReportResponseDto report;
    private Metric metric = Metric.RIDES;

    private int barAreaHeightPx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflateContent(R.layout.activity_user_reports);
        toolbar.setTitle(getString(R.string.menu_reports));

        repo = new UserReportsRepository(this);

        etFrom = v.findViewById(R.id.repEtFrom);
        etTo = v.findViewById(R.id.repEtTo);
        btnGenerate = v.findViewById(R.id.repBtnGenerate);
        progress = v.findViewById(R.id.repProgress);
        tvError = v.findViewById(R.id.repError);

        tvTotalRides = v.findViewById(R.id.repTotalRidesValue);
        tvAvgRidesPerDay = v.findViewById(R.id.repTotalRidesAvg);

        tvTotalKm = v.findViewById(R.id.repTotalKmValue);
        tvAvgKmPerDay = v.findViewById(R.id.repTotalKmAvg);

        tvMoneyLabel = v.findViewById(R.id.repMoneyLabel);
        tvTotalMoney = v.findViewById(R.id.repTotalMoneyValue);
        tvAvgMoneyPerDay = v.findViewById(R.id.repTotalMoneyAvg);

        tvAvgKmPerRide = v.findViewById(R.id.repAvgKmPerRide);
        tvAvgMoneyPerRide = v.findViewById(R.id.repAvgMoneyPerRide);
        tvDays = v.findViewById(R.id.repDays);

        tvChartTitle = v.findViewById(R.id.repChartTitle);
        btnTabRides = v.findViewById(R.id.repTabRides);
        btnTabKm = v.findViewById(R.id.repTabKm);
        btnTabMoney = v.findViewById(R.id.repTabMoney);

        barsContainer = v.findViewById(R.id.repBarsContainer);
        tvChartEmpty = v.findViewById(R.id.repChartEmpty);

        tvFootCumulative = v.findViewById(R.id.repFootCumulative);
        tvFootAverageDay = v.findViewById(R.id.repFootAverageDay);
        tvFootUnit = v.findViewById(R.id.repFootUnit);

        barAreaHeightPx = dpToPx(140);

        setupDateRangePicker();
        setupTabs();

        setDefaultLast7Days();
        loadReport();
    }

    private void setupTabs() {
        btnTabRides.setOnClickListener(v -> setMetric(Metric.RIDES));
        btnTabKm.setOnClickListener(v -> setMetric(Metric.KILOMETERS));
        btnTabMoney.setOnClickListener(v -> setMetric(Metric.MONEY));
        updateTabUi();
    }

    private void setMetric(Metric m) {
        metric = m;
        updateTabUi();
        rebuildChart();
    }

    private void updateTabUi() {
        if (metric == Metric.RIDES) {
            btnTabRides.setBackgroundResource(R.drawable.bg_button_primary_user_blue);
            btnTabRides.setTextColor(ContextCompat.getColor(this, R.color.white));

            btnTabKm.setBackgroundResource(R.drawable.bg_button_outline_user_green);
            btnTabKm.setTextColor(ContextCompat.getColor(this, R.color.white));

            btnTabMoney.setBackgroundResource(R.drawable.bg_button_outline_user_orange);
            btnTabMoney.setTextColor(ContextCompat.getColor(this, R.color.white));

        } else if (metric == Metric.KILOMETERS) {
            btnTabRides.setBackgroundResource(R.drawable.bg_button_outline_user_blue);
            btnTabRides.setTextColor(ContextCompat.getColor(this, R.color.white));

            btnTabKm.setBackgroundResource(R.drawable.bg_button_primary_user_green);
            btnTabKm.setTextColor(ContextCompat.getColor(this, R.color.white));

            btnTabMoney.setBackgroundResource(R.drawable.bg_button_outline_user_orange);
            btnTabMoney.setTextColor(ContextCompat.getColor(this, R.color.white));

        } else {
            btnTabRides.setBackgroundResource(R.drawable.bg_button_outline_user_blue);
            btnTabRides.setTextColor(ContextCompat.getColor(this, R.color.white));

            btnTabKm.setBackgroundResource(R.drawable.bg_button_outline_user_green);
            btnTabKm.setTextColor(ContextCompat.getColor(this, R.color.white));

            btnTabMoney.setBackgroundResource(R.drawable.bg_button_primary_user_orange);
            btnTabMoney.setTextColor(ContextCompat.getColor(this, R.color.white));
        }

        tvChartTitle.setText(getChartTitle());
        tvFootUnit.setText(getChartUnit().isEmpty() ? "" : getString(R.string.report_unit_fmt, getChartUnit()));
    }

    private void setupDateRangePicker() {
        MaterialDatePicker<Pair<Long, Long>> rangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText(R.string.report_pick_range)
                .build();

        View.OnClickListener openPicker = v -> rangePicker.show(getSupportFragmentManager(), "USER_REPORTS_RANGE");

        etFrom.setOnClickListener(openPicker);
        etTo.setOnClickListener(openPicker);

        rangePicker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null) return;

            Long start = selection.first;
            Long end = selection.second;

            if (start != null) {
                etFrom.setText(formatUiDate(start));
                fromIso = formatIsoDate(start);
            }
            if (end != null) {
                etTo.setText(formatUiDate(end));
                toIso = formatIsoDate(end);
            }
        });

        btnGenerate.setOnClickListener(v -> loadReport());
    }

    private void setDefaultLast7Days() {
        Calendar cal = Calendar.getInstance();
        long end = cal.getTimeInMillis();

        cal.add(Calendar.DAY_OF_MONTH, -6);
        long start = cal.getTimeInMillis();

        etFrom.setText(formatUiDate(start));
        etTo.setText(formatUiDate(end));

        fromIso = formatIsoDate(start);
        toIso = formatIsoDate(end);
    }

    private void loadReport() {
        if (fromIso == null || toIso == null || fromIso.isEmpty() || toIso.isEmpty()) {
            tvError.setText(R.string.report_error_choose_dates);
            tvError.setVisibility(View.VISIBLE);
            return;
        }
        if (fromIso.compareTo(toIso) > 0) {
            tvError.setText(R.string.report_error_range);
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        progress.setVisibility(View.VISIBLE);
        btnGenerate.setEnabled(false);
        tvError.setVisibility(View.GONE);

        repo.getMyRideReport(fromIso, toIso, new UserReportsRepository.ReportCb() {
            @Override
            public void onSuccess(RideStatsReportResponseDto res) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    btnGenerate.setEnabled(true);
                    report = res;
                    bindSummary();
                    rebuildChart();
                });
            }

            @Override
            public void onError(String msg) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    btnGenerate.setEnabled(true);
                    report = null;
                    clearUi();
                    tvError.setText(getString(R.string.report_error_failed));
                    tvError.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void clearUi() {
        tvTotalRides.setText("-");
        tvAvgRidesPerDay.setText("-");
        tvTotalKm.setText("-");
        tvAvgKmPerDay.setText("-");
        tvTotalMoney.setText("-");
        tvAvgMoneyPerDay.setText("-");
        tvAvgKmPerRide.setText("-");
        tvAvgMoneyPerRide.setText("-");
        tvDays.setText("-");

        barsContainer.removeAllViews();
        tvChartEmpty.setVisibility(View.VISIBLE);
        tvFootCumulative.setText("-");
        tvFootAverageDay.setText("-");
        tvFootUnit.setText("");
    }

    private void bindSummary() {
        if (report == null || report.getTotals() == null || report.getAverages() == null) {
            clearUi();
            return;
        }

        boolean isPassenger = "PASSENGER".equalsIgnoreCase(report.getTargetRole());

        tvMoneyLabel.setText(isPassenger ? getString(R.string.report_money_spent) : getString(R.string.report_money_earned));

        tvTotalRides.setText(String.valueOf(report.getTotals().getRides()));
        tvAvgRidesPerDay.setText(getString(R.string.report_avg_day_fmt, format2(report.getAverages().getRidesPerDay())));

        tvTotalKm.setText(getString(R.string.report_km_value_fmt, format2(report.getTotals().getKilometers())));
        tvAvgKmPerDay.setText(getString(R.string.report_avg_day_fmt, format2(report.getAverages().getKilometersPerDay())));

        tvTotalMoney.setText(getString(R.string.report_rsd_value_fmt, format2(report.getTotals().getMoney())));
        tvAvgMoneyPerDay.setText(getString(
                R.string.report_avg_day_rsd_fmt,
                (isPassenger ? getString(R.string.report_spent) : getString(R.string.report_earned)),
                format2(report.getAverages().getMoneyPerDay())
        ));

        tvAvgKmPerRide.setText(getString(R.string.report_avg_km_ride_fmt, format2(report.getAverages().getKilometersPerRide())));
        tvAvgMoneyPerRide.setText(getString(
                R.string.report_avg_money_ride_fmt,
                (isPassenger ? getString(R.string.report_spent) : getString(R.string.report_earned)),
                format2(report.getAverages().getMoneyPerRide())
        ));

        tvDays.setText(String.valueOf(report.getDays()));
    }

    private void rebuildChart() {
        if (report == null || report.getPoints() == null || report.getPoints().isEmpty()) {
            barsContainer.removeAllViews();
            tvChartEmpty.setVisibility(View.VISIBLE);
            tvFootCumulative.setText("-");
            tvFootAverageDay.setText("-");
            return;
        }

        List<RideStatsPointDto> points = report.getPoints();
        List<Double> vals = new ArrayList<>();
        for (RideStatsPointDto p : points) {
            vals.add(getMetricValue(p, metric));
        }

        double max = 1.0;
        for (double v : vals) max = Math.max(max, v);

        barsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (RideStatsPointDto p : points) {
            double v = getMetricValue(p, metric);
            int h = (int) Math.max(0, Math.min(barAreaHeightPx, (v / max) * barAreaHeightPx));

            View barItem = inflater.inflate(R.layout.item_report_bar, barsContainer, false);
            TextView tvLabel = barItem.findViewById(R.id.repBarLabel);
            View vBar = barItem.findViewById(R.id.repBar);

            ViewGroup.LayoutParams lp = vBar.getLayoutParams();
            lp.height = h;
            vBar.setLayoutParams(lp);

            if (metric == Metric.RIDES) vBar.setBackgroundResource(R.drawable.bg_button_primary_user_blue);
            else if (metric == Metric.KILOMETERS) vBar.setBackgroundResource(R.drawable.bg_button_primary_user_green);
            else vBar.setBackgroundResource(R.drawable.bg_button_primary_user_orange);

            String dateIso = p.getDate() != null ? p.getDate() : "";
            tvLabel.setText(formatShortDate(dateIso));

            String tooltip = makeTooltip(dateIso, v);
            barItem.setOnClickListener(vw -> Toast.makeText(this, tooltip, Toast.LENGTH_SHORT).show());

            barsContainer.addView(barItem);
        }

        tvChartEmpty.setVisibility(View.GONE);
        bindChartFoot();
    }

    private void bindChartFoot() {
        if (report == null || report.getTotals() == null || report.getAverages() == null) return;

        if (metric == Metric.RIDES) {
            tvFootCumulative.setText(getString(R.string.report_cumulative_fmt, String.valueOf(report.getTotals().getRides())));
            tvFootAverageDay.setText(getString(R.string.report_average_day_fmt, format2(report.getAverages().getRidesPerDay())));
        } else if (metric == Metric.KILOMETERS) {
            tvFootCumulative.setText(getString(R.string.report_cumulative_km_fmt, format2(report.getTotals().getKilometers())));
            tvFootAverageDay.setText(getString(R.string.report_average_day_km_fmt, format2(report.getAverages().getKilometersPerDay())));
        } else {
            tvFootCumulative.setText(getString(R.string.report_cumulative_rsd_fmt, format2(report.getTotals().getMoney())));
            tvFootAverageDay.setText(getString(R.string.report_average_day_rsd_fmt2, format2(report.getAverages().getMoneyPerDay())));
        }
    }

    private double getMetricValue(RideStatsPointDto p, Metric m) {
        if (p == null) return 0.0;
        if (m == Metric.RIDES) return (double) p.getRides();
        if (m == Metric.KILOMETERS) return p.getKilometers();
        return p.getMoney();
    }

    private String makeTooltip(String isoDate, double v) {
        if (metric == Metric.RIDES) return isoDate + ": " + Math.round(v) + " rides";
        if (metric == Metric.KILOMETERS) return isoDate + ": " + format2(v) + " km";
        return isoDate + ": " + format2(v) + " RSD";
    }

    private String getChartTitle() {
        boolean isPassenger = report == null || report.getTargetRole() == null || "PASSENGER".equalsIgnoreCase(report.getTargetRole());
        if (metric == Metric.RIDES) return getString(R.string.report_chart_rides);
        if (metric == Metric.KILOMETERS) return getString(R.string.report_chart_km);
        return isPassenger ? getString(R.string.report_chart_money_spent) : getString(R.string.report_chart_money_earned);
    }

    private String getChartUnit() {
        if (metric == Metric.RIDES) return "";
        if (metric == Metric.KILOMETERS) return "km";
        return "RSD";
    }

    private String format2(double v) {
        return String.format(Locale.US, "%.2f", v);
    }

    private String formatShortDate(String iso) {
        if (iso == null || iso.length() < 10) return iso != null ? iso : "";
        String dd = iso.substring(8, 10);
        String mm = iso.substring(5, 7);
        return dd + "." + mm;
    }

    private String formatUiDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(millis));
    }

    private String formatIsoDate(long millis) {
        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        iso.setTimeZone(TimeZone.getDefault());
        return iso.format(new Date(millis));
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
