package com.example.taximobile.feature.admin.ui;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.taximobile.R;
import com.example.taximobile.feature.admin.data.AdminCreateDriverRepository;
import com.example.taximobile.feature.admin.data.dto.request.AdminCreateDriverRequestDto;
import com.example.taximobile.feature.admin.data.dto.response.AdminCreateDriverResponseDto;

import java.util.ArrayList;
import java.util.List;

public class AdminCreateDriverActivity extends AdminBaseActivity {

    private EditText etFirstName, etLastName, etAddress, etPhone, etEmail;
    private EditText etVehicleModel, etLicensePlate, etSeats;
    private Spinner spVehicleType;
    private CheckBox cbBaby, cbPet;
    private Button btnCreate;
    private ProgressBar progress;
    private TextView tvError, tvSuccess;

    private AdminCreateDriverRepository repo;

    private boolean isSubmitting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflateContent(R.layout.activity_admin_create_driver);
        toolbar.setTitle(getString(R.string.menu_create_drivers));

        bind(v);
        initVehicleSpinner();

        repo = new AdminCreateDriverRepository(this);

        // defaults (kao web)
        etSeats.setText("4");
        cbBaby.setChecked(false);
        cbPet.setChecked(false);
        spVehicleType.setSelection(0);

        btnCreate.setOnClickListener(x -> submit());
        setSubmitting(false);
    }

    private void bind(View v) {
        etFirstName = v.findViewById(R.id.etFirstName);
        etLastName = v.findViewById(R.id.etLastName);
        etAddress = v.findViewById(R.id.etAddress);
        etPhone = v.findViewById(R.id.etPhone);
        etEmail = v.findViewById(R.id.etEmail);

        etVehicleModel = v.findViewById(R.id.etVehicleModel);
        spVehicleType = v.findViewById(R.id.spVehicleType);
        etLicensePlate = v.findViewById(R.id.etLicensePlate);
        etSeats = v.findViewById(R.id.etSeats);

        cbBaby = v.findViewById(R.id.cbBaby);
        cbPet = v.findViewById(R.id.cbPet);

        btnCreate = v.findViewById(R.id.btnCreateDriver);
        progress = v.findViewById(R.id.progressCreateDriver);

        tvError = v.findViewById(R.id.tvError);
        tvSuccess = v.findViewById(R.id.tvSuccess);
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

    private void submit() {
        showError(null);
        showSuccess(null);

        if (isSubmitting) return;

        String firstName = safe(etFirstName);
        String lastName = safe(etLastName);
        String address = safe(etAddress);
        String phone = safe(etPhone);
        String email = safe(etEmail);

        String vehicleModel = safe(etVehicleModel);
        String licensePlate = safe(etLicensePlate);
        String seatsRaw = safe(etSeats);

        if (isBlank(firstName) || isBlank(lastName) || isBlank(address) || isBlank(phone) || isBlank(email)
                || isBlank(vehicleModel) || isBlank(licensePlate) || isBlank(seatsRaw)) {
            showError(getString(R.string.admin_create_driver_error_required_fields));
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(getString(R.string.login_error_invalid_email));
            return;
        }

        int seats;
        try {
            seats = Integer.parseInt(seatsRaw);
        } catch (Exception e) {
            showError(getString(R.string.admin_create_driver_error_invalid_seats));
            return;
        }

        if (seats < 1 || seats > 9) {
            showError(getString(R.string.admin_create_driver_error_invalid_seats));
            return;
        }

        AdminCreateDriverRequestDto req = new AdminCreateDriverRequestDto();
        req.setFirstName(firstName);
        req.setLastName(lastName);
        req.setAddress(address);
        req.setPhoneNumber(phone);
        req.setEmail(email);

        req.setVehicleModel(vehicleModel);
        req.setVehicleType(vehicleTypePayload());
        req.setLicensePlate(licensePlate);
        req.setSeats(seats);

        req.setBabyTransport(cbBaby.isChecked());
        req.setPetTransport(cbPet.isChecked());

        setSubmitting(true);

        repo.createDriver(req, new AdminCreateDriverRepository.Cb() {
            @Override
            public void onSuccess(AdminCreateDriverResponseDto dto) {
                runOnUiThread(() -> {
                    setSubmitting(false);

                    String msg = getString(
                            R.string.admin_create_driver_success,
                            dto.getEmail() != null ? dto.getEmail() : email,
                            dto.getActivationLinkValidHours() != null ? dto.getActivationLinkValidHours() : 24
                    );
                    showSuccess(msg);

                    resetForm();
                });
            }

            @Override
            public void onError(String msg, int httpCode) {
                runOnUiThread(() -> {
                    setSubmitting(false);
                    showError(msg != null && !msg.trim().isEmpty() ? msg : ("HTTP " + httpCode));
                });
            }
        });
    }

    private String vehicleTypePayload() {
        int idx = spVehicleType.getSelectedItemPosition();
        if (idx == 1) return "luxury";
        if (idx == 2) return "van";
        return "standard";
    }

    private void resetForm() {
        etFirstName.setText("");
        etLastName.setText("");
        etAddress.setText("");
        etPhone.setText("");
        etEmail.setText("");

        etVehicleModel.setText("");
        etLicensePlate.setText("");

        etSeats.setText("4");
        spVehicleType.setSelection(0);
        cbBaby.setChecked(false);
        cbPet.setChecked(false);
    }

    private void setSubmitting(boolean submitting) {
        isSubmitting = submitting;
        progress.setVisibility(submitting ? View.VISIBLE : View.GONE);
        btnCreate.setEnabled(!submitting);
        btnCreate.setAlpha(submitting ? 0.65f : 1f);
        btnCreate.setText(submitting
                ? getString(R.string.admin_create_driver_creating)
                : getString(R.string.admin_create_driver_create));
    }

    private void showError(String msg) {
        if (msg == null || msg.trim().isEmpty()) {
            tvError.setVisibility(View.GONE);
        } else {
            tvError.setText(msg);
            tvError.setVisibility(View.VISIBLE);
        }
    }

    private void showSuccess(String msg) {
        if (msg == null || msg.trim().isEmpty()) {
            tvSuccess.setVisibility(View.GONE);
        } else {
            tvSuccess.setText(msg);
            tvSuccess.setVisibility(View.VISIBLE);
        }
    }

    private static String safe(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
