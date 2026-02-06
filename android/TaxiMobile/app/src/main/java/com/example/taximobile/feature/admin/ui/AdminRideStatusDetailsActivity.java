package com.example.taximobile.feature.admin.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.taximobile.R;

import java.util.Locale;

public class AdminRideStatusDetailsActivity extends AdminBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflateContent(R.layout.activity_admin_ride_status_details);

        long rideId = getIntent().getLongExtra("rideId", -1);
        toolbar.setTitle("Ride #" + rideId);

        TextView tvDriver = v.findViewById(R.id.tvDriver);
        TextView tvStatus = v.findViewById(R.id.tvStatus);
        TextView tvStart = v.findViewById(R.id.tvStart);
        TextView tvLoc = v.findViewById(R.id.tvLoc);
        Button btnMaps = v.findViewById(R.id.btnMaps);

        String fn = getIntent().getStringExtra("driverFirstName");
        String ln = getIntent().getStringExtra("driverLastName");
        String email = getIntent().getStringExtra("driverEmail");

        String name = ((fn != null ? fn : "") + " " + (ln != null ? ln : "")).trim();
        if (name.isEmpty()) name = email != null ? email : "-";

        String status = getIntent().getStringExtra("status");
        String startedAt = getIntent().getStringExtra("startedAt");
        double lat = getIntent().getDoubleExtra("carLat", Double.NaN);
        double lng = getIntent().getDoubleExtra("carLng", Double.NaN);

        tvDriver.setText("Driver: " + name);
        tvStatus.setText("Status: " + (status == null || status.isEmpty() ? "-" : status));
        tvStart.setText("Started at: " + (startedAt == null || startedAt.isEmpty() ? "-" : startedAt));

        if (!Double.isNaN(lat) && !Double.isNaN(lng)) {
            tvLoc.setText(String.format(Locale.US, "Car location: %.6f, %.6f", lat, lng));
            btnMaps.setEnabled(true);
            btnMaps.setOnClickListener(x -> openMaps(lat, lng));
        } else {
            tvLoc.setText("Car location: -");
            btnMaps.setEnabled(false);
        }
    }

    private void openMaps(double lat, double lng) {
        Uri uri = Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng);
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(i);
    }
}
