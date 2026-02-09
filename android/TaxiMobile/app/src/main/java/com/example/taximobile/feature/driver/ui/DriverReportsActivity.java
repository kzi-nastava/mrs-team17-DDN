package com.example.taximobile.feature.driver.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

import com.example.taximobile.R;
import com.example.taximobile.databinding.ActivityDriverReportsBinding;
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

public class DriverReportsActivity extends DriverBaseActivity {

    private enum Metric { RIDES, KILOMETERS, MONEY }

    private ActivityDriverReportsBinding binding;

    private UserReportsRepository repo;

    private String fromIso;
    private String toIso;

    private RideStatsReportResponseDto report;
    private Metric metric = Metric.RIDES;

    private int barAreaHeightPx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflateContent(R.layout.activity_driver_reports);
        binding = ActivityDriverReportsBinding.bind(v);

        toolbar.setTitle(getString(R.string.menu_reports));

        repo = new UserReportsRepository(this);

        barAreaHeightPx = dpToPx(140);

        setupDateRangePicker();
        setupTabs();

        setDefaultLast7Days();
        loadReport();
    }

    private void setupTabs() {
        binding.repTabRides.setOnClickListener(v -> setMetric(Metric.RIDES));
        binding.repTabKm.setOnClickListener(v -> setMetric(Metric.KILOMETERS));
        binding.repTabMoney.setOnClickListener(v -> setMetric(Metric.MONEY));
        updateTabUi();
    }

    private void setMetric(Metric m) {
        metric = m;
        updateTabUi();
        rebuildChart();
    }

    private void updateTabUi() {
        if (metric == Metric.RIDES) {
            binding.repTabRides.setBackgroundResource(R.drawable.bg_button_primary_user_blue);
            binding.repTabRides.setTextColor(ContextCompat.getColor(this, R.color.white));

            binding.repTabKm.setBackgroundResource(R.drawable.bg_button_outline_user_green);
            binding.repTabKm.setTextColor(ContextCompat.getColor(this, R.color.white));

            binding.repTabMoney.setBackgroundResource(R.drawable.bg_button_outline_user_orange);
            binding.repTabMoney.setTextColor(ContextCompat.getColor(this, R.color.white));

        } else if (metric == Metric.KILOMETERS) {
            binding.repTabRides.setBackgroundResource(R.drawable.bg_button_outline_user_blue);
            binding.repTabRides.setTextColor(ContextCompat.getColor(this, R.color.white));

            binding.repTabKm.setBackgroundResource(R.drawable.bg_button_primary_user_green);
            binding.repTabKm.setTextColor(ContextCompat.getColor(this, R.color.white));

            binding.repTabMoney.setBackgroundResource(R.drawable.bg_button_outline_user_orange);
            binding.repTabMoney.setTextColor(ContextCompat.getColor(this, R.color.white));

        } else {
            binding.repTabRides.setBackgroundResource(R.drawable.bg_button_outline_user_blue);
            binding.repTabRides.setTextColor(ContextCompat.getColor(this, R.color.white));

            binding.repTabKm.setBackgroundResource(R.drawable.bg_button_outline_user_green);
            binding.repTabKm.setTextColor(ContextCompat.getColor(this, R.color.white));

            binding.repTabMoney.setBackgroundResource(R.drawable.bg_button_primary_user_orange);
            binding.repTabMoney.setTextColor(ContextCompat.getColor(this, R.color.white));
        }

        binding.repChartTitle.setText(getChartTitle());
        String unit = getChartUnit();
        binding.repFootUnit.setText(unit.isEmpty() ? "" : getString(R.string.report_unit_fmt, unit));
    }

    private void setupDateRangePicker() {
        MaterialDatePicker<Pair<Long, Long>> rangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText(R.string.report_pick_range)
                .build();

        View.OnClickListener openPicker = v -> rangePicker.show(getSupportFragmentManager(), "DRIVER_REPORTS_RANGE");

        binding.repEtFrom.setOnClickListener(openPicker);
        binding.repEtTo.setOnClickListener(openPicker);

        rangePicker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null) return;

            Long start = selection.first;
            Long end = selection.second;

            if (start != null) {
                binding.repEtFrom.setText(formatUiDate(start));
                fromIso = formatIsoDate(start);
            }
            if (end != null) {
                binding.repEtTo.setText(formatUiDate(end));
                toIso = formatIsoDate(end);
            }
        });

        binding.repBtnGenerate.setOnClickListener(v -> loadReport());
    }

    private void setDefaultLast7Days() {
        Calendar cal = Calendar.getInstance();
        long end = cal.getTimeInMillis();

        cal.add(Calendar.DAY_OF_MONTH, -6);
        long start = cal.getTimeInMillis();

        binding.repEtFrom.setText(formatUiDate(start));
        binding.repEtTo.setText(formatUiDate(end));

        fromIso = formatIsoDate(start);
        toIso = formatIsoDate(end);
    }

    private void loadReport() {
        if (fromIso == null || toIso == null || fromIso.isEmpty() || toIso.isEmpty()) {
            binding.repError.setText(R.string.report_error_choose_dates);
            binding.repError.setVisibility(View.VISIBLE);
            return;
        }
        if (fromIso.compareTo(toIso) > 0) {
            binding.repError.setText(R.string.report_error_range);
            binding.repError.setVisibility(View.VISIBLE);
            return;
        }

        binding.repProgress.setVisibility(View.VISIBLE);
        binding.repBtnGenerate.setEnabled(false);
        binding.repError.setVisibility(View.GONE);

        repo.getMyRideReport(fromIso, toIso, new UserReportsRepository.ReportCb() {
            @Override
            public void onSuccess(RideStatsReportResponseDto res) {
                runOnUiThread(() -> {
                    binding.repProgress.setVisibility(View.GONE);
                    binding.repBtnGenerate.setEnabled(true);
                    report = res;
                    bindSummary();
                    rebuildChart();
                });
            }

            @Override
            public void onError(String msg) {
                runOnUiThread(() -> {
                    binding.repProgress.setVisibility(View.GONE);
                    binding.repBtnGenerate.setEnabled(true);
                    report = null;
                    clearUi();
                    binding.repError.setText(getString(R.string.report_error_failed));
                    binding.repError.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void clearUi() {
        binding.repTotalRidesValue.setText("-");
        binding.repTotalRidesAvg.setText("-");

        binding.repTotalKmValue.setText("-");
        binding.repTotalKmAvg.setText("-");

        binding.repTotalMoneyValue.setText("-");
        binding.repTotalMoneyAvg.setText("-");

        binding.repAvgKmPerRide.setText("-");
        binding.repAvgMoneyPerRide.setText("-");
        binding.repDays.setText("-");

        binding.repBarsContainer.removeAllViews();
        binding.repChartEmpty.setVisibility(View.VISIBLE);

        binding.repFootCumulative.setText("-");
        binding.repFootAverageDay.setText("-");
        binding.repFootUnit.setText("");
    }

    private void bindSummary() {
        if (report == null || report.getTotals() == null || report.getAverages() == null) {
            clearUi();
            return;
        }

        boolean isPassenger = "PASSENGER".equalsIgnoreCase(report.getTargetRole());

        binding.repMoneyLabel.setText(isPassenger ? getString(R.string.report_money_spent)
                : getString(R.string.report_money_earned));

        binding.repTotalRidesValue.setText(String.valueOf(report.getTotals().getRides()));
        binding.repTotalRidesAvg.setText(getString(R.string.report_avg_day_fmt, format2(report.getAverages().getRidesPerDay())));

        binding.repTotalKmValue.setText(getString(R.string.report_km_value_fmt, format2(report.getTotals().getKilometers())));
        binding.repTotalKmAvg.setText(getString(R.string.report_avg_day_fmt, format2(report.getAverages().getKilometersPerDay())));

        binding.repTotalMoneyValue.setText(getString(R.string.report_rsd_value_fmt, format2(report.getTotals().getMoney())));
        binding.repTotalMoneyAvg.setText(getString(
                R.string.report_avg_day_rsd_fmt,
                (isPassenger ? getString(R.string.report_spent) : getString(R.string.report_earned)),
                format2(report.getAverages().getMoneyPerDay())
        ));

        binding.repAvgKmPerRide.setText(getString(R.string.report_avg_km_ride_fmt, format2(report.getAverages().getKilometersPerRide())));
        binding.repAvgMoneyPerRide.setText(getString(
                R.string.report_avg_money_ride_fmt,
                (isPassenger ? getString(R.string.report_spent) : getString(R.string.report_earned)),
                format2(report.getAverages().getMoneyPerRide())
        ));

        binding.repDays.setText(String.valueOf(report.getDays()));
    }

    private void rebuildChart() {
        if (report == null || report.getPoints() == null || report.getPoints().isEmpty()) {
            binding.repBarsContainer.removeAllViews();
            binding.repChartEmpty.setVisibility(View.VISIBLE);
            binding.repFootCumulative.setText("-");
            binding.repFootAverageDay.setText("-");
            return;
        }

        List<RideStatsPointDto> points = report.getPoints();
        List<Double> vals = new ArrayList<>();
        for (RideStatsPointDto p : points) {
            vals.add(getMetricValue(p, metric));
        }

        double max = 1.0;
        for (double v : vals) max = Math.max(max, v);

        binding.repBarsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (RideStatsPointDto p : points) {
            double v = getMetricValue(p, metric);
            int h = (int) Math.max(0, Math.min(barAreaHeightPx, (v / max) * barAreaHeightPx));

            View barItem = inflater.inflate(R.layout.item_report_bar, binding.repBarsContainer, false);

            View vBar = barItem.findViewById(R.id.repBar);
            ViewGroup.LayoutParams lp = vBar.getLayoutParams();
            lp.height = h;
            vBar.setLayoutParams(lp);

            if (metric == Metric.RIDES) vBar.setBackgroundResource(R.drawable.bg_button_primary_user_blue);
            else if (metric == Metric.KILOMETERS) vBar.setBackgroundResource(R.drawable.bg_button_primary_user_green);
            else vBar.setBackgroundResource(R.drawable.bg_button_primary_user_orange);

            String dateIso = p.getDate() != null ? p.getDate() : "";
            ((android.widget.TextView) barItem.findViewById(R.id.repBarLabel)).setText(formatShortDate(dateIso));

            String tooltip = makeTooltip(dateIso, v);
            barItem.setOnClickListener(vw -> Toast.makeText(this, tooltip, Toast.LENGTH_SHORT).show());

            binding.repBarsContainer.addView(barItem);
        }

        binding.repChartEmpty.setVisibility(View.GONE);
        bindChartFoot();
    }

    private void bindChartFoot() {
        if (report == null || report.getTotals() == null || report.getAverages() == null) return;

        if (metric == Metric.RIDES) {
            binding.repFootCumulative.setText(getString(R.string.report_cumulative_fmt, String.valueOf(report.getTotals().getRides())));
            binding.repFootAverageDay.setText(getString(R.string.report_average_day_fmt, format2(report.getAverages().getRidesPerDay())));
        } else if (metric == Metric.KILOMETERS) {
            binding.repFootCumulative.setText(getString(R.string.report_cumulative_km_fmt, format2(report.getTotals().getKilometers())));
            binding.repFootAverageDay.setText(getString(R.string.report_average_day_km_fmt, format2(report.getAverages().getKilometersPerDay())));
        } else {
            binding.repFootCumulative.setText(getString(R.string.report_cumulative_rsd_fmt, format2(report.getTotals().getMoney())));
            binding.repFootAverageDay.setText(getString(R.string.report_average_day_rsd_fmt2, format2(report.getAverages().getMoneyPerDay())));
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
        boolean isPassenger = report == null || report.getTargetRole() == null
                || "PASSENGER".equalsIgnoreCase(report.getTargetRole());

        if (metric == Metric.RIDES) return getString(R.string.report_chart_rides);
        if (metric == Metric.KILOMETERS) return getString(R.string.report_chart_km);
        return isPassenger ? getString(R.string.report_chart_money_spent)
                : getString(R.string.report_chart_money_earned);
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
