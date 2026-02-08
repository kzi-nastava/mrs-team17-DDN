package com.example.taximobile.feature.user.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.taximobile.R;
import com.example.taximobile.core.auth.JwtUtils;
import com.example.taximobile.core.network.TokenStorage;
import com.example.taximobile.feature.user.data.RideOrderRepository;
import com.example.taximobile.feature.user.data.UserLookupRepository;
import com.example.taximobile.feature.user.data.UserProfileRepository;
import com.example.taximobile.feature.user.data.dto.request.CreateRideRequestDto;
import com.example.taximobile.feature.user.data.dto.request.RidePointRequestDto;
import com.example.taximobile.feature.user.data.dto.request.RoutePreviewRequestDto;
import com.example.taximobile.feature.user.data.dto.response.LatLngDto;
import com.example.taximobile.feature.user.data.dto.response.RoutePreviewResponseDto;
import com.example.taximobile.feature.user.data.dto.response.UserLookupStatusResponseDto;

import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class UserOrderRideActivity extends UserBaseActivity {

    private MapView map;

    private TextView tvMapHint;
    private TextView tvPreview;

    private TextView tvBlocked;
    private TextView tvError;
    private TextView tvSuccess;

    private RadioGroup rgOrderType;
    private RadioButton rbNow;
    private RadioButton rbSchedule;

    private TextView tvScheduledAt;
    private Spinner spVehicleType;
    private CheckBox cbBaby;
    private CheckBox cbPet;

    private TextView tvFrom;
    private TextView tvTo;

    private Button btnResetPoints;
    private Button btnRemoveLastCheckpoint;

    private LinearLayout listCheckpoints;

    private EditText etLinkedUserEmail;
    private Button btnAddLinkedUser;
    private LinearLayout listLinkedUsers;

    private Button btnCreateRide;
    private ProgressBar progressCreate;

    private RideOrderRepository rideRepo;
    private UserLookupRepository lookupRepo;
    private UserProfileRepository profileRepo;

    private RidePointRequestDto startPoint = null;
    private RidePointRequestDto destPoint = null;
    private final List<RidePointRequestDto> checkpoints = new ArrayList<>();

    private Marker startMarker;
    private Marker destMarker;
    private final List<Marker> checkpointMarkers = new ArrayList<>();
    private Polyline routeLine;

    private final List<LinkedUserEntry> linkedUsers = new ArrayList<>();
    private int guestCounter = 1;

    private boolean isSubmitting = false;

    private boolean requesterBlocked = false;
    private String requesterBlockReason = null;

    private String serverBlockMsg = null;

    private Date scheduledDate = null;

    private final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable previewRunnable;

    private static class LinkedUserEntry {
        String display;
        String payload;
        boolean ok;
        boolean pending;
        String issue;

        LinkedUserEntry(String display, String payload) {
            this.display = display;
            this.payload = payload;
            this.ok = true;
            this.pending = false;
            this.issue = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflateContent(R.layout.activity_user_order_ride);
        toolbar.setTitle(getString(R.string.title_order_ride));

        Configuration.getInstance().setUserAgentValue(getPackageName());

        rideRepo = new RideOrderRepository(this);
        lookupRepo = new UserLookupRepository(this);
        profileRepo = new UserProfileRepository(this);

        bindViews(v);
        initVehicleSpinner();
        initMap();
        initListeners();

        loadRequesterBlockStatus();
        updateCreateButtonState();
        updatePointViews();
        renderCheckpoints();
        renderLinkedUsers();
    }

    private void bindViews(View v) {
        map = v.findViewById(R.id.orderMap);
        tvMapHint = v.findViewById(R.id.tvMapHint);
        tvPreview = v.findViewById(R.id.tvPreview);

        tvBlocked = v.findViewById(R.id.tvBlocked);
        tvError = v.findViewById(R.id.tvError);
        tvSuccess = v.findViewById(R.id.tvSuccess);

        rgOrderType = v.findViewById(R.id.rgOrderType);
        rbNow = v.findViewById(R.id.rbNow);
        rbSchedule = v.findViewById(R.id.rbSchedule);

        tvScheduledAt = v.findViewById(R.id.tvScheduledAt);
        spVehicleType = v.findViewById(R.id.spVehicleType);
        cbBaby = v.findViewById(R.id.cbBaby);
        cbPet = v.findViewById(R.id.cbPet);

        tvFrom = v.findViewById(R.id.tvFrom);
        tvTo = v.findViewById(R.id.tvTo);

        btnResetPoints = v.findViewById(R.id.btnResetPoints);
        btnRemoveLastCheckpoint = v.findViewById(R.id.btnRemoveLastCheckpoint);

        listCheckpoints = v.findViewById(R.id.listCheckpoints);

        etLinkedUserEmail = v.findViewById(R.id.etLinkedUserEmail);
        btnAddLinkedUser = v.findViewById(R.id.btnAddLinkedUser);
        listLinkedUsers = v.findViewById(R.id.listLinkedUsers);

        btnCreateRide = v.findViewById(R.id.btnCreateRide);
        progressCreate = v.findViewById(R.id.progressCreateRide);
    }

    private void initVehicleSpinner() {
        List<String> items = new ArrayList<>();
        items.add(getString(R.string.vehicle_standard));
        items.add(getString(R.string.vehicle_luxury));
        items.add(getString(R.string.vehicle_van));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item_user,
                items
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_user);
        spVehicleType.setAdapter(adapter);
        spVehicleType.setSelection(0);
    }

    private void initMap() {
        map.setMultiTouchControls(true);
        map.getController().setZoom(16.0);
        map.getController().setCenter(new GeoPoint(45.2671, 19.8335));

        MapEventsReceiver r = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                onMapTap(p);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };

        map.getOverlays().add(new MapEventsOverlay(r));
        map.invalidate();
    }

    private void clearServerBlock() {
        serverBlockMsg = null;
    }

    private void initListeners() {
        rgOrderType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbNow) {
                scheduledDate = null;
                tvScheduledAt.setText(getString(R.string.order_pick_datetime));
                tvScheduledAt.setEnabled(false);
                tvScheduledAt.setAlpha(0.6f);
            } else {
                tvScheduledAt.setEnabled(true);
                tvScheduledAt.setAlpha(1f);
            }
            clearServerBlock();
            clearError();
            updateCreateButtonState();
        });

        tvScheduledAt.setEnabled(false);
        tvScheduledAt.setAlpha(0.6f);
        tvScheduledAt.setOnClickListener(v -> {
            if (!rbSchedule.isChecked()) return;
            pickScheduleDateTime();
        });

        btnResetPoints.setOnClickListener(v -> resetPoints());
        btnRemoveLastCheckpoint.setOnClickListener(v -> removeLastCheckpoint());

        btnAddLinkedUser.setOnClickListener(v -> addLinkedUser());
        btnCreateRide.setOnClickListener(v -> submitRide());

        previewRunnable = () -> doPreviewRoute();
    }

    private void loadRequesterBlockStatus() {
        String token = new TokenStorage(this).getToken();
        Long userId = token == null ? null : JwtUtils.getUserIdFromSub(token);

        if (userId == null) {
            requesterBlocked = false;
            requesterBlockReason = null;
            updateBlockedBanner();
            return;
        }

        profileRepo.getProfile(userId, new UserProfileRepository.ProfileCb() {
            @Override
            public void onSuccess(com.example.taximobile.feature.user.data.dto.response.UserProfileResponseDto dto) {
                requesterBlocked = dto != null && dto.isBlocked();
                requesterBlockReason = dto != null ? dto.getBlockReason() : null;
                updateBlockedBanner();
                updateCreateButtonState();
            }

            @Override
            public void onError(String msg, int httpCode) {
                requesterBlocked = false;
                requesterBlockReason = null;
                updateBlockedBanner();
                updateCreateButtonState();
            }
        });
    }

    private void onMapTap(GeoPoint p) {
        clearServerBlock();
        clearError();
        clearSuccess();

        if (startPoint == null) {
            setStart(p);
            return;
        }

        if (destPoint == null) {
            setDestination(p);
            return;
        }

        addCheckpoint(p);
    }

    private void setStart(GeoPoint p) {
        startPoint = new RidePointRequestDto(getString(R.string.order_selected_location), p.getLatitude(), p.getLongitude());
        startMarker = placeMarker(startMarker, p, R.drawable.ic_marker_pickup, getString(R.string.order_pickup));
        tvMapHint.setText(getString(R.string.order_map_hint_destination));
        updatePointViews();
        triggerPreviewDebounced();
        reverseGeocodeAsync(startPoint);
    }

    private void setDestination(GeoPoint p) {
        destPoint = new RidePointRequestDto(getString(R.string.order_selected_location), p.getLatitude(), p.getLongitude());
        destMarker = placeMarker(destMarker, p, R.drawable.ic_marker_destination, getString(R.string.order_destination));
        tvMapHint.setText(getString(R.string.order_map_hint_checkpoint));
        updatePointViews();
        triggerPreviewDebounced();
        reverseGeocodeAsync(destPoint);
    }

    private void addCheckpoint(GeoPoint p) {
        RidePointRequestDto cp = new RidePointRequestDto(getString(R.string.order_selected_location), p.getLatitude(), p.getLongitude());
        checkpoints.add(cp);

        Marker m = new Marker(map);
        m.setPosition(p);
        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        m.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_marker_checkpoint));
        m.setTitle(getString(R.string.order_checkpoint) + " " + checkpoints.size());
        map.getOverlays().add(m);
        checkpointMarkers.add(m);

        map.invalidate();

        renderCheckpoints();
        updatePointViews();
        triggerPreviewDebounced();
        reverseGeocodeAsync(cp);
    }

    private Marker placeMarker(Marker existing, GeoPoint p, int iconRes, String title) {
        if (existing != null) {
            try { map.getOverlays().remove(existing); } catch (Exception ignore) {}
        }

        Marker m = new Marker(map);
        m.setPosition(p);
        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        m.setIcon(ContextCompat.getDrawable(this, iconRes));
        m.setTitle(title);
        map.getOverlays().add(m);
        map.invalidate();
        return m;
    }

    private void resetPoints() {
        clearServerBlock();

        startPoint = null;
        destPoint = null;
        checkpoints.clear();

        if (startMarker != null) map.getOverlays().remove(startMarker);
        if (destMarker != null) map.getOverlays().remove(destMarker);
        startMarker = null;
        destMarker = null;

        for (Marker m : checkpointMarkers) {
            try { map.getOverlays().remove(m); } catch (Exception ignore) {}
        }
        checkpointMarkers.clear();

        if (routeLine != null) {
            try { map.getOverlays().remove(routeLine); } catch (Exception ignore) {}
            routeLine = null;
        }

        tvMapHint.setText(getString(R.string.order_map_hint_pickup));
        tvPreview.setVisibility(View.GONE);

        updatePointViews();
        renderCheckpoints();
        triggerPreviewDebounced();
        updateCreateButtonState();
        map.invalidate();
    }

    private void removeLastCheckpoint() {
        clearServerBlock();

        if (checkpoints.isEmpty()) {
            Toast.makeText(this, getString(R.string.order_no_checkpoints), Toast.LENGTH_SHORT).show();
            return;
        }

        int lastIdx = checkpoints.size() - 1;
        checkpoints.remove(lastIdx);

        Marker m = checkpointMarkers.remove(lastIdx);
        try { map.getOverlays().remove(m); } catch (Exception ignore) {}

        for (int i = 0; i < checkpointMarkers.size(); i++) {
            checkpointMarkers.get(i).setTitle(getString(R.string.order_checkpoint) + " " + (i + 1));
        }

        renderCheckpoints();
        triggerPreviewDebounced();
        updateCreateButtonState();
        map.invalidate();
    }

    private void updatePointViews() {
        String fromText = (startPoint == null)
                ? getString(R.string.order_from_placeholder)
                : (getString(R.string.order_from_prefix) + " " + safe(startPoint.getAddress()));

        String toText = (destPoint == null)
                ? getString(R.string.order_to_placeholder)
                : (getString(R.string.order_to_prefix) + " " + safe(destPoint.getAddress()));

        tvFrom.setText(fromText);
        tvTo.setText(toText);

        updateCreateButtonState();
    }

    private void renderCheckpoints() {
        listCheckpoints.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < checkpoints.size(); i++) {
            final int idx = i;
            RidePointRequestDto cp = checkpoints.get(i);

            View row = inflater.inflate(R.layout.item_order_list_row, listCheckpoints, false);
            TextView tv = row.findViewById(R.id.tvRowText);
            TextView btnRemove = row.findViewById(R.id.btnRemove);

            String label = getString(R.string.order_checkpoint) + " " + (i + 1) + ": " + safe(cp.getAddress());
            tv.setText(label);

            btnRemove.setOnClickListener(v -> removeCheckpointAt(idx));

            listCheckpoints.addView(row);
        }
    }

    private void removeCheckpointAt(int idx) {
        clearServerBlock();

        if (idx < 0 || idx >= checkpoints.size()) return;

        checkpoints.remove(idx);

        Marker m = checkpointMarkers.remove(idx);
        try { map.getOverlays().remove(m); } catch (Exception ignore) {}

        for (int i = 0; i < checkpointMarkers.size(); i++) {
            checkpointMarkers.get(i).setTitle(getString(R.string.order_checkpoint) + " " + (i + 1));
        }

        renderCheckpoints();
        triggerPreviewDebounced();
        updateCreateButtonState();
        map.invalidate();
    }

    private void addLinkedUser() {
        clearServerBlock();
        clearError();
        clearSuccess();

        String email = (etLinkedUserEmail.getText() != null) ? etLinkedUserEmail.getText().toString().trim() : "";
        if (email.isEmpty()) {
            LinkedUserEntry e = new LinkedUserEntry(getString(R.string.order_guest) + " " + (guestCounter++), "");
            linkedUsers.add(e);
            renderLinkedUsers();
            updateBlockedBanner();
            updateCreateButtonState();
            return;
        }

        if (containsEmail(email)) {
            Toast.makeText(this, getString(R.string.order_duplicate_user), Toast.LENGTH_SHORT).show();
            return;
        }

        LinkedUserEntry entry = new LinkedUserEntry(email, email);
        entry.pending = true;
        entry.ok = false;
        entry.display = email + " (" + getString(R.string.order_checking) + ")";
        linkedUsers.add(entry);

        etLinkedUserEmail.setText("");
        renderLinkedUsers();
        updateCreateButtonState();

        lookupRepo.lookupByEmail(email, new UserLookupRepository.LookupCb() {
            @Override
            public void onSuccess(UserLookupStatusResponseDto dto) {
                entry.pending = false;

                if (dto == null || !dto.isExists()) {
                    entry.payload = "";
                    entry.ok = true;
                    entry.issue = null;
                    entry.display = email + " (" + getString(R.string.order_not_found_guest) + ")";
                } else if (!dto.isActive()) {
                    entry.ok = false;
                    entry.issue = getString(R.string.order_linked_inactive, email);
                    entry.display = email + " (" + getString(R.string.order_inactive) + ")";
                } else if (dto.isBlocked()) {
                    String reason = dto.getBlockReason();
                    if (reason == null || reason.trim().isEmpty()) {
                        entry.issue = getString(R.string.order_linked_blocked, email);
                    } else {
                        entry.issue = getString(R.string.order_linked_blocked_reason, email, reason);
                    }
                    entry.ok = false;
                    entry.display = email + " (" + getString(R.string.order_blocked_short) + ")";
                } else {
                    entry.ok = true;
                    entry.issue = null;
                    entry.display = email;
                }

                renderLinkedUsers();
                updateBlockedBanner();
                updateCreateButtonState();
            }

            @Override
            public void onError(String msg, int httpCode) {
                entry.pending = false;
                entry.ok = true;
                entry.issue = null;
                entry.display = email;

                renderLinkedUsers();
                updateBlockedBanner();
                updateCreateButtonState();
            }
        });
    }

    private void renderLinkedUsers() {
        listLinkedUsers.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < linkedUsers.size(); i++) {
            final int idx = i;
            LinkedUserEntry e = linkedUsers.get(i);

            View row = inflater.inflate(R.layout.item_order_list_row, listLinkedUsers, false);
            TextView tv = row.findViewById(R.id.tvRowText);
            TextView btnRemove = row.findViewById(R.id.btnRemove);

            tv.setText(e.display);
            btnRemove.setOnClickListener(v -> {
                clearServerBlock();
                linkedUsers.remove(idx);
                renderLinkedUsers();
                updateBlockedBanner();
                updateCreateButtonState();
            });

            listLinkedUsers.addView(row);
        }
    }

    private boolean containsEmail(String email) {
        String lower = email.trim().toLowerCase();
        for (LinkedUserEntry e : linkedUsers) {
            if (e.payload != null && !e.payload.trim().isEmpty() && e.payload.trim().toLowerCase().equals(lower)) {
                return true;
            }
        }
        return false;
    }

    private void submitRide() {
        clearError();
        clearSuccess();

        if (isSubmitting) return;

        if (serverBlockMsg != null) {
            updateBlockedBanner();
            return;
        }
        if (requesterBlocked) {
            updateBlockedBanner();
            return;
        }
        LinkedUserEntry bad = firstBadLinkedUser();
        if (bad != null) {
            updateBlockedBanner();
            return;
        }

        if (startPoint == null || destPoint == null) {
            showError(getString(R.string.order_points_required));
            return;
        }

        String scheduleErr = validateSchedule();
        if (scheduleErr != null) {
            showError(scheduleErr);
            return;
        }

        CreateRideRequestDto req = buildRequest();
        setSubmitting(true);

        rideRepo.createRide(req, new RideOrderRepository.CreateCb() {
            @Override
            public void onSuccess(com.example.taximobile.feature.user.data.dto.response.CreateRideResponseDto dto) {
                setSubmitting(false);

                if (dto != null && dto.getPrice() != null) {
                    showSuccess(getString(R.string.order_created_price, dto.getPrice()));
                } else {
                    showSuccess(getString(R.string.order_created_ok_no_id));
                }

                resetPoints();
                linkedUsers.clear();
                renderLinkedUsers();
                updateBlockedBanner();
                updateCreateButtonState();
            }

            @Override
            public void onError(RideOrderRepository.ApiError err) {
                setSubmitting(false);

                if (err != null) {
                    if (err.httpCode == 403) {
                        String msg = err.message != null ? err.message : getString(R.string.order_failed);
                        serverBlockMsg = msg;
                        updateBlockedBanner();
                        showError(msg);
                        updateCreateButtonState();
                        return;
                    }
                    showError(err.message != null ? err.message : getString(R.string.order_failed));
                } else {
                    showError(getString(R.string.order_failed));
                }
            }
        });
    }

    private CreateRideRequestDto buildRequest() {
        CreateRideRequestDto req = new CreateRideRequestDto();

        boolean schedule = rbSchedule.isChecked();

        req.setOrderType(schedule ? "schedule" : "now");
        req.setScheduledAt(schedule ? toIsoOffsetString(scheduledDate) : null);

        req.setStart(startPoint);
        req.setDestination(destPoint);

        req.setCheckpoints(checkpoints.isEmpty() ? null : new ArrayList<>(checkpoints));

        List<String> linked = new ArrayList<>();
        for (LinkedUserEntry e : linkedUsers) {
            linked.add(e.payload == null ? "" : e.payload);
        }
        req.setLinkedUsers(linked);

        req.setVehicleType(vehicleTypePayload());
        req.setBabyTransport(cbBaby.isChecked());
        req.setPetTransport(cbPet.isChecked());

        return req;
    }

    private String vehicleTypePayload() {
        int idx = spVehicleType.getSelectedItemPosition();
        if (idx == 1) return "luxury";
        if (idx == 2) return "van";
        return "standard";
    }

    private void setSubmitting(boolean submitting) {
        isSubmitting = submitting;
        progressCreate.setVisibility(submitting ? View.VISIBLE : View.GONE);
        btnCreateRide.setEnabled(!submitting);
        btnCreateRide.setAlpha(submitting ? 0.6f : 1f);
        updateCreateButtonState();
    }

    private void updateCreateButtonState() {
        boolean scheduleOk = validateSchedule() == null;
        boolean pointsOk = startPoint != null && destPoint != null;

        boolean linkedOk = firstBadLinkedUser() == null;
        boolean anyPending = anyLinkedUserPending();

        boolean can = !isSubmitting
                && serverBlockMsg == null
                && !requesterBlocked
                && linkedOk
                && !anyPending
                && pointsOk
                && scheduleOk;

        btnCreateRide.setEnabled(can);
        btnCreateRide.setAlpha(can ? 1f : 0.65f);
        updateBlockedBanner();
    }

    private boolean anyLinkedUserPending() {
        for (LinkedUserEntry e : linkedUsers) {
            if (e.pending) return true;
        }
        return false;
    }

    private LinkedUserEntry firstBadLinkedUser() {
        for (LinkedUserEntry e : linkedUsers) {
            if (e.pending) return e;
            if (!e.ok) return e;
        }
        return null;
    }

    private void updateBlockedBanner() {
        if (requesterBlocked) {
            String msg = getString(R.string.order_you_are_blocked);
            if (requesterBlockReason != null && !requesterBlockReason.trim().isEmpty()) {
                msg = msg + "\n" + getString(R.string.order_block_reason_prefix) + " " + requesterBlockReason.trim();
            }
            tvBlocked.setText(msg);
            tvBlocked.setVisibility(View.VISIBLE);
            return;
        }

        LinkedUserEntry bad = firstBadLinkedUser();
        if (bad != null && bad.issue != null && !bad.issue.trim().isEmpty()) {
            tvBlocked.setText(bad.issue);
            tvBlocked.setVisibility(View.VISIBLE);
            return;
        } else if (bad != null && bad.pending) {
            tvBlocked.setText(getString(R.string.order_checking_users));
            tvBlocked.setVisibility(View.VISIBLE);
            return;
        }

        if (serverBlockMsg != null && !serverBlockMsg.trim().isEmpty()) {
            tvBlocked.setText(serverBlockMsg);
            tvBlocked.setVisibility(View.VISIBLE);
            return;
        }

        tvBlocked.setVisibility(View.GONE);
    }

    private String validateSchedule() {
        if (!rbSchedule.isChecked()) return null;

        if (scheduledDate == null) {
            return getString(R.string.order_schedule_pick_required);
        }

        Date now = new Date();
        if (scheduledDate.before(now)) {
            return getString(R.string.order_schedule_must_be_future);
        }

        Calendar tmp = Calendar.getInstance();
        tmp.setTime(now);
        tmp.add(Calendar.HOUR_OF_DAY, 5);
        Date max = tmp.getTime();

        if (scheduledDate.after(max)) {
            return getString(R.string.order_schedule_max_5h);
        }
        return null;
    }

    private void pickScheduleDateTime() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dp = new DatePickerDialog(this, (view, y, m, d) -> {
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog tp = new TimePickerDialog(this, (timeView, h, min) -> {
                Calendar chosen = Calendar.getInstance();
                chosen.set(Calendar.YEAR, y);
                chosen.set(Calendar.MONTH, m);
                chosen.set(Calendar.DAY_OF_MONTH, d);
                chosen.set(Calendar.HOUR_OF_DAY, h);
                chosen.set(Calendar.MINUTE, min);
                chosen.set(Calendar.SECOND, 0);
                chosen.set(Calendar.MILLISECOND, 0);

                scheduledDate = chosen.getTime();

                clearServerBlock();

                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                tvScheduledAt.setText(fmt.format(scheduledDate));

                clearError();
                updateCreateButtonState();

            }, hour, minute, true);

            tp.show();

        }, year, month, day);

        dp.show();
    }

    private void triggerPreviewDebounced() {
        handler.removeCallbacks(previewRunnable);
        handler.postDelayed(previewRunnable, 300);
    }

    private void doPreviewRoute() {
        if (startPoint == null || destPoint == null) {
            clearRouteLine();
            return;
        }

        List<LatLngDto> pts = new ArrayList<>();
        pts.add(toLatLng(startPoint));
        for (RidePointRequestDto cp : checkpoints) {
            pts.add(toLatLng(cp));
        }
        pts.add(toLatLng(destPoint));

        RoutePreviewRequestDto req = new RoutePreviewRequestDto(pts);

        rideRepo.previewRoute(req, new RideOrderRepository.PreviewCb() {
            @Override
            public void onSuccess(RoutePreviewResponseDto dto) {
                if (dto == null || dto.getRoute() == null || dto.getRoute().size() < 2) {
                    clearRouteLine();
                    return;
                }

                drawRouteLine(dto.getRoute());

                if (dto.getEtaMinutes() != null && dto.getDistanceKm() != null) {
                    String txt = getString(R.string.order_preview_fmt, dto.getEtaMinutes(), dto.getDistanceKm());
                    tvPreview.setText(txt);
                    tvPreview.setVisibility(View.VISIBLE);
                } else {
                    tvPreview.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String msg, int httpCode) {
                clearRouteLine();
            }
        });
    }

    private void drawRouteLine(List<LatLngDto> route) {
        if (routeLine != null) {
            try { map.getOverlays().remove(routeLine); } catch (Exception ignore) {}
            routeLine = null;
        }

        Polyline pl = new Polyline();
        List<GeoPoint> points = new ArrayList<>();
        for (LatLngDto p : route) {
            if (p == null) continue;
            points.add(new GeoPoint(p.getLat(), p.getLng()));
        }
        pl.setPoints(points);
        pl.setWidth(10f);
        pl.setColor(ContextCompat.getColor(this, R.color.green));
        routeLine = pl;

        map.getOverlays().add(routeLine);
        map.invalidate();
    }

    private void clearRouteLine() {
        if (routeLine != null) {
            try { map.getOverlays().remove(routeLine); } catch (Exception ignore) {}
            routeLine = null;
            map.invalidate();
        }
        tvPreview.setVisibility(View.GONE);
    }

    private LatLngDto toLatLng(RidePointRequestDto p) {
        LatLngDto dto = new LatLngDto();
        dto.setLat(p.getLat() != null ? p.getLat() : 0.0);
        dto.setLng(p.getLng() != null ? p.getLng() : 0.0);
        return dto;
    }

    private void reverseGeocodeAsync(RidePointRequestDto point) {
        if (point == null) return;

        double lat = point.getLat() != null ? point.getLat() : 0.0;
        double lng = point.getLng() != null ? point.getLng() : 0.0;

        new Thread(() -> {
            try {
                String addr = reverseGeocode(lat, lng);
                if (addr == null || addr.trim().isEmpty()) return;

                runOnUiThread(() -> {
                    point.setAddress(addr);
                    updatePointViews();
                    renderCheckpoints();
                });

            } catch (Exception ignore) {
            }
        }).start();
    }

    private String reverseGeocode(double lat, double lng) throws Exception {
        String urlStr = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=" + lat + "&lon=" + lng;

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(7000);
        conn.setReadTimeout(7000);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", getPackageName());

        int code = conn.getResponseCode();
        if (code != 200) {
            try { conn.disconnect(); } catch (Exception ignore) {}
            return null;
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        conn.disconnect();

        String json = sb.toString();
        JSONObject obj = new JSONObject(json);
        String display = obj.optString("display_name", null);
        if (display != null && !display.trim().isEmpty()) {
            return display.trim();
        }
        return null;
    }

    private String toIsoOffsetString(Date d) {
        if (d == null) return null;
        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
        iso.setTimeZone(TimeZone.getDefault());
        return iso.format(d);
    }

    private void showError(String msg) {
        if (msg == null) msg = getString(R.string.order_failed);
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }

    private void clearError() {
        tvError.setVisibility(View.GONE);
        tvError.setText("");
    }

    private void showSuccess(String msg) {
        tvSuccess.setText(msg);
        tvSuccess.setVisibility(View.VISIBLE);
    }

    private void clearSuccess() {
        tvSuccess.setVisibility(View.GONE);
        tvSuccess.setText("");
    }

    private String safe(String s) {
        if (s == null) return "";
        String t = s.trim();
        return t.isEmpty() ? "" : t;
    }
}
