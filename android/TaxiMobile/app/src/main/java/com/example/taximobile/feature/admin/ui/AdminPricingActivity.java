package com.example.taximobile.feature.admin.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.taximobile.R;
import com.example.taximobile.feature.admin.data.AdminPricingRepository;
import com.example.taximobile.feature.admin.data.dto.response.AdminPricingResponseDto;
import com.example.taximobile.feature.admin.data.dto.request.AdminPricingUpdateRequestDto;

public class AdminPricingActivity extends AdminBaseActivity {

    private EditText etStandard, etLuxury, etVan;
    private Button btnSave;
    private TextView tvError, tvHint;

    private AdminPricingRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflateContent(R.layout.activity_admin_pricing);
        toolbar.setTitle("Pricing");

        etStandard = v.findViewById(R.id.etStandard);
        etLuxury = v.findViewById(R.id.etLuxury);
        etVan = v.findViewById(R.id.etVan);
        btnSave = v.findViewById(R.id.btnSave);
        tvError = v.findViewById(R.id.tvError);
        tvHint = v.findViewById(R.id.tvHint);

        repo = new AdminPricingRepository(this);

        btnSave.setOnClickListener(x -> save());
        load();
    }

    private void load() {
        showError(null);
        setLoading(true);

        repo.get(new AdminPricingRepository.GetCb() {
            @Override public void onSuccess(AdminPricingResponseDto dto) {
                runOnUiThread(() -> {
                    setLoading(false);
                    etStandard.setText(dto.getStandard());
                    etLuxury.setText(dto.getLuxury());
                    etVan.setText(dto.getVan());
                });
            }

            @Override public void onError(String msg) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showError(msg);
                });
            }
        });
    }

    private void save() {
        showError(null);

        String standard = safe(etStandard);
        String luxury = safe(etLuxury);
        String van = safe(etVan);

        if (!isValidMoney(standard) || !isValidMoney(luxury) || !isValidMoney(van)) {
            showError("Enter non-negative numbers for all fields (e.g. 120.50)");
            return;
        }

        setLoading(true);

        AdminPricingUpdateRequestDto req = new AdminPricingUpdateRequestDto(standard, luxury, van);
        repo.update(req, new AdminPricingRepository.SaveCb() {
            @Override public void onSuccess() {
                runOnUiThread(() -> {
                    setLoading(false);
                    // opcionalno: pokaži “saved”
                    tvHint.setText("Saved");
                    tvHint.setVisibility(View.VISIBLE);
                });
            }

            @Override public void onError(String msg) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showError(msg);
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        btnSave.setEnabled(!loading);
        btnSave.setText(loading ? "..." : "Save");
    }

    private void showError(String msg) {
        if (msg == null || msg.trim().isEmpty()) {
            tvError.setVisibility(View.GONE);
        } else {
            tvError.setText(msg);
            tvError.setVisibility(View.VISIBLE);
        }
        tvHint.setVisibility(View.GONE);
    }

    private static String safe(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private static boolean isValidMoney(String s) {
        if (TextUtils.isEmpty(s)) return false;
        try {
            double v = Double.parseDouble(s);
            return v >= 0.0;
        } catch (Exception e) {
            return false;
        }
    }
}
